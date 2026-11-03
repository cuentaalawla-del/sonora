package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.RepeatMode
import com.example.data.model.Track
import com.example.player.PlaybackManager
import com.example.ui.MusicViewModel
import com.example.ui.theme.*

@Composable
fun MiniPlayer(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val playback = viewModel.playbackManager
    val track by playback.currentTrack.collectAsState()
    val isPlaying by playback.isPlaying.collectAsState()
    val isBuffering by playback.isBuffering.collectAsState()
    val positionMs by playback.currentPositionMs.collectAsState()
    val durationMs by playback.durationMs.collectAsState()

    val current = track ?: return

    val isFav by viewModel.isFavorite(current.id).collectAsState(initial = false)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .shadow(12.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable { viewModel.openFullScreenPlayer() },
        color = SurfaceElevatedHigh,
        tonalElevation = 8.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(current.coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = current.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(6.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Artist
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = current.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = current.artist,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val context = LocalContext.current
                // Smartools Download button
                IconButton(
                    onClick = {
                        viewModel.launchSmartoolsDownloader(
                            context = context,
                            videoId = current.youtubeId ?: "1fI48iN0X28",
                            trackTitle = "${current.title} - ${current.artist}"
                        )
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DownloadForOffline,
                        contentDescription = "Descargar en Smartools",
                        tint = SpotifyGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Heart favorite
                IconButton(
                    onClick = { viewModel.toggleFavorite(current) },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (isFav) SpotifyGreen else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Play / Pause
                IconButton(
                    onClick = { playback.togglePlayPause() },
                    modifier = Modifier.size(40.dp)
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            color = SpotifyGreen,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = TextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Next
                IconButton(
                    onClick = { playback.next() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Siguiente",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Progress Line at the very bottom
            val progress = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = SpotifyGreen,
                trackColor = SurfaceBorder
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenPlayer(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val playback = viewModel.playbackManager
    val track by playback.currentTrack.collectAsState()
    val isPlaying by playback.isPlaying.collectAsState()
    val isBuffering by playback.isBuffering.collectAsState()
    val positionMs by playback.currentPositionMs.collectAsState()
    val durationMs by playback.durationMs.collectAsState()
    val isShuffle by playback.isShuffle.collectAsState()
    val repeatMode by playback.repeatMode.collectAsState()
    val volume by playback.volume.collectAsState()

    val current = track ?: return
    val isFav by viewModel.isFavorite(current.id).collectAsState(initial = false)
    val context = LocalContext.current

    var isUserSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF282828),
                        BackgroundBlack,
                        BackgroundPureBlack
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.closeFullScreenPlayer() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Cerrar",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "REPRODUCIENDO DE LA PLAYLIST",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    )
                    Text(
                        text = current.album.ifBlank { "Sonora Premium" },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { viewModel.showAddToPlaylistDialog(current) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Opciones",
                        tint = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large Album Artwork
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .shadow(24.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(current.coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = current.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Track Title & Artist & Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = current.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (!current.youtubeId.isNullOrBlank() && !current.isLocal) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.SmartDisplay,
                                contentDescription = null,
                                tint = AccentRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Audio oficial de YouTube",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SpotifyGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Text(
                        text = current.artist,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleFavorite(current) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (isFav) SpotifyGreen else TextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Slider
            val maxPos = durationMs.coerceAtLeast(1000).toFloat()
            val currentPos = if (isUserSeeking) seekPosition else positionMs.toFloat()

            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = currentPos.coerceIn(0f, maxPos),
                    onValueChange = {
                        isUserSeeking = true
                        seekPosition = it
                    },
                    onValueChangeFinished = {
                        playback.seekTo(seekPosition.toInt())
                        isUserSeeking = false
                    },
                    valueRange = 0f..maxPos,
                    colors = SliderDefaults.colors(
                        thumbColor = TextPrimary,
                        activeTrackColor = TextPrimary,
                        inactiveTrackColor = SurfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMillis(currentPos.toInt()),
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    Text(
                        text = "-" + formatMillis((maxPos - currentPos).toInt().coerceAtLeast(0)),
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Playback Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Shuffle
                IconButton(
                    onClick = { playback.toggleShuffle() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shuffle,
                        contentDescription = "Aleatorio",
                        tint = if (isShuffle) SpotifyGreen else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous
                IconButton(
                    onClick = { playback.previous() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Anterior",
                        tint = TextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Main Play / Pause Circle
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(TextPrimary)
                        .clickable { playback.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            color = BackgroundBlack,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = BackgroundBlack,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Next
                IconButton(
                    onClick = { playback.next() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Siguiente",
                        tint = TextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat
                IconButton(
                    onClick = { playback.toggleRepeat() },
                    modifier = Modifier.size(48.dp)
                ) {
                    val icon = when (repeatMode) {
                        RepeatMode.ONE -> Icons.Filled.RepeatOne
                        else -> Icons.Filled.Repeat
                    }
                    val tint = when (repeatMode) {
                        RepeatMode.OFF -> TextSecondary
                        else -> SpotifyGreen
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = "Repetir",
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Volume Control & Smartools Integration & Queue Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Device info / Hi-Fi
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        // Toggle volume / device
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Speaker,
                        contentDescription = "Dispositivo",
                        tint = SpotifyGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sonora Hi-Fi Audio",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SpotifyGreen,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Smartools Direct Downloader Button
                    Button(
                        onClick = {
                            viewModel.launchSmartoolsDownloader(
                                context = context,
                                videoId = current.youtubeId ?: "1fI48iN0X28",
                                trackTitle = "${current.title} - ${current.artist}"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                        shape = RoundedCornerShape(50.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = null,
                            tint = BackgroundBlack,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Descargar",
                            color = BackgroundBlack,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Queue button
                    IconButton(
                        onClick = { viewModel.toggleQueueDialog() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QueueMusic,
                            contentDescription = "Cola de reproducción",
                            tint = TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QueueDialog(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val playback = viewModel.playbackManager
    val queue by playback.queue.collectAsState()
    val currentIndex by playback.queueIndex.collectAsState()
    val currentTrack by playback.currentTrack.collectAsState()

    Dialog(
        onDismissRequest = { viewModel.toggleQueueDialog() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = BackgroundBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.toggleQueueDialog() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cerrar",
                                tint = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cola de reproducción",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    TextButton(onClick = { playback.clearQueue() }) {
                        Text("Limpiar", color = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Now Playing Section
                currentTrack?.let { curr ->
                    Text(
                        text = "Ahora suena",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    Surface(
                        color = SurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = curr.coverUrl,
                                contentDescription = curr.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = curr.title,
                                    color = SpotifyGreen,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = curr.artist,
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.Equalizer,
                                contentDescription = "Sonando",
                                tint = SpotifyGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "A continuación",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )

                if (queue.isEmpty() || queue.size == 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay más canciones en la cola",
                            color = TextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        itemsIndexed(queue) { index, item ->
                            if (index != currentIndex) {
                                Surface(
                                    color = Color.Transparent,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { playback.playFromQueue(index) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            color = TextSecondary,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.width(28.dp)
                                        )
                                        AsyncImage(
                                            model = item.coverUrl,
                                            contentDescription = item.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.title,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = item.artist,
                                                color = TextSecondary,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1
                                            )
                                        }

                                        IconButton(
                                            onClick = { playback.removeFromQueue(index) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.DeleteOutline,
                                                contentDescription = "Quitar",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatMillis(millis: Int): String {
    val totalSec = millis / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
