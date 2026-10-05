package com.example.ui.screens

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.data.repository.StreamNestRepository
import com.example.player.PlayerManager

@OptIn(UnstableApi::class)
@Composable
fun WatchScreen(
    videoId: String,
    repository: StreamNestRepository,
    playerManager: PlayerManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerState by playerManager.uiState.collectAsStateWithLifecycle()
    var title by remember(videoId) { mutableStateOf("Loading video…") }
    var uploader by remember(videoId) { mutableStateOf("") }
    var error by remember(videoId) { mutableStateOf<String?>(null) }

    LaunchedEffect(videoId) {
        error = null
        try {
            val details = repository.getStreamDetails(videoId)
            title = details.title ?: "CviTube"
            uploader = details.uploader ?: ""
            val candidates = buildList {
                details.hls?.takeIf { it.isNotBlank() }?.let(::add)
                details.videoStreams.filter { !it.videoOnly && it.url.isNotBlank() }.forEach { add(it.url) }
                details.videoStreams.filter { it.url.isNotBlank() }.forEach { add(it.url) }
                details.audioStreams.filter { it.url.isNotBlank() }.forEach { add(it.url) }
            }.distinct()

            val streamUrl = candidates.firstOrNull()
            if (streamUrl == null) {
                error = "No live stream is available for this video."
            } else {
                playerManager.playStream(
                    videoId = videoId,
                    title = title,
                    uploader = uploader,
                    uploaderAvatar = "",
                    thumbnail = "",
                    mediaUrl = streamUrl,
                    fallbackUrls = emptyList()
                )
            }
        } catch (t: Throwable) {
            error = t.message ?: "Unable to load this video."
        }
    }

    Column(
        modifier = modifier.fillMaxSize().background(Color.Black)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                if (uploader.isNotBlank()) {
                    Text(uploader, color = Color.LightGray, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { context -> PlayerView(context).apply { player = playerManager.exoPlayer } },
                modifier = Modifier.fillMaxSize()
            )
            if (error != null) {
                Text(error!!, color = Color.White, modifier = Modifier.padding(24.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { playerManager.togglePlayPause() }) {
                Icon(
                    if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
