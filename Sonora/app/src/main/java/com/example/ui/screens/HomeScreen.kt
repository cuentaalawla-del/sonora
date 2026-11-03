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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.MusicViewModel
import com.example.ui.Screen
import com.example.ui.components.*
import com.example.ui.theme.*
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val curatedPlaylists = viewModel.curatedPlaylists
    val popularAlbums = viewModel.popularAlbums
    val featuredArtists = viewModel.featuredArtists
    val catalogTracks = viewModel.allCatalogTracks

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importAudioFile(it) }
    }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..12 -> "Buenos días"
            in 13..20 -> "Buenas tardes"
            else -> "Buenas noches"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2C3E50),
                        BackgroundBlack,
                        BackgroundBlack
                    ),
                    startY = 0f,
                    endY = 700f
                )
            ),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Top Bar: Greeting & Profile
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Bienvenido a Sonora Premium",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Import local file quick button
                    IconButton(
                        onClick = { audioPickerLauncher.launch("audio/*") },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AudioFile,
                            contentDescription = "Importar audio",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Profile Avatar
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(userProfile.avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.navigateTo(Screen.PREMIUM) }
                    )
                }
            }
        }

        // 6-Card Quick Access Grid (Spotify Signature)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val quickItems = curatedPlaylists.take(6)
                for (i in quickItems.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val item1 = quickItems[i]
                        QuickMixCard(
                            title = item1.name,
                            imageUrl = item1.coverUrl,
                            onClick = { viewModel.openPlaylist(item1) },
                            onPlayClick = { viewModel.playPlaylist(item1) },
                            modifier = Modifier.weight(1f)
                        )

                        if (i + 1 < quickItems.size) {
                            val item2 = quickItems[i + 1]
                            QuickMixCard(
                                title = item2.name,
                                imageUrl = item2.coverUrl,
                                onClick = { viewModel.openPlaylist(item2) },
                                onPlayClick = { viewModel.playPlaylist(item2) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Section: Recomendaciones para ti
        item {
            Spacer(modifier = Modifier.height(28.dp))
            SectionHeader(
                title = "Hecho para ti",
                subtitle = "Mezclas y listas basadas en tus artistas favoritos"
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(curatedPlaylists) { playlist ->
                    CardAlbumPlaylist(
                        title = playlist.name,
                        subtitle = playlist.description,
                        imageUrl = playlist.coverUrl,
                        onClick = { viewModel.openPlaylist(playlist) },
                        onPlayClick = { viewModel.playPlaylist(playlist) }
                    )
                }
            }
        }

        // Section: Álbumes Populares
        item {
            Spacer(modifier = Modifier.height(28.dp))
            SectionHeader(
                title = "Álbumes populares",
                subtitle = "Las mejores producciones de estudio"
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(popularAlbums) { album ->
                    CardAlbumPlaylist(
                        title = album.title,
                        subtitle = "${album.artist} • ${album.year}",
                        imageUrl = album.coverUrl,
                        onClick = { viewModel.openAlbum(album) },
                        onPlayClick = {
                            if (album.tracks.isNotEmpty()) {
                                viewModel.playTrack(album.tracks.first(), album.tracks)
                            }
                        }
                    )
                }
            }
        }

        // Section: Artistas Recomendados
        item {
            Spacer(modifier = Modifier.height(28.dp))
            SectionHeader(
                title = "Artistas recomendados",
                subtitle = "Los más escuchados globalmente"
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(featuredArtists) { artist ->
                    CardArtist(
                        name = artist.name,
                        subtitle = "Artista • ${artist.monthlyListeners} oyentes",
                        imageUrl = artist.imageUrl,
                        onClick = { viewModel.openArtist(artist) }
                    )
                }
            }
        }

        // Section: Smartools Quick Downloader Banner
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SpotifyGreenDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = null,
                            tint = SpotifyGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Descargas con Smartools",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Toca el icono verde de descarga en cualquier canción para abrirla en Smartools.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary
                            )
                        )
                    }
                }
            }
        }

        // Section: Canciones en Tendencia (Spotify Top 10)
        item {
            Spacer(modifier = Modifier.height(28.dp))
            SectionHeader(
                title = "Tendencias del momento en Spotify",
                subtitle = "Las canciones más escuchadas del mundo hoy"
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                val currentTrack by viewModel.playbackManager.currentTrack.collectAsState()
                catalogTracks.take(10).forEachIndexed { idx, track ->
                    TrackItem(
                        track = track,
                        index = idx + 1,
                        isPlayingThis = currentTrack?.id == track.id,
                        viewModel = viewModel,
                        onTrackClick = { viewModel.playTrack(track, catalogTracks) }
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )
        subtitle?.let {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary
                )
            )
        }
    }
}
