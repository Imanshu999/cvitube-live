package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SubscriptionEntity::class,
        WatchHistoryEntity::class,
        WatchLaterEntity::class,
        VideoNoteEntity::class,
        DownloadItemEntity::class,
        LocalPlaylistEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StreamNestDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun watchLaterDao(): WatchLaterDao
    abstract fun videoNoteDao(): VideoNoteDao
    abstract fun downloadDao(): DownloadDao
    abstract fun localPlaylistDao(): LocalPlaylistDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile private var INSTANCE: StreamNestDatabase? = null

        fun getDatabase(context: Context): StreamNestDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StreamNestDatabase::class.java,
                    "streamnest_database"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
