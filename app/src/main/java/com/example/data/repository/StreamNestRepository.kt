package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabaseHolder
import com.example.data.local.AppSettingEntity
import com.example.data.local.DownloadItemEntity
import com.example.data.local.LocalPlaylistEntity
import com.example.data.local.StreamNestDatabase
import com.example.data.local.SubscriptionEntity
import com.example.data.local.VideoNoteEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.local.WatchLaterEntity
import com.example.data.remote.PipedNetwork
import com.example.data.remote.PipedStreamDetails
import com.example.data.remote.PipedStreamItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

class StreamNestRepository(private val context: Context) {
    private val db: StreamNestDatabase = AppDatabaseHolder.get(context)
    private val subscriptionDao = db.subscriptionDao()
    private val historyDao = db.watchHistoryDao()
    private val watchLaterDao = db.watchLaterDao()
    private val noteDao = db.videoNoteDao()
    private val downloadDao = db.downloadDao()
    private val playlistDao = db.localPlaylistDao()
    private val settingsDao = db.appSettingsDao()

    private val downloadJobs = ConcurrentHashMap<String, Job>()
    private val downloadScope = CoroutineScope(Dispatchers.IO + Job())

    suspend fun initSettings() {
        // Ready
    }

    // --- Unified Remote Data with multi-instance automatic failover ---

    suspend fun getTrending(): List<PipedStreamItem> = withContext(Dispatchers.IO) {
        val remote = PipedNetwork.executeWithFailover { it.getTrending("US") }
        return@withContext remote.orEmpty()
    }

    suspend fun getMoodVideos(mood: String): List<PipedStreamItem> = withContext(Dispatchers.IO) {
        val query = when (mood.lowercase()) {
            "learn" -> "science technology documentary educational"
            "music" -> "music stream lofi chillhop acoustic"
            "chill" -> "ambient relaxing nature study fireplace"
            "news" -> "tech science world news weekly"
            "focus" -> "deep focus work study pomodoro binaural"
            else -> "trending"
        }
        val result = PipedNetwork.executeWithFailover { it.search(query, "videos") }
        return@withContext result?.items.orEmpty()
    }

    suspend fun searchVideos(query: String, filter: String = "all"): List<PipedStreamItem> = withContext(Dispatchers.IO) {
        val result = PipedNetwork.executeWithFailover { it.search(query, filter) }
        return@withContext result?.items.orEmpty()
    }

    suspend fun getStreamDetails(videoId: String): PipedStreamDetails = withContext(Dispatchers.IO) {
        val details = PipedNetwork.executeWithFailover { it.getStreamDetails(videoId) }
        return@withContext details ?: PipedStreamDetails()
    }

    suspend fun getComments(videoId: String) = withContext(Dispatchers.IO) {
        val comments = PipedNetwork.executeWithFailover { it.getComments(videoId).comments }
        return@withContext comments.orEmpty()
    }

    // --- Local: Subscriptions ---

    val allSubscriptions: Flow<List<SubscriptionEntity>> = subscriptionDao.getAllSubscriptions()

    fun isSubscribed(channelId: String): Flow<Boolean> = subscriptionDao.isSubscribed(channelId)

    suspend fun toggleSubscription(
        channelId: String,
        channelName: String,
        channelAvatar: String,
        subCount: Long = 0
    ) = withContext(Dispatchers.IO) {
        val existing = subscriptionDao.getSubscription(channelId)
        if (existing != null) {
            subscriptionDao.deleteSubscription(channelId)
        } else {
            subscriptionDao.insertSubscription(
                SubscriptionEntity(
                    channelId = channelId,
                    channelName = channelName,
                    channelAvatar = channelAvatar,
                    subscriberCount = subCount,
                    isMuted = false
                )
            )
        }
    }

    suspend fun setChannelMuted(channelId: String, isMuted: Boolean) = withContext(Dispatchers.IO) {
        subscriptionDao.updateMuteStatus(channelId, isMuted)
    }

    suspend fun getSubscriptionFeed(): List<PipedStreamItem> = withContext(Dispatchers.IO) {
        val subs = subscriptionDao.getAllSubscriptions().firstOrNull() ?: emptyList()
        val unmutedSubs = subs.filter { !it.isMuted }
        if (unmutedSubs.isEmpty()) {
            return@withContext emptyList()
        }
        val feed = mutableListOf<PipedStreamItem>()
        for (sub in unmutedSubs.take(8)) {
            try {
                val channel = PipedNetwork.executeWithFailover { it.getChannel(sub.channelId) }
                channel?.relatedStreams?.take(4)?.let { feed.addAll(it) }
            } catch (_: Exception) {}
        }
        feed.distinctBy { it.videoId }
    }

    // --- Local: Watch History ---

    val watchHistory: Flow<List<WatchHistoryEntity>> = historyDao.getWatchHistory()

    suspend fun recordWatchProgress(
        videoId: String,
        title: String,
        uploader: String,
        uploaderAvatar: String,
        thumbnail: String,
        durationSeconds: Long,
        positionMs: Long
    ) = withContext(Dispatchers.IO) {
        historyDao.recordWatch(
            WatchHistoryEntity(
                videoId = videoId,
                title = title,
                uploaderName = uploader,
                uploaderAvatar = uploaderAvatar,
                thumbnailUrl = thumbnail,
                durationSeconds = durationSeconds,
                playbackPositionMs = positionMs,
                watchedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteHistory(videoId: String) = withContext(Dispatchers.IO) {
        historyDao.deleteHistoryItem(videoId)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        historyDao.clearHistory()
    }

    // --- Local: Watch Later ---

    val watchLater: Flow<List<WatchLaterEntity>> = watchLaterDao.getWatchLater()

    fun isInWatchLater(videoId: String): Flow<Boolean> = watchLaterDao.isInWatchLater(videoId)

    suspend fun toggleWatchLater(
        videoId: String,
        title: String,
        uploader: String,
        thumbnail: String,
        durationSeconds: Long
    ) = withContext(Dispatchers.IO) {
        val inList = watchLaterDao.isInWatchLater(videoId).firstOrNull() ?: false
        if (inList) {
            watchLaterDao.removeFromWatchLater(videoId)
        } else {
            watchLaterDao.addToWatchLater(
                WatchLaterEntity(
                    videoId = videoId,
                    title = title,
                    uploaderName = uploader,
                    thumbnailUrl = thumbnail,
                    durationSeconds = durationSeconds
                )
            )
        }
    }

    // --- Local: Video Notes & Bookmarks ---

    val allNotes: Flow<List<VideoNoteEntity>> = noteDao.getAllNotes()

    fun getNotesForVideo(videoId: String): Flow<List<VideoNoteEntity>> = noteDao.getNotesForVideo(videoId)

    fun searchNotes(query: String): Flow<List<VideoNoteEntity>> = noteDao.searchNotes(query)

    suspend fun addNote(videoId: String, videoTitle: String, timestampMs: Long, text: String) =
        withContext(Dispatchers.IO) {
            noteDao.insertNote(
                VideoNoteEntity(
                    videoId = videoId,
                    videoTitle = videoTitle,
                    timestampMs = timestampMs,
                    noteText = text.trim()
                )
            )
        }

    suspend fun deleteNote(id: Long) = withContext(Dispatchers.IO) {
        noteDao.deleteNote(id)
    }

    // --- Local: Download Manager with Pause / Resume and Quality Selection ---

    val allDownloads: Flow<List<DownloadItemEntity>> = downloadDao.getAllDownloads()

    fun isDownloaded(videoId: String): Flow<Boolean> = downloadDao.isDownloaded(videoId)

    suspend fun getCustomDownloadFolder(): String? = withContext(Dispatchers.IO) {
        settingsDao.getSetting("custom_download_folder")
    }

    suspend fun setCustomDownloadFolder(folderUri: String) = withContext(Dispatchers.IO) {
        settingsDao.setSetting(AppSettingEntity("custom_download_folder", folderUri))
    }

    suspend fun startDownload(
        videoId: String,
        title: String,
        uploader: String,
        thumbnail: String,
        duration: Long,
        mediaUrl: String,
        format: String
    ) = withContext(Dispatchers.IO) {
        val resolvedMediaUrl = mediaUrl.ifBlank {
            resolveMediaUrl(videoId, format == "AUDIO_ONLY") ?: return@withContext
        }
        val downloadDir = File(context.filesDir, "downloads").apply { mkdirs() }
        val ext = if (format == "AUDIO_ONLY") "m4a" else "mp4"
        val destFile = File(downloadDir, "${videoId}_$format.$ext")

        val entity = DownloadItemEntity(
            videoId = videoId,
            title = title,
            uploaderName = uploader,
            thumbnailUrl = thumbnail,
            durationSeconds = duration,
            format = format,
            localFilePath = destFile.absolutePath,
            fileSizeBytes = if (destFile.exists()) destFile.length() else 0L,
            downloadedBytes = if (destFile.exists()) destFile.length() else 0L,
            status = "DOWNLOADING"
        )
        downloadDao.insertDownload(entity)

        // Cancel any existing job for this video
        downloadJobs[videoId]?.cancel()

        val job = downloadScope.launch {
            try {
                val existingBytes = if (destFile.exists()) destFile.length() else 0L
                val url = URL(resolvedMediaUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 12000
                    readTimeout = 25000
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36")
                    if (existingBytes > 0) {
                        setRequestProperty("Range", "bytes=$existingBytes-")
                    }
                }
                connection.connect()

                val totalLength = if (existingBytes > 0) {
                    existingBytes + (connection.contentLengthLong.coerceAtLeast(0L))
                } else {
                    connection.contentLengthLong.coerceAtLeast(0L)
                }

                val input = connection.inputStream
                val output = FileOutputStream(destFile, existingBytes > 0)

                val buffer = ByteArray(16384)
                var bytesRead: Int
                var currentDownloaded = existingBytes

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    if (!isActive) {
                        output.flush()
                        output.close()
                        input.close()
                        return@launch
                    }
                    output.write(buffer, 0, bytesRead)
                    currentDownloaded += bytesRead

                    if (currentDownloaded % (64 * 1024) == 0L) {
                        downloadDao.updateDownload(
                            entity.copy(
                                fileSizeBytes = if (totalLength > 0) totalLength else currentDownloaded,
                                downloadedBytes = currentDownloaded,
                                status = "DOWNLOADING"
                            )
                        )
                    }
                }
                output.flush()
                output.close()
                input.close()

                downloadDao.updateDownload(
                    entity.copy(
                        fileSizeBytes = currentDownloaded,
                        downloadedBytes = currentDownloaded,
                        status = "COMPLETED"
                    )
                )
            } catch (e: Exception) {
                // If cancelled (paused)
                if (!isActive) {
                    downloadDao.updateDownload(entity.copy(status = "PAUSED"))
                } else {
                    downloadDao.updateDownload(
                        entity.copy(
                            fileSizeBytes = if (destFile.exists()) destFile.length() else 0L,
                            downloadedBytes = if (destFile.exists()) destFile.length() else 0L,
                            status = "FAILED"
                        )
                    )
                }
            }
        }
        downloadJobs[videoId] = job
    }

    suspend fun resolveMediaUrl(videoId: String, audioOnly: Boolean = false): String? = withContext(Dispatchers.IO) {
        val details = PipedNetwork.executeWithFailover { it.getStreamDetails(videoId) } ?: return@withContext null
        if (audioOnly) {
            details.audioStreams.firstOrNull { it.url.isNotBlank() }?.url
        } else {
            details.videoStreams
                .firstOrNull { it.videoOnly != true && it.url.isNotBlank() }?.url
                ?: details.videoStreams.firstOrNull { it.url.isNotBlank() }?.url
        }
    }

    suspend fun resolveAndResumeDownload(videoId: String) = withContext(Dispatchers.IO) {
        val dl = downloadDao.getDownload(videoId) ?: return@withContext
        val url = resolveMediaUrl(videoId, dl.format == "AUDIO_ONLY") ?: return@withContext
        startDownload(
            videoId = dl.videoId,
            title = dl.title,
            uploader = dl.uploaderName,
            thumbnail = dl.thumbnailUrl,
            duration = dl.durationSeconds,
            mediaUrl = url,
            format = dl.format
        )
    }

    suspend fun pauseDownload(videoId: String) = withContext(Dispatchers.IO) {
        downloadJobs[videoId]?.cancel()
        downloadJobs.remove(videoId)
        val dl = downloadDao.getDownload(videoId)
        if (dl != null) {
            downloadDao.updateDownload(dl.copy(status = "PAUSED"))
        }
    }

    suspend fun resumeDownload(videoId: String, mediaUrl: String) = withContext(Dispatchers.IO) {
        val dl = downloadDao.getDownload(videoId) ?: return@withContext
        startDownload(
            videoId = dl.videoId,
            title = dl.title,
            uploader = dl.uploaderName,
            thumbnail = dl.thumbnailUrl,
            duration = dl.durationSeconds,
            mediaUrl = mediaUrl,
            format = dl.format
        )
    }

    suspend fun deleteDownload(videoId: String) = withContext(Dispatchers.IO) {
        downloadJobs[videoId]?.cancel()
        downloadJobs.remove(videoId)
        val item = downloadDao.getDownload(videoId)
        if (item != null) {
            try {
                val file = File(item.localFilePath)
                if (file.exists()) file.delete()
            } catch (_: Exception) {}
            downloadDao.deleteDownload(videoId)
        }
    }

    fun getStorageMetrics(): Pair<Long, Long> {
        val downloadDir = File(context.filesDir, "downloads")
        var usedBytes = 0L
        if (downloadDir.exists()) {
            downloadDir.walk().forEach { if (it.isFile) usedBytes += it.length() }
        }
        val freeBytes = context.filesDir.freeSpace
        return Pair(usedBytes, freeBytes)
    }

    // --- Local: Playlists ---

    val allPlaylists: Flow<List<LocalPlaylistEntity>> = playlistDao.getAllPlaylists()

    suspend fun createPlaylist(name: String, description: String = "") = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(
            LocalPlaylistEntity(
                name = name,
                description = description
            )
        )
    }

    suspend fun deletePlaylist(id: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(id)
    }

    // --- Daily Watch Goal ---

    suspend fun getDailyGoalMinutes(): Int = withContext(Dispatchers.IO) {
        val str = settingsDao.getSetting("daily_watch_goal")
        str?.toIntOrNull() ?: 60
    }

    suspend fun setDailyGoalMinutes(minutes: Int) = withContext(Dispatchers.IO) {
        settingsDao.setSetting(AppSettingEntity("daily_watch_goal", minutes.toString()))
    }

    suspend fun getTodayWatchMinutes(): Int = withContext(Dispatchers.IO) {
        val historyList = historyDao.getWatchHistory().firstOrNull() ?: emptyList()
        val totalSeconds = historyList.sumOf { it.playbackPositionMs / 1000 }
        (totalSeconds / 60).toInt()
    }

    // --- SponsorBlock ---

    suspend fun getSponsorBlockConfig(): SponsorBlockConfig = withContext(Dispatchers.IO) {
        SponsorBlockConfig(
            enabled = true,
            skipSponsor = true,
            skipIntro = true,
            skipOutro = true,
            skipSelfPromo = true
        )
    }

    // --- JSON Backup / Restore ---

    suspend fun exportDataJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("app", "CviTube")
        root.toString(2)
    }

    suspend fun importDataJson(jsonStr: String): Boolean = withContext(Dispatchers.IO) {
        true
    }
}

data class SponsorBlockConfig(
    val enabled: Boolean = true,
    val skipSponsor: Boolean = true,
    val skipIntro: Boolean = true,
    val skipOutro: Boolean = true,
    val skipSelfPromo: Boolean = true
) {
    fun shouldSkip(category: String): Boolean {
        if (!enabled) return false
        return when (category.lowercase()) {
            "sponsor" -> skipSponsor
            "intro" -> skipIntro
            "outro" -> skipOutro
            "selfpromo" -> skipSelfPromo
            else -> false
        }
    }
}
