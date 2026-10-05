package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class PipedStreamItem(
    @Json(name = "url") val url: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "thumbnail") val thumbnail: String? = null,
    @Json(name = "uploaderName") val uploaderName: String? = null,
    @Json(name = "uploaderUrl") val uploaderUrl: String? = null,
    @Json(name = "uploaderAvatar") val uploaderAvatar: String? = null,
    @Json(name = "uploadedDate") val uploadedDate: String? = null,
    @Json(name = "shortDescription") val shortDescription: String? = null,
    @Json(name = "duration") val duration: Long? = 0,
    @Json(name = "views") val views: Long? = 0,
    @Json(name = "type") val type: String? = "stream"
) {
    val videoId: String
        get() {
            if (url == null) return ""
            return if (url.contains("v=")) {
                url.substringAfter("v=").substringBefore("&")
            } else if (url.startsWith("/watch?v=")) {
                url.removePrefix("/watch?v=").substringBefore("&")
            } else {
                url.trimStart('/')
            }
        }
}

@JsonClass(generateAdapter = true)
data class PipedSearchResult(
    @Json(name = "items") val items: List<PipedStreamItem> = emptyList(),
    @Json(name = "nextpage") val nextpage: String? = null
)

@JsonClass(generateAdapter = true)
data class PipedAudioStream(
    @Json(name = "url") val url: String,
    @Json(name = "format") val format: String? = null,
    @Json(name = "quality") val quality: String? = null,
    @Json(name = "mimeType") val mimeType: String? = null,
    @Json(name = "bitrate") val bitrate: Long? = 0
)

@JsonClass(generateAdapter = true)
data class PipedVideoStream(
    @Json(name = "url") val url: String,
    @Json(name = "format") val format: String? = null,
    @Json(name = "quality") val quality: String? = null,
    @Json(name = "mimeType") val mimeType: String? = null,
    @Json(name = "videoOnly") val videoOnly: Boolean? = false,
    @Json(name = "height") val height: Int? = null,
    @Json(name = "width") val width: Int? = null,
    @Json(name = "fps") val fps: Int? = null,
    @Json(name = "bitrate") val bitrate: Long? = 0
)

@JsonClass(generateAdapter = true)
data class PipedChapter(
    @Json(name = "title") val title: String = "",
    @Json(name = "start") val start: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class PipedSponsorSegment(
    @Json(name = "category") val category: String = "sponsor",
    @Json(name = "segment") val segment: List<Double> = emptyList(),
    @Json(name = "UUID") val uuid: String? = null
) {
    val startSeconds: Double get() = segment.getOrNull(0) ?: 0.0
    val endSeconds: Double get() = segment.getOrNull(1) ?: 0.0
}

@JsonClass(generateAdapter = true)
data class PipedStreamDetails(
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "uploadDate") val uploadDate: String? = null,
    @Json(name = "uploader") val uploader: String? = null,
    @Json(name = "uploaderUrl") val uploaderUrl: String? = null,
    @Json(name = "uploaderAvatar") val uploaderAvatar: String? = null,
    @Json(name = "uploaderSubscriberCount") val uploaderSubscriberCount: Long? = 0,
    @Json(name = "views") val views: Long? = 0,
    @Json(name = "likes") val likes: Long? = 0,
    @Json(name = "duration") val duration: Long? = 0,
    @Json(name = "audioStreams") val audioStreams: List<PipedAudioStream> = emptyList(),
    @Json(name = "videoStreams") val videoStreams: List<PipedVideoStream> = emptyList(),
    @Json(name = "relatedStreams") val relatedStreams: List<PipedStreamItem> = emptyList(),
    @Json(name = "chapters") val chapters: List<PipedChapter> = emptyList(),
    @Json(name = "sponsorSegments") val sponsorSegments: List<PipedSponsorSegment> = emptyList(),
    @Json(name = "hls") val hls: String? = null
)

@JsonClass(generateAdapter = true)
data class PipedCommentItem(
    @Json(name = "author") val author: String? = null,
    @Json(name = "thumbnail") val thumbnail: String? = null,
    @Json(name = "commentText") val commentText: String? = null,
    @Json(name = "commentedTime") val commentedTime: String? = null,
    @Json(name = "likeCount") val likeCount: Long? = 0
)

@JsonClass(generateAdapter = true)
data class PipedCommentsResponse(
    @Json(name = "comments") val comments: List<PipedCommentItem> = emptyList(),
    @Json(name = "nextpage") val nextpage: String? = null
)

@JsonClass(generateAdapter = true)
data class PipedChannelResponse(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "avatarUrl") val avatarUrl: String? = null,
    @Json(name = "bannerUrl") val bannerUrl: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "subscriberCount") val subscriberCount: Long? = 0,
    @Json(name = "relatedStreams") val relatedStreams: List<PipedStreamItem> = emptyList()
)

interface PipedApiService {
    @GET("trending")
    suspend fun getTrending(@Query("region") region: String = "US"): List<PipedStreamItem>

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("filter") filter: String = "all"
    ): PipedSearchResult

    @GET("streams/{videoId}")
    suspend fun getStreamDetails(@Path("videoId") videoId: String): PipedStreamDetails

    @GET("comments/{videoId}")
    suspend fun getComments(@Path("videoId") videoId: String): PipedCommentsResponse

    @GET("channel/{channelId}")
    suspend fun getChannel(@Path("channelId") channelId: String): PipedChannelResponse
}

object PipedNetwork {
    val PUBLIC_INSTANCES = listOf(
        "https://pipedapi.kavin.rocks",
        "https://api.piped.private.coffee",
        "https://pipedapi.tokhmi.xyz",
        "https://piped-api.lunar.icu",
        "https://api-piped.mha.fi",
        "https://pipedapi.adminforge.de",
        "https://pipedapi.colins.app",
        "https://pipedapi.leptons.xyz",
        "https://pipedapi.drgns.space",
        "https://pa.il.ax",
        "https://pipedapi.rivo.cc",
        "https://pipedapi.smnz.de"
    )

    @Volatile
    private var preferredInstance: String = PUBLIC_INSTANCES.first()

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    private val serviceCache = mutableMapOf<String, PipedApiService>()

    @Synchronized
    fun getService(baseUrl: String = preferredInstance): PipedApiService {
        val sanitized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return serviceCache.getOrPut(sanitized) {
            Retrofit.Builder()
                .baseUrl(sanitized)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(PipedApiService::class.java)
        }
    }

    suspend fun <T> executeWithFailover(block: suspend (PipedApiService) -> T): T? {
        // Try the preferred working instance first
        val currentPreferred = preferredInstance
        try {
            val service = getService(currentPreferred)
            val result = block(service)
            if (result != null) return result
        } catch (_: Exception) {
            // Preferred failed, cycle through all unified instances
        }

        for (instance in PUBLIC_INSTANCES) {
            if (instance == currentPreferred) continue
            try {
                val service = getService(instance)
                val result = block(service)
                if (result != null) {
                    preferredInstance = instance
                    return result
                }
            } catch (_: Exception) {
                // seamlessly try next instance in the pool
            }
        }
        return null
    }
}
