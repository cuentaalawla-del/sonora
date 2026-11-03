package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.example.data.model.RepeatMode
import com.example.data.model.Track
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

class PlaybackManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: PlaybackManager? = null

        fun getInstance(context: Context): PlaybackManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PlaybackManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    val youTubeEngine = YouTubeAudioEngine.getInstance(context)

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _activeYouTubeId = MutableStateFlow<String?>(null)
    val activeYouTubeId: StateFlow<String?> = _activeYouTubeId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0)
    val currentPositionMs: StateFlow<Int> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0)
    val durationMs: StateFlow<Int> = _durationMs.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(0)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    var onTrackPlayedListener: ((Track) -> Unit)? = null

    init {
        // Sync states from YouTubeAudioEngine for YouTube / streaming playback
        scope.launch {
            youTubeEngine.isPlaying.collect { playing ->
                val track = _currentTrack.value
                if (track != null && !track.isLocal) {
                    _isPlaying.value = playing
                }
            }
        }
        scope.launch {
            youTubeEngine.isBuffering.collect { buffering ->
                val track = _currentTrack.value
                if (track != null && !track.isLocal) {
                    _isBuffering.value = buffering
                }
            }
        }
        scope.launch {
            youTubeEngine.currentPositionMs.collect { pos ->
                val track = _currentTrack.value
                if (track != null && !track.isLocal && pos > 0) {
                    _currentPositionMs.value = pos
                }
            }
        }
        scope.launch {
            youTubeEngine.durationMs.collect { dur ->
                val track = _currentTrack.value
                if (track != null && !track.isLocal && dur > 0) {
                    _durationMs.value = dur
                }
            }
        }
        youTubeEngine.onErrorListener = { _ ->
            val track = _currentTrack.value
            if (track != null && !track.isLocal) {
                // Video can't be played (not embeddable / removed): skip to next if there is one.
                _isBuffering.value = false
                _isPlaying.value = false
                if (_queue.value.size > 1) next()
            }
        }
        youTubeEngine.onCompletionListener = {
            val track = _currentTrack.value
            if (track != null && !track.isLocal) {
                handleTrackCompletion()
            }
        }
    }

    fun setInitialTrack(track: Track, newQueue: List<Track> = listOf(track)) {
        if (_currentTrack.value == null) {
            _currentTrack.value = track
            _activeYouTubeId.value = track.youtubeId
            _queue.value = if (newQueue.isEmpty()) listOf(track) else newQueue
            _queueIndex.value = newQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
            _durationMs.value = track.durationSeconds * 1000
            _isPlaying.value = false
            _isBuffering.value = false
            _currentPositionMs.value = 0
        }
    }

    fun playTrack(track: Track, newQueue: List<Track> = listOf(track)) {
        val index = newQueue.indexOfFirst { it.id == track.id }.takeIf { it >= 0 } ?: 0
        _queue.value = if (newQueue.isEmpty()) listOf(track) else newQueue
        _queueIndex.value = index
        startTrackInternal(track)
    }

    fun playFromQueue(index: Int) {
        val q = _queue.value
        if (index in q.indices) {
            _queueIndex.value = index
            startTrackInternal(q[index])
        }
    }

    private fun startTrackInternal(track: Track) {
        _currentTrack.value = track
        _activeYouTubeId.value = track.youtubeId
        _isBuffering.value = true
        _currentPositionMs.value = 0
        _durationMs.value = track.durationSeconds * 1000
        _isPlaying.value = true

        onTrackPlayedListener?.invoke(track)

        // Safety timeout so the buffering spinner never stays stuck; reflect the real engine state.
        val startedTrackId = track.id
        scope.launch {
            delay(8000)
            if (_currentTrack.value?.id == startedTrackId && _isBuffering.value) {
                _isBuffering.value = false
                if (!track.isLocal) _isPlaying.value = youTubeEngine.isPlaying.value
            }
        }

        if (track.isLocal) {
            // Local MP3 playback via MediaPlayer
            youTubeEngine.stop()
            releasePlayer()
            playLocalWithMediaPlayer(track)
        } else {
            // YouTube Official Engine Playback
            releasePlayer()
            val videoId = track.youtubeId?.takeIf { it.isNotBlank() } ?: extractYouTubeId(track.streamUrl) ?: "1fI48iN0X28"
            _activeYouTubeId.value = videoId
            youTubeEngine.play(videoId)
        }
    }

    private fun playLocalWithMediaPlayer(track: Track) {
        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, Uri.parse(track.streamUrl))
                setVolume(_volume.value, _volume.value)
                setOnPreparedListener { mp ->
                    _isBuffering.value = false
                    val dur = mp.duration
                    if (dur > 0) {
                        _durationMs.value = dur
                    }
                    mp.start()
                    _isPlaying.value = true
                    startLocalProgressTracker()
                }
                setOnCompletionListener {
                    handleTrackCompletion()
                }
                setOnErrorListener { _, _, _ ->
                    _isBuffering.value = false
                    _isPlaying.value = true
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            e.printStackTrace()
            _isBuffering.value = false
            _isPlaying.value = true
        }
    }

    fun togglePlayPause() {
        val current = _currentTrack.value ?: return

        if (current.isLocal) {
            val player = mediaPlayer ?: run {
                startTrackInternal(current)
                return
            }
            if (player.isPlaying) {
                try { player.pause() } catch (e: Exception) {}
                _isPlaying.value = false
                stopProgressTracker()
            } else {
                try { player.start() } catch (e: Exception) {}
                _isPlaying.value = true
                startLocalProgressTracker()
            }
        } else {
            // YouTube playback
            if (_isPlaying.value) {
                youTubeEngine.pause()
                _isPlaying.value = false
            } else {
                youTubeEngine.resume()
                _isPlaying.value = true
            }
        }
    }

    fun seekTo(positionMs: Int) {
        val current = _currentTrack.value
        _currentPositionMs.value = positionMs
        if (current != null && current.isLocal) {
            mediaPlayer?.let { player ->
                try { player.seekTo(positionMs) } catch (e: Exception) {}
            }
        } else {
            youTubeEngine.seekTo(positionMs)
        }
    }

    fun next() {
        val q = _queue.value
        if (q.isEmpty()) return

        val nextIndex = if (_isShuffle.value) {
            if (q.size > 1) {
                var rand = (q.indices).random()
                while (rand == _queueIndex.value) {
                    rand = (q.indices).random()
                }
                rand
            } else 0
        } else {
            (_queueIndex.value + 1) % q.size
        }

        _queueIndex.value = nextIndex
        startTrackInternal(q[nextIndex])
    }

    fun previous() {
        if (_currentPositionMs.value > 3000) {
            seekTo(0)
            return
        }

        val q = _queue.value
        if (q.isEmpty()) return

        val prevIndex = if (_queueIndex.value > 0) {
            _queueIndex.value - 1
        } else {
            q.size - 1
        }

        _queueIndex.value = prevIndex
        startTrackInternal(q[prevIndex])
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0.0f, 1.0f)
        _volume.value = clamped
        try {
            mediaPlayer?.setVolume(clamped, clamped)
        } catch (e: Exception) {}
        youTubeEngine.setVolume(clamped)
    }

    fun addToQueue(track: Track) {
        val currentList = _queue.value.toMutableList()
        currentList.add(track)
        _queue.value = currentList
    }

    fun playNext(track: Track) {
        val currentList = _queue.value.toMutableList()
        val insertIndex = (_queueIndex.value + 1).coerceAtMost(currentList.size)
        currentList.add(insertIndex, track)
        _queue.value = currentList
    }

    fun removeFromQueue(index: Int) {
        val currentList = _queue.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _queue.value = currentList
            if (index < _queueIndex.value) {
                _queueIndex.value = (_queueIndex.value - 1).coerceAtLeast(0)
            } else if (index == _queueIndex.value && currentList.isNotEmpty()) {
                val newIndex = index.coerceAtMost(currentList.size - 1)
                _queueIndex.value = newIndex
                startTrackInternal(currentList[newIndex])
            }
        }
    }

    fun clearQueue() {
        val curr = _currentTrack.value
        if (curr != null) {
            _queue.value = listOf(curr)
            _queueIndex.value = 0
        } else {
            _queue.value = emptyList()
            _queueIndex.value = 0
        }
    }

    private fun handleTrackCompletion() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                seekTo(0)
                _currentTrack.value?.let { startTrackInternal(it) }
            }
            RepeatMode.ALL -> {
                next()
            }
            RepeatMode.OFF -> {
                val q = _queue.value
                if (_queueIndex.value < q.size - 1) {
                    next()
                } else {
                    _isPlaying.value = false
                    _currentPositionMs.value = 0
                    stopProgressTracker()
                }
            }
        }
    }

    private fun startLocalProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            while (isActive) {
                try {
                    mediaPlayer?.let { player ->
                        if (player.isPlaying) {
                            _currentPositionMs.value = player.currentPosition
                        }
                    }
                } catch (e: Exception) {}
                delay(300)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun releasePlayer() {
        stopProgressTracker()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {}
        mediaPlayer = null
    }

    private fun extractYouTubeId(url: String): String? {
        val pattern = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=)[^#&?\\n]*"
        val compiledPattern = java.util.regex.Pattern.compile(pattern)
        val matcher = compiledPattern.matcher(url)
        return if (matcher.find()) matcher.group() else null
    }
}
