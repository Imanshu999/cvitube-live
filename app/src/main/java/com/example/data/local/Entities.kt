package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val channelId: String,
    val channelName: String,
    val channelAvatar: String,
    val subscriberCount: Long = 0,
    val isMuted: Boolean = false,
    val subscribedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val uploaderName: String,
    val uploaderAvatar: String = "",
    val thumbnailUrl: String = "",
    val durationSeconds: Long = 0,
    val playbackPositionMs: Long = 0,
    val watchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_later")
data class WatchLaterEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val uploaderName: String,
    val thumbnailUrl: String = "",
    val durationSeconds: Long = 0,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "video_notes")
data class VideoNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoId: String,
    val videoTitle: String,
    val timestampMs: Long,
    val noteText: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadItemEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val uploaderName: String,
    val thumbnailUrl: String = "",
    val durationSeconds: Long = 0,
    val format: String = "VIDEO_720P", // "VIDEO_720P", "AUDIO_ONLY"
    val localFilePath: String = "",
    val fileSizeBytes: Long = 0,
    val downloadedBytes: Long = 0,
    val status: String = "COMPLETED", // "COMPLETED", "DOWNLOADING", "PAUSED", "FAILED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_playlists")
data class LocalPlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val videoIdsJson: String = "[]" // JSON array of video IDs
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
