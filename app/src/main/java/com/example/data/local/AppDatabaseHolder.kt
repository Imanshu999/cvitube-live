package com.example.data.local

import android.content.Context

object AppDatabaseHolder {
    @Volatile
    private var instance: StreamNestDatabase? = null

    fun get(context: Context): StreamNestDatabase {
        return instance ?: synchronized(this) {
            instance ?: StreamNestDatabase.getDatabase(context).also { instance = it }
        }
    }
}
