package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.MusicRepository
import com.example.player.PlaybackManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    SEARCH,
    LIBRARY,
    PREMIUM,
    PLAYLIST_DETAIL,
    ARTIST_DETAIL,
    ALBUM_DETAIL
}

enum class SearchTab {
    ALL,
    TRACKS,
    ARTISTS,
    ALBUMS,
    YOUTUBE
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)
    val playbackManager = PlaybackManager.getInstance(application)

    // Navigation Stack
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val screenHistory = mutableListOf<Screen>()

    // Selected items for detail screens
    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    private val _selectedArtist = MutableStateFlow<Artist?>(null)
    val selectedArtist: StateFlow<Artist?> = _selectedArtist.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum: StateFlow<Album?> = _selectedAlbum.asStateFlow()

    // Playlist tracks for current detail
    private val _playlistTracks = MutableStateFlow<List<Track>>(emptyList())
    val playlistTracks: StateFlow<List<Track>> = _playlistTracks.asStateFlow()

    // UI Dialog States
    private val _isFullScreenPlayerVisible = MutableStateFlow(false)
    val isFullScreenPlayerVisible: StateFlow<Boolean> = _isFullScreenPlayerVisible.asStateFlow()

    private val _isQueueDialogVisible = MutableStateFlow(false)
    val isQueueDialogVisible: StateFlow<Boolean> = _isQueueDialogVisible.asStateFlow()

    private val _trackToAddToPlaylist = MutableStateFlow<Track?>(null)
    val trackToAddToPlaylist: StateFlow<Track?> = _trackToAddToPlaylist.asStateFlow()

    private val _showCreatePlaylistDialog = MutableStateFlow(false)
    val showCreatePlaylistDialog: StateFlow<Boolean> = _showCreatePlaylistDialog.asStateFlow()

    private val _smartoolsTargetTrack = MutableStateFlow<Track?>(null)
    val smartoolsTargetTrack: StateFlow<Track?> = _smartoolsTargetTrack.asStateFlow()

    private val _smartoolsTargetYouTube = MutableStateFlow<YouTubeSearchResult?>(null)
    val smartoolsTargetYouTube: StateFlow<YouTubeSearchResult?> = _smartoolsTargetYouTube.asStateFlow()

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchTab = MutableStateFlow(SearchTab.ALL)
    val searchTab: StateFlow<SearchTab> = _searchTab.asStateFlow()

    private val _youTubeResults = MutableStateFlow<List<YouTubeSearchResult>>(emptyList())
    val youTubeResults: StateFlow<List<YouTubeSearchResult>> = _youTubeResults.asStateFlow()

    private val _isSearchingYouTube = MutableStateFlow(false)
    val isSearchingYouTube: StateFlow<Boolean> = _isSearchingYouTube.asStateFlow()

    // User profile
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Curated catalog flows
    val curatedPlaylists = repository.curatedPlaylists
    val popularAlbums = repository.popularAlbums
    val featuredArtists = repository.featuredArtists
    val genres = repository.genres
    val allCatalogTracks = repository.allCatalogTracks

    // Persistent database flows
    val userPlaylists = repository.userPlaylistsFlow
    val favoriteTracks = repository.favoriteTracksFlow
    val historyTracks = repository.historyTracksFlow

    init {
        // Connect history listener so played tracks are recorded
        playbackManager.onTrackPlayedListener = { track ->
            viewModelScope.launch {
                repository.addToHistory(track)
            }
        }

        // Initialize default track so bottom player is ready without auto-playing
        if (playbackManager.currentTrack.value == null && allCatalogTracks.isNotEmpty()) {
            val first = allCatalogTracks.first()
            playbackManager.setInitialTrack(first, allCatalogTracks)
        }

        // Real-time live YouTube search listener
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .collect { q ->
                    if (q.isNotBlank()) {
                        _isSearchingYouTube.value = true
                        try {
                            _youTubeResults.value = repository.searchYouTube(q)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            _isSearchingYouTube.value = false
                        }
                    } else {
                        _youTubeResults.value = repository.searchYouTube("")
                    }
                }
        }
    }

    fun triggerExplicitYouTubeSearch(query: String) {
        val clean = query.trim()
        if (clean.isNotBlank()) {
            viewModelScope.launch {
                _isSearchingYouTube.value = true
                try {
                    _youTubeResults.value = repository.searchYouTube(clean)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _isSearchingYouTube.value = false
                }
            }
        }
    }

    // Navigation
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun handleBack(): Boolean {
        if (_isFullScreenPlayerVisible.value) {
            _isFullScreenPlayerVisible.value = false
            return true
        }
        if (_isQueueDialogVisible.value) {
            _isQueueDialogVisible.value = false
            return true
        }
        if (_trackToAddToPlaylist.value != null) {
            _trackToAddToPlaylist.value = null
            return true
        }
        if (_smartoolsTargetTrack.value != null || _smartoolsTargetYouTube.value != null) {
            _smartoolsTargetTrack.value = null
            _smartoolsTargetYouTube.value = null
            return true
        }
        if (screenHistory.isNotEmpty()) {
            val prev = screenHistory.removeAt(screenHistory.size - 1)
            _currentScreen.value = prev
            return true
        }
        return false
    }

    fun openPlaylist(playlist: Playlist) {
        _selectedPlaylist.value = playlist
        viewModelScope.launch {
            repository.getTracksForPlaylist(playlist.id).collect { tracks ->
                _playlistTracks.value = tracks
            }
        }
        navigateTo(Screen.PLAYLIST_DETAIL)
    }

    fun openArtist(artist: Artist) {
        _selectedArtist.value = artist
        navigateTo(Screen.ARTIST_DETAIL)
    }

    fun openAlbum(album: Album) {
        _selectedAlbum.value = album
        navigateTo(Screen.ALBUM_DETAIL)
    }

    // Full Screen Player
    fun openFullScreenPlayer() {
        _isFullScreenPlayerVisible.value = true
    }

    fun closeFullScreenPlayer() {
        _isFullScreenPlayerVisible.value = false
    }

    fun toggleQueueDialog() {
        _isQueueDialogVisible.value = !_isQueueDialogVisible.value
    }

    // Search
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSearchTabSelected(tab: SearchTab) {
        _searchTab.value = tab
    }

    // Playback Helpers
    fun playTrack(track: Track, queue: List<Track> = listOf(track)) {
        playbackManager.playTrack(track, queue)
    }

    fun playPlaylist(playlist: Playlist, shuffle: Boolean = false) {
        viewModelScope.launch {
            val tracks = if (playlist.tracks.isNotEmpty()) {
                playlist.tracks
            } else {
                repository.allCatalogTracks
            }
            if (tracks.isNotEmpty()) {
                val list = if (shuffle) tracks.shuffled() else tracks
                playbackManager.playTrack(list.first(), list)
            }
        }
    }

    // Favorites
    fun isFavorite(trackId: String): Flow<Boolean> = repository.isFavorite(trackId)

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            val added = repository.toggleFavorite(track)
            val msg = if (added) "Añadida a Tus me gusta" else "Eliminada de Tus me gusta"
            Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Playlist CRUD
    fun showAddToPlaylistDialog(track: Track) {
        _trackToAddToPlaylist.value = track
    }

    fun dismissAddToPlaylistDialog() {
        _trackToAddToPlaylist.value = null
    }

    fun addTrackToPlaylist(playlistId: String, track: Track) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, track)
            _trackToAddToPlaylist.value = null
            Toast.makeText(getApplication(), "Añadida a la playlist", Toast.LENGTH_SHORT).show()
        }
    }

    fun showCreatePlaylist() {
        _showCreatePlaylistDialog.value = true
    }

    fun dismissCreatePlaylist() {
        _showCreatePlaylistDialog.value = false
    }

    fun createPlaylist(name: String, description: String) {
        viewModelScope.launch {
            val id = repository.createPlaylist(name, description)
            _showCreatePlaylistDialog.value = false
            Toast.makeText(getApplication(), "Playlist creada con éxito", Toast.LENGTH_SHORT).show()
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (_selectedPlaylist.value?.id == playlistId) {
                handleBack()
            }
            Toast.makeText(getApplication(), "Playlist eliminada", Toast.LENGTH_SHORT).show()
        }
    }

    fun updatePlaylistInfo(playlistId: String, name: String, description: String) {
        viewModelScope.launch {
            repository.updatePlaylist(playlistId, name, description)
            _selectedPlaylist.value = _selectedPlaylist.value?.copy(name = name, description = description)
            Toast.makeText(getApplication(), "Playlist actualizada", Toast.LENGTH_SHORT).show()
        }
    }

    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId)
            Toast.makeText(getApplication(), "Canción retirada de la playlist", Toast.LENGTH_SHORT).show()
        }
    }

    // YouTube & Smartools Integration
    fun openSmartoolsForTrack(track: Track) {
        _smartoolsTargetTrack.value = track
    }

    fun openSmartoolsForYouTube(result: YouTubeSearchResult) {
        _smartoolsTargetYouTube.value = result
    }

    fun dismissSmartoolsDialog() {
        _smartoolsTargetTrack.value = null
        _smartoolsTargetYouTube.value = null
    }

    fun launchSmartoolsDownloader(context: Context, videoId: String, trackTitle: String? = null) {
        val cleanId = videoId.ifBlank { "1fI48iN0X28" }
        val downloadUrl = repository.getSmartoolsDownloadUrl(cleanId)
        val ytWatchUrl = "https://www.youtube.com/watch?v=$cleanId"
        copyToClipboard(context, ytWatchUrl)
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            val nameText = if (!trackTitle.isNullOrBlank()) " «$trackTitle»" else ""
            Toast.makeText(context, "Abriendo Smartools para descargar$nameText (Enlace copiado)", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Enlace copiado al portapapeles: $ytWatchUrl", Toast.LENGTH_LONG).show()
        }
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Enlace Musical Sonora", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Enlace copiado al portapapeles", Toast.LENGTH_SHORT).show()
    }

    fun playYouTubeAsAudio(result: YouTubeSearchResult) {
        val durationSec = parseDurationToSeconds(result.durationText)
        val track = Track(
            id = "yt_" + result.videoId,
            title = result.title,
            artist = result.channelTitle,
            album = "YouTube Music",
            durationSeconds = durationSec,
            streamUrl = "https://www.youtube.com/watch?v=${result.videoId}",
            coverUrl = result.thumbnailUrl,
            youtubeId = result.videoId,
            genre = "YouTube"
        )
        playTrack(track, listOf(track))
        Toast.makeText(getApplication(), "Reproduciendo audio de YouTube: ${result.title}", Toast.LENGTH_SHORT).show()
    }

    private fun parseDurationToSeconds(text: String): Int {
        return try {
            val parts = text.split(":")
            if (parts.size == 2) {
                parts[0].trim().toInt() * 60 + parts[1].trim().toInt()
            } else if (parts.size == 3) {
                parts[0].trim().toInt() * 3600 + parts[1].trim().toInt() * 60 + parts[2].trim().toInt()
            } else {
                210
            }
        } catch (e: Exception) {
            210
        }
    }

    // Local Music Import
    fun importAudioFile(uri: Uri) {
        viewModelScope.launch {
            val imported = repository.importLocalAudio(uri)
            if (imported != null) {
                playTrack(imported, listOf(imported))
                Toast.makeText(getApplication(), "Audio importado y reproduciendo: ${imported.title}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(getApplication(), "No se pudo leer el archivo de audio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun toggleOfflineMode() {
        val curr = _userProfile.value
        _userProfile.value = curr.copy(isOffline = !curr.isOffline)
        val msg = if (_userProfile.value.isOffline) "Modo sin conexión activado" else "Modo online activado"
        Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
    }
}
