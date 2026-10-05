package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.StreamNestRepository
import com.example.player.PlayerManager
import com.example.ui.theme.DarkChipBg
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.launch

@Composable
fun DownloadsScreen(
    repository: StreamNestRepository,
    playerManager: PlayerManager,
    onNavigateToWatch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val downloads by repository.allDownloads.collectAsStateWithLifecycle(initialValue = emptyList())

    val (usedBytes, freeBytes) = remember(downloads) { repository.getStorageMetrics() }
    val totalBytes = usedBytes + freeBytes
    val storageFraction = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f

    var customFolderUri by remember { mutableStateOf<String?>(null) }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            val path = uri.toString()
            customFolderUri = path
            scope.launch {
                repository.setCustomDownloadFolder(path)
                snackbarHostState.showSnackbar("Download folder updated")
            }
        }
    }

    LaunchedEffect(Unit) {
        customFolderUri = repository.getCustomDownloadFolder()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DownloadDone,
                            contentDescription = null,
                            tint = YouTubeRed
                        )
                        Text(
                            text = "Downloads",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "Manage offline video & audio files with pause and resume",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Storage Meter & Folder Selector
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.Storage, contentDescription = null, tint = YouTubeRed)
                                Text(
                                    text = "Device Storage",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            // Choose save folder button
                            OutlinedButton(
                                onClick = { folderPickerLauncher.launch(null) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Rounded.FolderOpen, null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save Folder", fontSize = 12.sp, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { storageFraction.coerceAtLeast(0.01f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = YouTubeRed,
                            trackColor = Color(0xFF383838)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "CviTube: ${formatBytes(usedBytes)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "Available: ${formatBytes(freeBytes)}",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                            )
                        }

                        if (!customFolderUri.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Folder: Custom Directory Selected",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondaryDark,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Downloaded Media (${downloads.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (downloads.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FileDownload,
                                contentDescription = null,
                                tint = TextSecondaryDark,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Downloads Stored",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap download on any video to store for offline playback without network.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                            )
                        }
                    }
                }
            } else {
                items(downloads) { dl ->
                    val progress = if (dl.fileSizeBytes > 0) {
                        (dl.downloadedBytes.toFloat() / dl.fileSizeBytes.toFloat()).coerceIn(0f, 1f)
                    } else if (dl.status == "COMPLETED") 1f else 0.2f

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                // Play offline stream
                                playerManager.playStream(
                                    videoId = dl.videoId,
                                    title = dl.title,
                                    uploader = dl.uploaderName,
                                    uploaderAvatar = dl.thumbnailUrl,
                                    thumbnail = dl.thumbnailUrl,
                                    mediaUrl = if (dl.localFilePath.isNotEmpty()) "file://${dl.localFilePath}" else "",
                                    isAudioOnly = dl.format == "AUDIO_ONLY"
                                )
                                onNavigateToWatch(dl.videoId)
                            }
                            .testTag("download_item_${dl.videoId}"),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkChipBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (dl.format == "AUDIO_ONLY") {
                                        Icon(Icons.Rounded.Headphones, null, tint = Color.White)
                                    } else {
                                        Icon(Icons.Rounded.Videocam, null, tint = YouTubeRed)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dl.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = dl.uploaderName,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSecondaryDark,
                                                fontSize = 11.sp
                                            )
                                        )
                                        Text(text = "•", color = TextSecondaryDark)
                                        Text(
                                            text = dl.format.replace('_', ' '),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        if (dl.fileSizeBytes > 0) {
                                            Text(text = "•", color = TextSecondaryDark)
                                            Text(
                                                text = formatBytes(dl.downloadedBytes),
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = TextSecondaryDark,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }

                                // Pause / Resume Controls
                                if (dl.status == "DOWNLOADING") {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                repository.pauseDownload(dl.videoId)
                                                snackbarHostState.showSnackbar("Download paused")
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Rounded.Pause, "Pause", tint = YouTubeRed)
                                    }
                                } else if (dl.status == "PAUSED") {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                repository.resolveAndResumeDownload(dl.videoId)
                                                snackbarHostState.showSnackbar("Resuming download...")
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Rounded.PlayArrow, "Resume", tint = Color.White)
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            repository.deleteDownload(dl.videoId)
                                            snackbarHostState.showSnackbar("Deleted download")
                                        }
                                    }
                                ) {
                                    Icon(Icons.Rounded.DeleteOutline, "Delete", tint = TextSecondaryDark)
                                }
                            }

                            // Progress Indicator if downloading or paused
                            if (dl.status == "DOWNLOADING" || dl.status == "PAUSED") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(4.dp)
                                            .clip(CircleShape),
                                        color = if (dl.status == "PAUSED") Color.Gray else YouTubeRed,
                                        trackColor = Color(0xFF383838)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "${(progress * 100).toInt()}% • ${dl.status}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (dl.status == "PAUSED") Color.Gray else YouTubeRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    return String.format(java.util.Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
