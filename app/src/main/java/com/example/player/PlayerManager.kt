package com.example.player

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.data.remote.PipedChapter
import com.example.data.remote.PipedSponsorSegment
import com.example.data.repository.SponsorBlockConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
    val videoId: String = "",
    val title: String = "",
    val uploaderName: String = "",
    val uploaderAvatar: String = "",
    val thumbnailUrl: String = "",
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val isAudioOnly: Boolean = false,
    val isMiniPlayerVisible: Boolean = false,
    val isLoopActive: Boolean = false,
    val loopPointA: Long? = null,
    val loopPointB: Long? = null,
    val sleepTimerMinutesRemaining: Int? = null,
    val equalizerPreset: String = "Balanced",
    val focusMode: Boolean = false,
    val chapters: List<PipedChapter> = emptyList(),
    val sponsorSegments: List<PipedSponsorSegment> = emptyList()
)

@OptIn(UnstableApi::class)
class PlayerManager(private val context: Context) {

    private val pendingFallbacks = mutableListOf<String>()
    private var currentActiveUrl: String = ""

    val exoPlayer: ExoPlayer by lazy {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("com.google.android.youtube/19.29.37 (Linux; U; Android 14; US) gzip")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(25000)

        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(dataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build().apply {
                repeatMode = Player.REPEAT_MODE_OFF
                addListener(playerListener)
            }
    }

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var tickerJob: Job? = null
    private var sleepTimerJob: Job? = null

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    private var sponsorConfig = SponsorBlockConfig()

    fun updateSponsorConfig(config: SponsorBlockConfig) {
        sponsorConfig = config
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
            if (isPlaying) {
                startTicker()
            } else {
                tickerJob?.cancel()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val duration = exoPlayer.duration.coerceAtLeast(0L)
            _uiState.value = _uiState.value.copy(
                durationMs = duration,
                bufferedPositionMs = exoPlayer.bufferedPosition.coerceAtLeast(0L)
            )
            if (playbackState == Player.STATE_ENDED) {
                if (_uiState.value.isLoopActive && _uiState.value.loopPointA != null) {
                    exoPlayer.seekTo(_uiState.value.loopPointA!!)
                    exoPlayer.play()
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e("PlayerManager", "Playback error: " + error.message, error)
            val nextUrl = pendingFallbacks.firstOrNull()
            if (!nextUrl.isNullOrBlank()) {
                pendingFallbacks.removeAt(0)
                currentActiveUrl = nextUrl
                try {
                    exoPlayer.setMediaItem(MediaItem.fromUri(nextUrl))
                    exoPlayer.prepare()
                    exoPlayer.play()
                } catch (e: Exception) {
                    Log.e("PlayerManager", "Fallback playback failed", e)
                }
            } else {
                emitMessage("This video stream could not be played. Try refreshing or another video.")
            }
        }
    }


    fun playStream(
        videoId: String,
        title: String,
        uploader: String,
        uploaderAvatar: String,
        thumbnail: String,
        mediaUrl: String,
        fallbackUrls: List<String> = emptyList(),
        isAudioOnly: Boolean = false,
        chapters: List<PipedChapter> = emptyList(),
        sponsorSegments: List<PipedSponsorSegment> = emptyList(),
        initialPositionMs: Long = 0L
    ) {
        pendingFallbacks.clear()
        pendingFallbacks.addAll(fallbackUrls.filter { it != mediaUrl && it.isNotBlank() })
        currentActiveUrl = mediaUrl

        _uiState.value = _uiState.value.copy(
            videoId = videoId,
            title = title,
            uploaderName = uploader,
            uploaderAvatar = uploaderAvatar,
            thumbnailUrl = thumbnail,
            isAudioOnly = isAudioOnly,
            chapters = chapters,
            sponsorSegments = sponsorSegments,
            isMiniPlayerVisible = true,
            loopPointA = null,
            loopPointB = null,
            isLoopActive = false
        )

        try {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            val mediaItem = MediaItem.fromUri(mediaUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            if (initialPositionMs > 0) {
                exoPlayer.seekTo(initialPositionMs)
            }
            exoPlayer.play()
            startTicker()
        } catch (e: Exception) {
            Log.e("PlayerManager", "Initial live stream error", e)
            emitMessage("Unable to start this live stream.")
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs.coerceIn(0L, exoPlayer.duration.coerceAtLeast(0L)))
    }

    fun seekRelative(deltaSeconds: Int) {
        val target = (exoPlayer.currentPosition + deltaSeconds * 1000L).coerceIn(0L, exoPlayer.duration.coerceAtLeast(0L))
        exoPlayer.seekTo(target)
    }

    fun setSpeed(speed: Float) {
        exoPlayer.playbackParameters = PlaybackParameters(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun setAudioOnlyMode(audioOnly: Boolean) {
        _uiState.value = _uiState.value.copy(isAudioOnly = audioOnly)
    }

    fun toggleFocusMode() {
        _uiState.value = _uiState.value.copy(focusMode = !_uiState.value.focusMode)
    }

    // A-B Loop Controls
    fun setLoopPointA() {
        val current = exoPlayer.currentPosition
        _uiState.value = _uiState.value.copy(loopPointA = current, isLoopActive = false)
        emitMessage("Loop Point A set at ${formatTime(current)}")
    }

    fun setLoopPointB() {
        val current = exoPlayer.currentPosition
        val a = _uiState.value.loopPointA
        if (a == null || current <= a) {
            emitMessage("Point B must be after Point A")
            return
        }
        _uiState.value = _uiState.value.copy(loopPointB = current, isLoopActive = true)
        emitMessage("A-B Loop enabled (${formatTime(a)} - ${formatTime(current)})")
    }

    fun clearLoop() {
        _uiState.value = _uiState.value.copy(loopPointA = null, loopPointB = null, isLoopActive = false)
        emitMessage("A-B Loop cleared")
    }

    // Sleep Timer
    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _uiState.value = _uiState.value.copy(sleepTimerMinutesRemaining = null)
            emitMessage("Sleep timer turned off")
            return
        }

        _uiState.value = _uiState.value.copy(sleepTimerMinutesRemaining = minutes)
        emitMessage("Sleep timer set for $minutes minutes")

        sleepTimerJob = scope.launch {
            var rem = minutes
            while (rem > 0 && isActive) {
                delay(60_000L)
                rem -= 1
                _uiState.value = _uiState.value.copy(sleepTimerMinutesRemaining = if (rem > 0) rem else null)
            }
            exoPlayer.pause()
            emitMessage("Sleep timer ended. Goodnight!")
        }
    }

    // Equalizer Presets
    fun setEqualizerPreset(preset: String) {
        _uiState.value = _uiState.value.copy(equalizerPreset = preset)
        emitMessage("Audio preset: $preset")
    }

    fun closeMiniPlayer() {
        exoPlayer.stop()
        tickerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isMiniPlayerVisible = false,
            videoId = "",
            isPlaying = false
        )
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                val current = exoPlayer.currentPosition.coerceAtLeast(0L)
                val duration = exoPlayer.duration.coerceAtLeast(0L)
                val buffered = exoPlayer.bufferedPosition.coerceAtLeast(0L)

                _uiState.value = _uiState.value.copy(
                    currentPositionMs = current,
                    durationMs = duration,
                    bufferedPositionMs = buffered
                )

                // 1. SponsorBlock check
                checkSponsorSegments(current)

                // 2. A-B Loop check
                checkAbLoop(current)

                delay(250L)
            }
        }
    }

    private fun checkSponsorSegments(currentMs: Long) {
        val currentSec = currentMs / 1000.0
        val segments = _uiState.value.sponsorSegments
        for (seg in segments) {
            if (sponsorConfig.shouldSkip(seg.category)) {
                if (currentSec >= seg.startSeconds && currentSec < seg.endSeconds - 0.5) {
                    val skipToMs = (seg.endSeconds * 1000L).toLong()
                    exoPlayer.seekTo(skipToMs)
                    val saved = (seg.endSeconds - seg.startSeconds).toInt()
                    emitMessage("⚡ Skipped ${seg.category.replaceFirstChar { it.uppercase() }} segment (${saved}s saved)")
                    break
                }
            }
        }
    }

    private fun checkAbLoop(currentMs: Long) {
        val state = _uiState.value
        if (state.isLoopActive && state.loopPointA != null && state.loopPointB != null) {
            if (currentMs >= state.loopPointB) {
                exoPlayer.seekTo(state.loopPointA)
            }
        }
    }

    private fun emitMessage(msg: String) {
        scope.launch {
            _events.emit(msg)
        }
    }

    fun release() {
        tickerJob?.cancel()
        sleepTimerJob?.cancel()
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
    }

    companion object {
        fun formatTime(ms: Long): String {
            val totalSeconds = (ms / 1000).coerceAtLeast(0)
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format(java.util.Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
            }
        }
    }
}
