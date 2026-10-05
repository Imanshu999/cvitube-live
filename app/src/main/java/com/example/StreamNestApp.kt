package com.example

import android.app.Application
import com.example.data.local.AppDatabaseHolder
import com.example.data.repository.StreamNestRepository
import com.example.player.PlayerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StreamNestApp : Application() {
    lateinit var repository: StreamNestRepository
        private set

    lateinit var playerManager: PlayerManager
        private set

    override fun onCreate() {
        super.onCreate()
        AppDatabaseHolder.get(this)
        repository = StreamNestRepository(this)
        playerManager = PlayerManager(this)

        CoroutineScope(Dispatchers.IO).launch {
            repository.initSettings()
            val sponsorConfig = repository.getSponsorBlockConfig()
            playerManager.updateSponsorConfig(sponsorConfig)
        }
    }
}
