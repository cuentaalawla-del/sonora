package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.Playlist
import com.example.ui.MusicViewModel
import com.example.ui.components.TrackItem
import com.example.ui.theme.*

enum class LibraryFilter {
    ALL,
    PLAYLISTS,
    FAVORITES,
    ALBUMS,
    ARTISTS,
    HISTORY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    var activeFilter by remember { mutableStateOf(LibraryFilter.ALL) }
    val userPlaylists by viewModel.userPlaylists.collectAsState(initial = emptyList())
    val favoriteTracks by viewModel.favoriteTracks.collectAsState(initial = emptyList())
    val historyTracks by viewModel.historyTracks.collectAsState(initial = emptyList())
    val popularAlbums = viewModel.popularAlbums
    val featuredArtists = viewModel.featuredArtists
    val currentTrack by viewModel.playbackManager.currentTrack.collectAsState()

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importAudioFile(it) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .statusBarsPadding()
    ) {
        // Library Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LibraryMusic,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Tu biblioteca",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Import audio button
                IconButton(onClick = { audioPicker.launch("audio/*") }) {
                    Icon(
                        imageVector = Icons.Filled.AudioFile,
                        contentDescription = "Importar audio local",
                        tint = TextSecondary
                    )
                }

                // Create Playlist Button
                IconButton(onClick = { viewModel.showCreatePlaylist() }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Crear playlist",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Filter Pills
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = activeFilter == LibraryFilter.ALL,
                    onClick = { activeFilter = LibraryFilter.ALL },
                    label = { Text("Todo") },
                    colors = chipColors()
                )
            }
            item {
                FilterChip(
                    selected = activeFilter == LibraryFilter.PLAYLISTS,
                    onClick = { activeFilter = LibraryFilter.PLAYLISTS },
                    label = { Text("Playlists") },
                    colors = chipColors()
                )
            }
            item {
                FilterChip(
                    selected = activeFilter == LibraryFilter.FAVORITES,
                    onClick = { activeFilter = LibraryFilter.FAVORITES },
                    label = { Text("Tus me gusta") },
                    colors = chipColors()
                )
            }
            item {
                FilterChip(
                    selected = activeFilter == LibraryFilter.ALBUMS,
                    onClick = { activeFilter = LibraryFilter.ALBUMS },
                    label = { Text("Álbumes") },
                    colors = chipColors()
                )
            }
            item {
                FilterChip(
                    selected = activeFilter == LibraryFilter.ARTISTS,
                    onClick = { activeFilter = LibraryFilter.ARTISTS },
                    label = { Text("Artistas") },
                    colors = chipColors()
                )
            }
            item {
                FilterChip(
                    selected = activeFilter == LibraryFilter.HISTORY,
                    onClick = { activeFilter = LibraryFilter.HISTORY },
                    label = { Text("Historial") },
                    colors = chipColors()
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp, top = 4.dp)
        ) {
            // "Tus me gusta" Card (Pinned at the top)
            if (activeFilter == LibraryFilter.ALL || activeFilter == LibraryFilter.FAVORITES) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val favPlaylist = Playlist(
                                    id = "playlist_likes_special",
                                    name = "Canciones que te gustan",
                                    description = "Todas las canciones a las que has dado me gusta.",
                                    coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500",
                                    trackCount = favoriteTracks.size,
                                    tracks = favoriteTracks
                                )
                                viewModel.openPlaylist(favPlaylist)
                            },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF450AF5), Color(0xFF8E8EE5))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Canciones que te gustan",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Playlist • ${favoriteTracks.size} canciones",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SpotifyGreen
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // User Created Playlists
            if (activeFilter == LibraryFilter.ALL || activeFilter == LibraryFilter.PLAYLISTS) {
                items(userPlaylists) { pl ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.openPlaylist(pl) },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = pl.coverUrl,
                                contentDescription = pl.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pl.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    ),
                                    maxLines = 1
                                )
                                Text(
                                    text = "Playlist • Creada por ti",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary
                                    )
                                )
                            }

                            IconButton(onClick = { viewModel.deletePlaylist(pl.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Eliminar playlist",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Saved Albums
            if (activeFilter == LibraryFilter.ALL || activeFilter == LibraryFilter.ALBUMS) {
                items(popularAlbums) { album ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.openAlbum(album) },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = album.coverUrl,
                                contentDescription = album.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = album.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Álbum • ${album.artist}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Followed Artists
            if (activeFilter == LibraryFilter.ALL || activeFilter == LibraryFilter.ARTISTS) {
                items(featuredArtists) { artist ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.openArtist(artist) },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = artist.imageUrl,
                                contentDescription = artist.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = artist.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Artista",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // History Tab
            if (activeFilter == LibraryFilter.HISTORY) {
                if (historyTracks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aún no has reproducido canciones en el historial",
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    items(historyTracks) { track ->
                        TrackItem(
                            track = track,
                            isPlayingThis = currentTrack?.id == track.id,
                            viewModel = viewModel,
                            onTrackClick = { viewModel.playTrack(track, historyTracks) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    containerColor = SurfaceElevated,
    labelColor = TextPrimary,
    selectedContainerColor = SpotifyGreen,
    selectedLabelColor = BackgroundBlack
)
