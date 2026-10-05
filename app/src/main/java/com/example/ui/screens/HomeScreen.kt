package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.remote.PipedStreamItem
import com.example.data.repository.StreamNestRepository
import com.example.player.PlayerManager
import com.example.ui.components.CompactShelfCard
import com.example.ui.components.MoodChipRow
import com.example.ui.components.StreamNestLogo
import com.example.ui.components.VideoCard
import com.example.ui.theme.DarkChipBg
import com.example.ui.theme.MoodChill
import com.example.ui.theme.MoodFocus
import com.example.ui.theme.MoodLearn
import com.example.ui.theme.MoodMusic
import com.example.ui.theme.MoodNews
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    repository: StreamNestRepository,
    playerManager: PlayerManager,
    onNavigateToWatch: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedMood by remember { mutableStateOf("all") }
    var isLoading by remember { mutableStateOf(true) }

    var trendingVideos by remember { mutableStateOf<List<PipedStreamItem>>(emptyList()) }
    var learnVideos by remember { mutableStateOf<List<PipedStreamItem>>(emptyList()) }
    var musicVideos by remember { mutableStateOf<List<PipedStreamItem>>(emptyList()) }
    var chillVideos by remember { mutableStateOf<List<PipedStreamItem>>(emptyList()) }
    var newsVideos by remember { mutableStateOf<List<PipedStreamItem>>(emptyList()) }
    var focusVideos by remember { mutableStateOf<List<PipedStreamItem>>(emptyList()) }

    fun loadData() {
        scope.launch {
            isLoading = true
            trendingVideos = repository.getTrending()
            learnVideos = repository.getMoodVideos("learn")
            musicVideos = repository.getMoodVideos("music")
            chillVideos = repository.getMoodVideos("chill")
            newsVideos = repository.getMoodVideos("news")
            focusVideos = repository.getMoodVideos("focus")
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StreamNestLogo(size = 32.dp)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = onNavigateToSearch,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(DarkChipBg)
                                    .testTag("home_search_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "Search",
                                    tint = Color.White
                                )
                            }

                            IconButton(
                                onClick = { loadData() },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(DarkChipBg)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Refresh",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Clickable Mood Chips: All, Learn, Music, Chill, News, Focus
                    MoodChipRow(
                        selectedMood = selectedMood,
                        onMoodSelected = { selectedMood = it }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = YouTubeRed)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Loading Mood Shelves…",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (selectedMood == "all") {
                        // 1. Featured Spotlight Video
                        trendingVideos.firstOrNull()?.let { featured ->
                            item {
                                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    VideoCard(
                                        video = featured,
                                        onClick = { onNavigateToWatch(featured.videoId) },
                                        onWatchLater = {
                                            scope.launch {
                                                repository.toggleWatchLater(
                                                    videoId = featured.videoId,
                                                    title = featured.title ?: "",
                                                    uploader = featured.uploaderName ?: "",
                                                    thumbnail = featured.thumbnail ?: "",
                                                    durationSeconds = featured.duration ?: 0L
                                                )
                                                snackbarHostState.showSnackbar("Added to Watch Later")
                                            }
                                        },
                                        onDownload = {
                                            scope.launch {
                                                repository.startDownload(
                                                    videoId = featured.videoId,
                                                    title = featured.title ?: "",
                                                    uploader = featured.uploaderName ?: "",
                                                    thumbnail = featured.thumbnail ?: "",
                                                    duration = featured.duration ?: 0L,
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

                        // 2. Learn Shelf
                        item {
                            MoodShelfSection(
                                title = "Learn",
                                subtitle = "Science, Coding & Tech",
                                tagColor = MoodLearn,
                                videos = learnVideos,
                                onVideoClick = onNavigateToWatch
                            )
                        }

                        // 3. Music Shelf
                        item {
                            MoodShelfSection(
                                title = "Music",
                                subtitle = "Lofi, Chillhop & Acoustic",
                                tagColor = MoodMusic,
                                videos = musicVideos,
                                onVideoClick = onNavigateToWatch
                            )
                        }

                        // 4. Chill Shelf
                        item {
                            MoodShelfSection(
                                title = "Chill",
                                subtitle = "Relaxing & Nature",
                                tagColor = MoodChill,
                                videos = chillVideos,
                                onVideoClick = onNavigateToWatch
                            )
                        }

                        // 5. News Shelf
                        item {
                            MoodShelfSection(
                                title = "News",
                                subtitle = "Tech & Science Updates",
                                tagColor = MoodNews,
                                videos = newsVideos,
                                onVideoClick = onNavigateToWatch
                            )
                        }

                        // 6. Focus Shelf
                        item {
                            MoodShelfSection(
                                title = "Focus",
                                subtitle = "Deep Work & Study Sounds",
                                tagColor = MoodFocus,
                                videos = focusVideos,
                                onVideoClick = onNavigateToWatch
                            )
                        }

                        // 7. Trending Streams List
                        items(trendingVideos.drop(1)) { item ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                VideoCard(
                                    video = item,
                                    onClick = { onNavigateToWatch(item.videoId) },
                                    onWatchLater = {
                                        scope.launch {
                                            repository.toggleWatchLater(
                                                videoId = item.videoId,
                                                title = item.title ?: "",
                                                uploader = item.uploaderName ?: "",
                                                thumbnail = item.thumbnail ?: "",
                                                durationSeconds = item.duration ?: 0L
                                            )
                                            snackbarHostState.showSnackbar("Added to Watch Later")
                                        }
                                    },
                                    onDownload = {
                                        scope.launch {
                                            repository.startDownload(
                                                videoId = item.videoId,
                                                title = item.title ?: "",
                                                uploader = item.uploaderName ?: "",
                                                thumbnail = item.thumbnail ?: "",
                                                duration = item.duration ?: 0L,
                                                mediaUrl = "",
                                                format = "VIDEO_720P"
                                            )
                                            snackbarHostState.showSnackbar("Download started")
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        // Filtered Mood Row View
                        val currentList = when (selectedMood) {
                            "learn" -> learnVideos
                            "music" -> musicVideos
                            "chill" -> chillVideos
                            "news" -> newsVideos
                            "focus" -> focusVideos
                            else -> trendingVideos
                        }

                        items(currentList) { item ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                VideoCard(
                                    video = item,
                                    onClick = { onNavigateToWatch(item.videoId) },
                                    onWatchLater = {
                                        scope.launch {
                                            repository.toggleWatchLater(
                                                videoId = item.videoId,
                                                title = item.title ?: "",
                                                uploader = item.uploaderName ?: "",
                                                thumbnail = item.thumbnail ?: "",
                                                durationSeconds = item.duration ?: 0L
                                            )
                                            snackbarHostState.showSnackbar("Added to Watch Later")
                                        }
                                    },
                                    onDownload = {
                                        scope.launch {
                                            repository.startDownload(
                                                videoId = item.videoId,
                                                title = item.title ?: "",
                                                uploader = item.uploaderName ?: "",
                                                thumbnail = item.thumbnail ?: "",
                                                duration = item.duration ?: 0L,
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
    }
}

@Composable
fun MoodShelfSection(
    title: String,
    subtitle: String,
    tagColor: Color,
    videos: List<PipedStreamItem>,
    onVideoClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(tagColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "• $subtitle",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondaryDark,
                    fontSize = 12.sp
                )
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(videos) { video ->
                CompactShelfCard(
                    video = video,
                    onClick = { onVideoClick(video.videoId) }
                )
            }
        }
    }
}
