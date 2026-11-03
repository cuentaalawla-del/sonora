package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Track
import com.example.ui.MusicViewModel
import com.example.ui.theme.*

@Composable
fun TrackItem(
    track: Track,
    index: Int? = null,
    isPlayingThis: Boolean = false,
    viewModel: MusicViewModel,
    onTrackClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val context = LocalContext.current
    val isFav by viewModel.isFavorite(track.id).collectAsState(initial = false)
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTrackClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track number or playing equalizer
        if (isPlayingThis) {
            Icon(
                imageVector = Icons.Filled.Equalizer,
                contentDescription = "Sonando",
                tint = SpotifyGreen,
                modifier = Modifier
                    .width(28.dp)
                    .size(20.dp)
            )
        } else if (index != null) {
            Text(
                text = "$index",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.width(28.dp)
            )
        }

        // Cover Thumbnail
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(track.coverUrl)
                .crossfade(true)
                .build(),
            contentDescription = track.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Title & Artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPlayingThis) SpotifyGreen else TextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${track.artist} • ${track.album}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (trailingContent != null) {
            trailingContent()
        } else {
            // Dedicated Direct Smartools Download Action Button (1-tap download!)
            IconButton(
                onClick = {
                    viewModel.launchSmartoolsDownloader(
                        context = context,
                        videoId = track.youtubeId ?: "1fI48iN0X28",
                        trackTitle = "${track.title} - ${track.artist}"
                    )
                },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DownloadForOffline,
                    contentDescription = "Descargar con Smartools",
                    tint = SpotifyGreen,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Heart toggle
            IconButton(
                onClick = { viewModel.toggleFavorite(track) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (isFav) SpotifyGreen else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // More Options Dropdown
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Más opciones",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Descargar con Smartools") },
                        onClick = {
                            showMenu = false
                            viewModel.launchSmartoolsDownloader(
                                context = context,
                                videoId = track.youtubeId ?: "1fI48iN0X28",
                                trackTitle = "${track.title} - ${track.artist}"
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Download, contentDescription = null, tint = SpotifyGreen)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Ver detalles de descarga") },
                        onClick = {
                            showMenu = false
                            viewModel.openSmartoolsForTrack(track)
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Info, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Añadir a playlist") },
                        onClick = {
                            showMenu = false
                            viewModel.showAddToPlaylistDialog(track)
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.PlaylistAdd, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reproducir a continuación") },
                        onClick = {
                            showMenu = false
                            viewModel.playbackManager.playNext(track)
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.QueueMusic, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Copiar enlace de YouTube") },
                        onClick = {
                            showMenu = false
                            val id = track.youtubeId ?: "1fI48iN0X28"
                            viewModel.copyToClipboard(context, "https://www.youtube.com/watch?v=$id")
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null)
                        }
                    )
                }
            }
        }
    }
}
