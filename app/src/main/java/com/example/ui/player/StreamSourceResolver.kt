package com.example.ui.player

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.mp4.Mp4Extractor
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory
import androidx.media3.extractor.ts.TsExtractor
import com.example.data.model.ChannelServer
import okhttp3.Call
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit
import kotlin.math.min

/**
 * Supported media container/stream formats handled natively by the Unified Media3 ExoPlayer Engine.
 */
enum class StreamSourceType(val label: String) {
    HLS("HLS/M3U8"),
    MPEG_TS("MPEG-TS"),
    MP4("MP4"),
    MKV("MKV/WebM"),
    PROGRESSIVE_DIRECT("DIRECT")
}

/**
 * Shared, connection-pooled OkHttpClient provider tuned for low-latency HLS and MPEG-TS streaming.
 */
object StreamOkHttpClientProvider {
    val defaultUserAgent: String =
        "Mozilla/5.0 (Linux; Android 14; SmartTV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36 FoulTV/1.2.1"

    val sharedClient: OkHttpClient by lazy {
        val dispatcher = Dispatcher().apply {
            maxRequests = 48
            maxRequestsPerHost = 12
        }
        OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .connectionPool(ConnectionPool(16, 3, TimeUnit.MINUTES))
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
            .connectTimeout(9, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }
}

/**
 * Production-grade Media3 [HttpDataSource] backed by OkHttp with:
 * - Proper `Range` request handling (`206 Partial Content` and `200 OK` fallback)
 * - Active [Call] cancellation on [close] so switching channels immediately aborts in-flight sockets
 * - Strict EOF verification (`EOFException` when stream ends before expected `bytesToRead`)
 * - Diagnostic hooks for HTTP status codes and network errors
 */
@OptIn(UnstableApi::class)
class OkHttpStreamDataSource(
    private val client: OkHttpClient,
    private val userAgent: String,
    private val defaultHeaders: Map<String, String> = emptyMap(),
    private val onHttpCodeError: ((Int, String) -> Unit)? = null
) : BaseDataSource(true), HttpDataSource {

    private var dataSpec: DataSpec? = null
    private var currentCall: Call? = null
    private var response: Response? = null
    private var inputStream: InputStream? = null
    private var opened = false
    private var bytesToRead: Long = 0
    private var bytesRead: Long = 0
    private val requestProperties = mutableMapOf<String, String>()

    class Factory(
        private val client: OkHttpClient = StreamOkHttpClientProvider.sharedClient,
        private val userAgent: String = StreamOkHttpClientProvider.defaultUserAgent,
        private val onHttpCodeError: ((Int, String) -> Unit)? = null
    ) : HttpDataSource.Factory {
        private val defaultRequestProperties = mutableMapOf<String, String>()

        override fun createDataSource(): HttpDataSource {
            return OkHttpStreamDataSource(
                client = client,
                userAgent = userAgent,
                defaultHeaders = defaultRequestProperties.toMap(),
                onHttpCodeError = onHttpCodeError
            )
        }

        override fun setDefaultRequestProperties(defaultRequestProperties: MutableMap<String, String>): HttpDataSource.Factory {
            this.defaultRequestProperties.clear()
            this.defaultRequestProperties.putAll(defaultRequestProperties)
            return this
        }
    }

    override fun getUri(): Uri? {
        return response?.request?.url?.toString()?.let { Uri.parse(it) } ?: dataSpec?.uri
    }

    override fun getResponseCode(): Int {
        return response?.code ?: -1
    }

    override fun getResponseHeaders(): Map<String, List<String>> {
        return response?.headers?.toMultimap() ?: emptyMap()
    }

    override fun setRequestProperty(name: String, value: String) {
        requestProperties[name] = value
    }

    override fun clearRequestProperty(name: String) {
        requestProperties.remove(name)
    }

    override fun clearAllRequestProperties() {
        requestProperties.clear()
    }

    @Throws(HttpDataSource.HttpDataSourceException::class)
    override fun open(dataSpec: DataSpec): Long {
        this.dataSpec = dataSpec
        this.bytesRead = 0
        this.bytesToRead = 0
        transferInitializing(dataSpec)

        val requestBuilder = try {
            Request.Builder().url(dataSpec.uri.toString())
        } catch (e: IllegalArgumentException) {
            throw HttpDataSource.HttpDataSourceException(
                IOException("Malformed URL: ${dataSpec.uri}", e),
                dataSpec,
                androidx.media3.common.PlaybackException.ERROR_CODE_IO_UNSPECIFIED,
                HttpDataSource.HttpDataSourceException.TYPE_OPEN
            )
        }

        requestBuilder.header("User-Agent", userAgent)
        requestBuilder.header("Accept", "*/*")
        requestBuilder.header("Connection", "keep-alive")

        for ((k, v) in defaultHeaders) {
            requestBuilder.header(k, v)
        }
        for ((k, v) in dataSpec.httpRequestHeaders) {
            requestBuilder.header(k, v)
        }
        for ((k, v) in requestProperties) {
            requestBuilder.header(k, v)
        }

        if (dataSpec.position != 0L || dataSpec.length != C.LENGTH_UNSET.toLong()) {
            val rangeRequest = buildString {
                append("bytes=")
                append(dataSpec.position)
                append("-")
                if (dataSpec.length != C.LENGTH_UNSET.toLong()) {
                    append(dataSpec.position + dataSpec.length - 1)
                }
            }
            requestBuilder.header("Range", rangeRequest)
        }

        val call = client.newCall(requestBuilder.build())
        this.currentCall = call

        val executedResponse = try {
            call.execute()
        } catch (e: IOException) {
            this.currentCall = null
            throw HttpDataSource.HttpDataSourceException.createForIOException(
                e,
                dataSpec,
                HttpDataSource.HttpDataSourceException.TYPE_OPEN
            )
        }

        this.response = executedResponse
        val body = executedResponse.body
        val statusCode = executedResponse.code

        if (!executedResponse.isSuccessful || body == null) {
            val headersMap = executedResponse.headers.toMultimap()
            val errorUrl = dataSpec.uri.toString()
            closeQuietly()
            onHttpCodeError?.invoke(statusCode, errorUrl)
            throw HttpDataSource.InvalidResponseCodeException(
                statusCode,
                executedResponse.message,
                null,
                headersMap,
                dataSpec,
                ByteArray(0)
            )
        }

        val stream = body.byteStream()
        this.inputStream = stream

        // If server returned 200 OK instead of 206 Partial Content when a non-zero position was requested,
        // skip the leading bytes up to dataSpec.position.
        val bytesToSkip = if (statusCode == 200 && dataSpec.position != 0L) dataSpec.position else 0L
        if (bytesToSkip > 0L) {
            try {
                var skippedTotal = 0L
                val skipBuffer = ByteArray(4096)
                while (skippedTotal < bytesToSkip) {
                    val readCount = stream.read(
                        skipBuffer,
                        0,
                        min((bytesToSkip - skippedTotal).toInt(), skipBuffer.size)
                    )
                    if (Thread.currentThread().isInterrupted) {
                        throw IOException("Interrupted while skipping bytes")
                    }
                    if (readCount == -1) {
                        throw EOFException("Premature EOF while skipping to position $bytesToSkip")
                    }
                    skippedTotal += readCount
                }
            } catch (e: IOException) {
                closeQuietly()
                throw HttpDataSource.HttpDataSourceException.createForIOException(
                    e,
                    dataSpec,
                    HttpDataSource.HttpDataSourceException.TYPE_OPEN
                )
            }
        }

        bytesToRead = if (dataSpec.length != C.LENGTH_UNSET.toLong()) {
            dataSpec.length
        } else {
            val contentLength = body.contentLength()
            if (contentLength != -1L) contentLength - bytesToSkip else C.LENGTH_UNSET.toLong()
        }

        opened = true
        transferStarted(dataSpec)
        return bytesToRead
    }

    @Throws(HttpDataSource.HttpDataSourceException::class)
    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        if (bytesToRead != C.LENGTH_UNSET.toLong()) {
            val bytesRemaining = bytesToRead - bytesRead
            if (bytesRemaining == 0L) {
                return C.RESULT_END_OF_INPUT
            }
        }

        val currentSpec = dataSpec ?: return C.RESULT_END_OF_INPUT
        val maxReadLength = if (bytesToRead == C.LENGTH_UNSET.toLong()) {
            length
        } else {
            min(length.toLong(), bytesToRead - bytesRead).toInt()
        }

        val readBytes = try {
            inputStream?.read(buffer, offset, maxReadLength) ?: C.RESULT_END_OF_INPUT
        } catch (e: IOException) {
            throw HttpDataSource.HttpDataSourceException.createForIOException(
                e,
                currentSpec,
                HttpDataSource.HttpDataSourceException.TYPE_READ
            )
        }

        if (readBytes == -1) {
            if (bytesToRead != C.LENGTH_UNSET.toLong() && bytesRead < bytesToRead) {
                throw HttpDataSource.HttpDataSourceException.createForIOException(
                    EOFException("Stream ended prematurely: read $bytesRead of $bytesToRead bytes"),
                    currentSpec,
                    HttpDataSource.HttpDataSourceException.TYPE_READ
                )
            }
            return C.RESULT_END_OF_INPUT
        }

        bytesRead += readBytes
        bytesTransferred(readBytes)
        return readBytes
    }

    override fun close() {
        try {
            closeQuietly()
        } finally {
            if (opened) {
                opened = false
                transferEnded()
            }
        }
    }

    private fun closeQuietly() {
        try {
            inputStream?.close()
        } catch (_: Exception) {
        }
        inputStream = null

        try {
            response?.body?.close()
            response?.close()
        } catch (_: Exception) {
        }
        response = null

        try {
            currentCall?.cancel()
        } catch (_: Exception) {
        }
        currentCall = null
    }
}

/**
 * Resolves stream source types (HLS/M3U8, MPEG-TS, MP4, MKV, Direct) and builds the exact
 * Media3 [MediaSource] required without ever mutating URL extensions (.m3u8 <-> .ts).
 */
@OptIn(UnstableApi::class)
object StreamSourceResolver {

    /**
     * Classifies the stream URL using:
     * 1. Explicit server streamType hint (e.g. "m3u8", "hls", "ts", "mp4", "mkv")
     * 2. URL path extension and path segments (before query parameters)
     * 3. Query string format hints (e.g. `output=m3u8`, `output=ts`, `type=m3u8`, `extension=ts`)
     * 4. Xtream Codes URL structure (`/live/user/pass/12345.m3u8`, `/live/user/pass/12345.ts`, or extensionless `/live/user/pass/12345`)
     */
    fun detectStreamSourceType(
        url: String,
        serverStreamTypeHint: String = "",
        isLiveChannel: Boolean = true
    ): StreamSourceType {
        val hintLower = serverStreamTypeHint.trim().lowercase()
        when (hintLower) {
            "m3u8", "hls", "application/x-mpegurl", "application/vnd.apple.mpegurl" -> return StreamSourceType.HLS
            "ts", "mpegts", "mpeg-ts", "video/mp2t" -> return StreamSourceType.MPEG_TS
            "mp4", "m4v", "video/mp4" -> return StreamSourceType.MP4
            "mkv", "webm", "video/x-matroska", "video/webm" -> return StreamSourceType.MKV
        }

        val cleanUrl = url.trim()
        if (cleanUrl.isEmpty()) {
            return if (isLiveChannel) StreamSourceType.HLS else StreamSourceType.PROGRESSIVE_DIRECT
        }

        val lowerUrl = cleanUrl.lowercase()
        val pathBeforeQuery = lowerUrl.substringBefore('?').substringBefore('#')
        val queryPart = lowerUrl.substringAfter('?', "")

        // 1. Check explicit path extensions
        when {
            pathBeforeQuery.endsWith(".m3u8") ||
                pathBeforeQuery.endsWith(".m3u") ||
                pathBeforeQuery.contains(".m3u8/") ||
                pathBeforeQuery.contains("/hls/") ||
                pathBeforeQuery.contains("/manifest.m3u8") -> return StreamSourceType.HLS

            pathBeforeQuery.endsWith(".ts") ||
                pathBeforeQuery.endsWith(".mts") ||
                pathBeforeQuery.endsWith(".mpegts") -> return StreamSourceType.MPEG_TS

            pathBeforeQuery.endsWith(".mp4") ||
                pathBeforeQuery.endsWith(".m4v") ||
                pathBeforeQuery.endsWith(".mov") -> return StreamSourceType.MP4

            pathBeforeQuery.endsWith(".mkv") ||
                pathBeforeQuery.endsWith(".webm") -> return StreamSourceType.MKV
        }

        // 2. Check query parameter format hints
        if (queryPart.isNotEmpty()) {
            when {
                queryPart.contains("output=m3u8") ||
                    queryPart.contains("output=hls") ||
                    queryPart.contains("type=m3u8") ||
                    queryPart.contains("format=m3u8") ||
                    queryPart.contains("format=hls") ||
                    queryPart.contains("ext=m3u8") ||
                    queryPart.contains(".m3u8") -> return StreamSourceType.HLS

                queryPart.contains("output=ts") ||
                    queryPart.contains("output=mpegts") ||
                    queryPart.contains("type=ts") ||
                    queryPart.contains("format=ts") ||
                    queryPart.contains("extension=ts") ||
                    queryPart.contains("ext=ts") -> return StreamSourceType.MPEG_TS

                queryPart.contains("ext=mp4") ||
                    queryPart.contains("format=mp4") -> return StreamSourceType.MP4

                queryPart.contains("ext=mkv") ||
                    queryPart.contains("format=mkv") -> return StreamSourceType.MKV
            }
        }

        // 3. Xtream Codes extensionless live stream pattern:
        // e.g. http://host:port/live/user/pass/12345 or http://host:port/user/pass/12345
        // Extensionless Xtream live URLs deliver raw MPEG-TS byte streams by default.
        val isXtreamExtensionlessLive = isLiveChannel && (
            Regex(".*/live/[^/]+/[^/]+/\\d+$").matches(pathBeforeQuery) ||
                Regex("^https?://[^/]+/[^/]+/[^/]+/\\d+$").matches(pathBeforeQuery)
            )
        if (isXtreamExtensionlessLive) {
            return StreamSourceType.MPEG_TS
        }

        return if (isLiveChannel) {
            StreamSourceType.PROGRESSIVE_DIRECT
        } else {
            StreamSourceType.MP4
        }
    }

    /**
     * Builds a dedicated [MediaItem] and [MediaSource] tailored to the detected [StreamSourceType].
     * - HLS -> [HlsMediaSource] with `allowChunklessPreparation = true` and stable live offset configuration.
     * - MPEG-TS -> [ProgressiveMediaSource] with a high-tolerance [TsExtractor] (non-IDR keyframes, access unit detection, splice info ignore, 1300 TS packet timestamp search).
     * - MP4 / MKV -> [ProgressiveMediaSource] with constant bitrate seeking enabled.
     * - PROGRESSIVE_DIRECT -> [DefaultMediaSourceFactory] backed by both HLS and tuned TS/Progressive extractors for extensionless URLs.
     */
    fun createMediaSource(
        url: String,
        sourceType: StreamSourceType,
        isLiveChannel: Boolean,
        dataSourceFactory: DataSource.Factory
    ): MediaSource {
        val uri = Uri.parse(url.trim())
        val mediaItemBuilder = MediaItem.Builder().setUri(uri)

        if (isLiveChannel && sourceType == StreamSourceType.HLS) {
            mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
            mediaItemBuilder.setLiveConfiguration(
                MediaItem.LiveConfiguration.Builder()
                    .setTargetOffsetMs(6_000L)
                    .setMinOffsetMs(3_000L)
                    .setMaxOffsetMs(18_000L)
                    .setMinPlaybackSpeed(1.0f)
                    .setMaxPlaybackSpeed(1.0f)
                    .build()
            )
        } else {
            when (sourceType) {
                StreamSourceType.HLS -> mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
                StreamSourceType.MPEG_TS -> mediaItemBuilder.setMimeType(MimeTypes.VIDEO_MP2T)
                StreamSourceType.MP4 -> mediaItemBuilder.setMimeType(MimeTypes.VIDEO_MP4)
                StreamSourceType.MKV -> mediaItemBuilder.setMimeType(MimeTypes.VIDEO_MATROSKA)
                StreamSourceType.PROGRESSIVE_DIRECT -> {
                    // Leave MIME type open for container sniffing by DefaultMediaSourceFactory
                }
            }
        }

        val mediaItem = mediaItemBuilder.build()

        return when (sourceType) {
            StreamSourceType.HLS -> {
                HlsMediaSource.Factory(dataSourceFactory)
                    .setAllowChunklessPreparation(true)
                    .createMediaSource(mediaItem)
            }

            StreamSourceType.MPEG_TS -> {
                val tsExtractorsFactory = DefaultExtractorsFactory()
                    .setTsExtractorFlags(
                        DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES or
                            DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS or
                            DefaultTsPayloadReaderFactory.FLAG_IGNORE_SPLICE_INFO_STREAM
                    )
                    .setTsExtractorTimestampSearchBytes(1300 * TsExtractor.TS_PACKET_SIZE)
                    .setConstantBitrateSeekingEnabled(true)

                ProgressiveMediaSource.Factory(dataSourceFactory, tsExtractorsFactory)
                    .setContinueLoadingCheckIntervalBytes(512 * 1024)
                    .createMediaSource(mediaItem)
            }

            StreamSourceType.MP4,
            StreamSourceType.MKV -> {
                val progressiveExtractors = DefaultExtractorsFactory()
                    .setConstantBitrateSeekingEnabled(true)
                    .setMp4ExtractorFlags(Mp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS)

                ProgressiveMediaSource.Factory(dataSourceFactory, progressiveExtractors)
                    .createMediaSource(mediaItem)
            }

            StreamSourceType.PROGRESSIVE_DIRECT -> {
                val hybridExtractors = DefaultExtractorsFactory()
                    .setTsExtractorFlags(
                        DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES or
                            DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS or
                            DefaultTsPayloadReaderFactory.FLAG_IGNORE_SPLICE_INFO_STREAM
                    )
                    .setTsExtractorTimestampSearchBytes(1300 * TsExtractor.TS_PACKET_SIZE)
                    .setConstantBitrateSeekingEnabled(true)

                DefaultMediaSourceFactory(dataSourceFactory, hybridExtractors)
                    .createMediaSource(mediaItem)
            }
        }
    }

    /**
     * Selects the safest initial server index when a channel has multiple servers.
     *
     * Rules:
     * - If Server #0 is a standard H.264 stream (not labeled 4K/UHD/FHD/1080/HEVC/H.265), keep Server #0.
     * - If Server #0 is ultra-heavy (4K/UHD/FHD/1080p) or uses HEVC/H.265 (which often lacks hardware
     *   decoders on budget phones and Android TV boxes), prefer a standard H.264 HD (720p) or SD/Low
     *   server first so playback starts fast and smoothly without decoder failure.
     * - Never auto-selects HEVC/H.265 over standard H.264 HD/SD.
     */
    fun selectOptimalInitialServerIndex(
        servers: List<ChannelServer>,
        isLiveChannel: Boolean
    ): Int {
        if (!isLiveChannel || servers.size <= 1) return 0

        fun isHevcLabel(label: String): Boolean {
            return label.contains("hevc") || label.contains("h265") || label.contains("h.265") || label.contains("x265")
        }

        fun isUltraHeavyLabel(label: String): Boolean {
            return label.contains("4k") ||
                label.contains("uhd") ||
                label.contains("2160") ||
                label.contains("fhd") ||
                label.contains("1080") ||
                label.contains("raw")
        }

        val firstLabel = servers.first().name.lowercase()
        if (!isUltraHeavyLabel(firstLabel) && !isHevcLabel(firstLabel)) {
            return 0
        }

        // 1. Prefer standard HD (720p) without HEVC/FHD/4K
        val standardHdIndex = servers.indexOfFirst { server ->
            val label = server.name.lowercase()
            !isHevcLabel(label) &&
                !isUltraHeavyLabel(label) &&
                (label.contains("hd") || label.contains("720") || label.contains("متوسط") || label.contains("auto"))
        }
        if (standardHdIndex >= 0) return standardHdIndex

        // 2. Prefer SD / Low / Mobile without HEVC
        val sdIndex = servers.indexOfFirst { server ->
            val label = server.name.lowercase()
            !isHevcLabel(label) &&
                (label.contains("sd") || label.contains("low") || label.contains("480") || label.contains("360") || label.contains("ضعيف"))
        }
        if (sdIndex >= 0) return sdIndex

        // 3. Any server that is neither ultra-heavy nor HEVC
        val anySafeIndex = servers.indexOfFirst { server ->
            val label = server.name.lowercase()
            !isHevcLabel(label) && !isUltraHeavyLabel(label)
        }
        if (anySafeIndex >= 0) return anySafeIndex

        return 0
    }
}
