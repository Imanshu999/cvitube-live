package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.remote.PipedStreamItem
import com.example.data.repository.StreamNestRepository
import com.example.ui.components.VideoCard
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.launch

@Composable
fun SubscriptionsScreen(
    repository: StreamNestRepository,
    onNavigateToWatch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val subscriptions by repository.allSubscriptions.collectAsStateWithLifecycle(initialValue = emptyList())

    var feedVideos by remember { mutableStateOf<List<PipedStreamItem>>(emptyList()) }
    var isLoadingFeed by remember { mutableStateOf(false) }

    LaunchedEffect(subscriptions) {
        isLoadingFeed = true
        feedVideos = repository.getSubscriptionFeed()
        isLoadingFeed = false
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
                            imageVector = Icons.Rounded.Subscriptions,
                            contentDescription = null,
                            tint = YouTubeRed
                        )
                        Text(
                            text = "Subscriptions",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "Chronological feed • No recommendation algorithms",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
            // Channel avatars row with Mute toggles
            if (subscriptions.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Channels (${subscriptions.size})",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(subscriptions) { sub ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            scope.launch {
                                                val newMuted = !sub.isMuted
                                                repository.setChannelMuted(sub.channelId, newMuted)
                                                snackbarHostState.showSnackbar(
                                                    if (newMuted) "Muted ${sub.channelName}" else "Unmuted ${sub.channelName}"
                                                )
                                            }
                                        }
                                        .padding(4.dp)
                                        .testTag("sub_item_${sub.channelId}")
                                ) {
                                    Box {
                                        AsyncImage(
                                            model = sub.channelAvatar,
                                            contentDescription = sub.channelName,
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    2.dp,
                                                    if (sub.isMuted) Color.Gray else YouTubeRed,
                                                    CircleShape
                                                )
                                        )

                                        // Mute status badge
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(if (sub.isMuted) Color.DarkGray else YouTubeRed),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (sub.isMuted) Icons.Rounded.NotificationsOff else Icons.Rounded.Notifications,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = sub.channelName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            color = if (sub.isMuted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Subscriptions,
                                contentDescription = null,
                                tint = YouTubeRed,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Subscriptions Yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Subscribe to channels on any watch page to build your strictly chronological feed.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            // Feed Items
            item {
                Text(
                    text = "Recent Releases",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (isLoadingFeed) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = YouTubeRed)
                    }
                }
            } else if (feedVideos.isEmpty()) {
                item {
                    Text(
                        text = "No recent videos from active subscriptions.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                items(feedVideos) { video ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        VideoCard(
                            video = video,
                            onClick = { onNavigateToWatch(video.videoId) },
                            onWatchLater = {
                                scope.launch {
                                    repository.toggleWatchLater(
                                        videoId = video.videoId,
                                        title = video.title ?: "",
                                        uploader = video.uploaderName ?: "",
                                        thumbnail = video.thumbnail ?: "",
                                        durationSeconds = video.duration ?: 0L
                                    )
                                    snackbarHostState.showSnackbar("Added to Watch Later queue")
                                }
                            },
                            onDownload = {
                                scope.launch {
                                    repository.startDownload(
                                        videoId = video.videoId,
                                        title = video.title ?: "",
                                        uploader = video.uploaderName ?: "",
                                        thumbnail = video.thumbnail ?: "",
                                        duration = video.duration ?: 0L,
                                        mediaUrl = "",
                                        format = "VIDEO_720P"
                                    )
                                    snackbarHostState.showSnackbar("Download started")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
