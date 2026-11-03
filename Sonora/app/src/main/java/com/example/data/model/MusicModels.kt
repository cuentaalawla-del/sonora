package com.example.data.model

enum class RepeatMode {
    OFF, ALL, ONE
}

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val streamUrl: String,
    val coverUrl: String,
    val youtubeId: String? = null,
    val isLocal: Boolean = false,
    val genre: String = "Pop",
    val playCount: Long = 0L
) {
    val formattedDuration: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

data class Playlist(
    val id: String,
    val name: String,
    val description: String,
    val coverUrl: String,
    val trackCount: Int = 0,
    val isCustom: Boolean = false,
    val tracks: List<Track> = emptyList()
)

data class Artist(
    val id: String,
    val name: String,
    val monthlyListeners: String,
    val imageUrl: String,
    val bio: String,
    val genre: String,
    val popularTracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList()
)

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val year: String,
    val coverUrl: String,
    val tracksCount: Int = 0,
    val tracks: List<Track> = emptyList()
)

data class Genre(
    val id: String,
    val name: String,
    val colorHex: Long,
    val imageUrl: String
)

data class UserProfile(
    val id: String = "user_premium_01",
    val name: String = "Melómano",
    val email: String = "usuario@sonora.premium",
    val avatarUrl: String = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop&q=80",
    val plan: String = "Premium Individual",
    val isOffline: Boolean = false
)

data class YouTubeSearchResult(
    val videoId: String,
    val title: String,
    val channelTitle: String,
    val durationText: String,
    val thumbnailUrl: String,
    val streamUrl: String
)
