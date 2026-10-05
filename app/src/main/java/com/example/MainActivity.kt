package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FloatingDockMobile
import com.example.ui.components.MiniPlayer
import com.example.ui.components.SlimIconRailDesktop
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SubscriptionsScreen
import com.example.ui.screens.WatchScreen
import com.example.ui.theme.StreamNestTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as StreamNestApp
        val repository = app.repository
        val playerManager = app.playerManager

        setContent {
            StreamNestTheme {
                StreamNestAppRoot(
                    repository = repository,
                    playerManager = playerManager
                )
            }
        }
    }
}

@Composable
fun StreamNestAppRoot(
    repository: com.example.data.repository.StreamNestRepository,
    playerManager: com.example.player.PlayerManager
) {
    var currentTab by remember { mutableStateOf("home") }
    var activeWatchVideoId by remember { mutableStateOf<String?>(null) }
    var isSearchActive by remember { mutableStateOf(false) }

    val playerState by playerManager.uiState.collectAsStateWithLifecycle()

    // Handle back button
    BackHandler(enabled = activeWatchVideoId != null || isSearchActive || currentTab != "home") {
        when {
            activeWatchVideoId != null -> {
                // Minimize watch screen to mini player
                activeWatchVideoId = null
            }
            isSearchActive -> {
                isSearchActive = false
            }
            currentTab != "home" -> {
                currentTab = "home"
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isExpandedScreen = maxWidth >= 600.dp

        if (activeWatchVideoId != null) {
            // Fullscreen Watch Screen
            WatchScreen(
                videoId = activeWatchVideoId!!,
                repository = repository,
                playerManager = playerManager,
                onBack = { activeWatchVideoId = null }
            )
        } else if (isSearchActive) {
            // Search Screen
            SearchScreen(
                repository = repository,
                onNavigateToWatch = { videoId ->
                    isSearchActive = false
                    activeWatchVideoId = videoId
                },
                onBack = { isSearchActive = false }
            )
        } else {
            // Main App with Navigation (Mobile Dock or Desktop Rail)
            Row(modifier = Modifier.fillMaxSize()) {
                if (isExpandedScreen) {
                    // Desktop / Tablet Slim Rail
                    SlimIconRailDesktop(
                        currentRoute = currentTab,
                        onNavigate = { currentTab = it }
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    // Screen Content
                    when (currentTab) {
                        "home" -> HomeScreen(
                            repository = repository,
                            playerManager = playerManager,
                            onNavigateToWatch = { activeWatchVideoId = it },
                            onNavigateToSearch = { isSearchActive = true }
                        )
                        "subscriptions" -> SubscriptionsScreen(
                            repository = repository,
                            onNavigateToWatch = { activeWatchVideoId = it }
                        )
                        "library" -> LibraryScreen(
                            repository = repository,
                            onNavigateToWatch = { activeWatchVideoId = it }
                        )
                        "downloads" -> DownloadsScreen(
                            repository = repository,
                            playerManager = playerManager,
                            onNavigateToWatch = { activeWatchVideoId = it }
                        )
                    }

                    // Floating Mini-Player & Bottom Floating Dock on Mobile
                    if (!isExpandedScreen) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                            ) {
                                // MiniPlayer if active
                                if (playerState.isMiniPlayerVisible && playerState.videoId.isNotEmpty()) {
                                    MiniPlayer(
                                        state = playerState,
                                        onExpand = { activeWatchVideoId = playerState.videoId },
                                        onTogglePlayPause = { playerManager.togglePlayPause() },
                                        onClose = { playerManager.closeMiniPlayer() }
                                    )
                                }

                                // Mobile Floating Dock
                                FloatingDockMobile(
                                    currentRoute = currentTab,
                                    onNavigate = { currentTab = it }
                                )
                            }
                        }
                    } else {
                        // Desktop floating mini player at bottom corner
                        if (playerState.isMiniPlayerVisible && playerState.videoId.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(24.dp)
                                    .width(360.dp)
                            ) {
                                MiniPlayer(
                                    state = playerState,
                                    onExpand = { activeWatchVideoId = playerState.videoId },
                                    onTogglePlayPause = { playerManager.togglePlayPause() },
                                    onClose = { playerManager.closeMiniPlayer() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
