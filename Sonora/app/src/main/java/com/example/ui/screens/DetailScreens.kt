package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.data.model.Album
import com.example.data.model.Artist
import com.example.data.model.Playlist
import com.example.data.model.Track
import com.example.ui.MusicViewModel
import com.example.ui.components.CardAlbumPlaylist
import com.example.ui.components.TrackItem
import com.example.ui.theme.*

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val currentTrack by viewModel.playbackManager.currentTrack.collectAsState()
    val isPlaying by viewModel.playbackManager.isPlaying.collectAsState()
    val tracks by viewModel.playlistTracks.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(playlist.name) }
    var editDesc by remember { mutableStateOf(playlist.description) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF38434F),
                        BackgroundBlack,
                        BackgroundBlack
                    ),
                    startY = 0f,
                    endY = 800f
                )
            ),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Back Button & Playlist Info
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = { viewModel.handleBack() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(playlist.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = playlist.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = playlist.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${tracks.size} canciones • Sonora Premium",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Bar: Big Play Button & Shuffle & More
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Big Green Play Button
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(SpotifyGreen)
                                .clickable {
                                    if (tracks.isNotEmpty()) {
                                        viewModel.playTrack(tracks.first(), tracks)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying && currentTrack?.let { ct -> tracks.any { it.id == ct.id } } == true)
                                    Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Reproducir todo",
                                tint = BackgroundBlack,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Shuffle Button
                        IconButton(
                            onClick = {
                                if (tracks.isNotEmpty()) {
                                    val shuffled = tracks.shuffled()
                                    viewModel.playTrack(shuffled.first(), shuffled)
                                }
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shuffle,
                                contentDescription = "Aleatorio",
                                tint = SpotifyGreen,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Smartools Download Playlist First / Selected Track Button
                        val context = LocalContext.current
                        IconButton(
                            onClick = {
                                if (tracks.isNotEmpty()) {
                                    val t = currentTrack ?: tracks.first()
                                    viewModel.launchSmartoolsDownloader(
                                        context = context,
                                        videoId = t.youtubeId ?: "1fI48iN0X28",
                                        trackTitle = "${t.title} - ${t.artist}"
                                    )
                                }
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DownloadForOffline,
                                contentDescription = "Descargar con Smartools",
                                tint = SpotifyGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    if (playlist.isCustom) {
                        Row {
                            IconButton(onClick = { showEditDialog = true }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = TextSecondary)
                            }
                            IconButton(onClick = { viewModel.deletePlaylist(playlist.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // Tracks Listing
        if (tracks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Esta playlist aún no tiene canciones.\n¡Añade tus favoritas desde el buscador o el catálogo!",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            itemsIndexed(tracks) { index, track ->
                TrackItem(
                    track = track,
                    index = index + 1,
                    isPlayingThis = currentTrack?.id == track.id,
                    viewModel = viewModel,
                    onTrackClick = { viewModel.playTrack(track, tracks) },
                    trailingContent = if (playlist.isCustom) {
                        {
                            IconButton(
                                onClick = { viewModel.removeTrackFromPlaylist(playlist.id, track.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.RemoveCircleOutline,
                                    contentDescription = "Quitar",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else null
                )
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar playlist", color = TextPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nombre") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Descripción") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updatePlaylistInfo(playlist.id, editName, editDesc)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                ) {
                    Text("Guardar", color = BackgroundBlack)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = SurfaceElevated
        )
    }
}

@Composable
fun ArtistDetailScreen(
    artist: Artist,
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    var isFollowing by remember { mutableStateOf(false) }
    val currentTrack by viewModel.playbackManager.currentTrack.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Hero Image Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(artist.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = artist.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    BackgroundBlack.copy(alpha = 0.5f),
                                    BackgroundBlack
                                )
                            )
                        )
                )

                // Back Button
                IconButton(
                    onClick = { viewModel.handleBack() },
                    modifier = Modifier
                        .padding(16.dp)
                        .statusBarsPadding()
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BackgroundBlack.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                }

                // Artist Info at the bottom of the hero
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Verificado",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Artista verificado",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary)
                        )
                    }

                    Text(
                        text = artist.name,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 32.sp
                        )
                    )

                    Text(
                        text = "${artist.monthlyListeners} oyentes mensuales",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }
        }

        // Controls: Follow & Play
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { isFollowing = !isFollowing },
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isFollowing) SpotifyGreen else Color.Transparent
                    )
                ) {
                    Text(
                        text = if (isFollowing) "Siguiendo" else "Seguir",
                        color = if (isFollowing) BackgroundBlack else TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(SpotifyGreen)
                        .clickable {
                            if (artist.popularTracks.isNotEmpty()) {
                                viewModel.playTrack(artist.popularTracks.first(), artist.popularTracks)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Reproducir artista",
                        tint = BackgroundBlack,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Popular Tracks
        item {
            Text(
                text = "Populares",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        itemsIndexed(artist.popularTracks) { idx, track ->
            TrackItem(
                track = track,
                index = idx + 1,
                isPlayingThis = currentTrack?.id == track.id,
                viewModel = viewModel,
                onTrackClick = { viewModel.playTrack(track, artist.popularTracks) }
            )
        }

        // Discography
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Discografía",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(artist.albums) { alb ->
                    CardAlbumPlaylist(
                        title = alb.title,
                        subtitle = alb.year,
                        imageUrl = alb.coverUrl,
                        onClick = { viewModel.openAlbum(alb) },
                        onPlayClick = {
                            if (alb.tracks.isNotEmpty()) {
                                viewModel.playTrack(alb.tracks.first(), alb.tracks)
                            }
                        }
                    )
                }
            }
        }

        // Bio Card
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Información del artista",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = artist.bio,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary,
                            lineHeight = 22.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Género: ${artist.genre}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SpotifyGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AlbumDetailScreen(
    album: Album,
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val currentTrack by viewModel.playbackManager.currentTrack.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4A3425),
                        BackgroundBlack,
                        BackgroundBlack
                    ),
                    startY = 0f,
                    endY = 800f
                )
            ),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = { viewModel.handleBack() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(album.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = album.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = album.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Text(
                    text = "${album.artist} • ${album.year}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(SpotifyGreen)
                            .clickable {
                                if (album.tracks.isNotEmpty()) {
                                    viewModel.playTrack(album.tracks.first(), album.tracks)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Reproducir álbum",
                            tint = BackgroundBlack,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(
                        onClick = {
                            if (album.tracks.isNotEmpty()) {
                                val sh = album.tracks.shuffled()
                                viewModel.playTrack(sh.first(), sh)
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Shuffle, contentDescription = "Aleatorio", tint = SpotifyGreen)
                    }
                }
            }
        }

        itemsIndexed(album.tracks) { index, track ->
            TrackItem(
                track = track,
                index = index + 1,
                isPlayingThis = currentTrack?.id == track.id,
                viewModel = viewModel,
                onTrackClick = { viewModel.playTrack(track, album.tracks) }
            )
        }
    }
}

@Composable
fun PremiumScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "Sonora Premium",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Música sin anuncios, audio en alta fidelidad y descargas",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // User Profile Card (Google Account info)
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = userProfile.avatarUrl,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userProfile.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = userProfile.email,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = SpotifyGreenDark,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = userProfile.plan,
                                color = TextPrimary,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Premium Features Overview
            Text(
                text = "Beneficios de tu suscripción",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            PremiumBenefitRow(
                icon = Icons.Filled.Check,
                title = "Sin anuncios ni interrupciones",
                desc = "Disfruta de música ininterrumpida las 24 horas del día"
            )
            PremiumBenefitRow(
                icon = Icons.Filled.HighQuality,
                title = "Audio Hi-Fi 320 kbps",
                desc = "Máxima definición sonora y rango dinámico impecable"
            )
            PremiumBenefitRow(
                icon = Icons.Filled.CloudDownload,
                title = "Smartools Video & Audio Downloader",
                desc = "Integración directa para transferir enlaces de YouTube a Smartools de forma segura"
            )
            PremiumBenefitRow(
                icon = Icons.Filled.Shuffle,
                title = "Reproducción aleatoria ilimitada",
                desc = "Salta todas las canciones que quieras sin restricciones"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Settings & Toggles
            Text(
                text = "Configuración de audio",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Modo sin conexión", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Reproduce solo canciones descargadas y locales", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = userProfile.isOffline,
                            onCheckedChange = { viewModel.toggleOfflineMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SpotifyGreen,
                                checkedTrackColor = SpotifyGreenDark
                            )
                        )
                    }

                    HorizontalDivider(color = SurfaceBorder, modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Calidad de streaming", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Muy alta (320 kbps)", color = SpotifyGreen, style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.Filled.Check, contentDescription = null, tint = SpotifyGreen)
                    }

                    HorizontalDivider(color = SurfaceBorder, modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Normalización de volumen", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Mismo nivel de volumen para todas las canciones", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = true,
                            onCheckedChange = {},
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SpotifyGreen,
                                checkedTrackColor = SpotifyGreenDark
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun PremiumBenefitRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SpotifyGreen,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(text = title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(text = desc, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}
