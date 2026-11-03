package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val streamUrl: String,
    val coverUrl: String,
    val youtubeId: String?,
    val isLocal: Boolean,
    val genre: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val coverUrl: String,
    val isCustom: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_tracks", primaryKeys = ["playlistId", "trackId"])
data class PlaylistTrackCrossRef(
    val playlistId: String,
    val trackId: String,
    val sortOrder: Int
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val trackId: String,
    val favoritedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val trackId: String,
    val playedAt: Long = System.currentTimeMillis()
)
