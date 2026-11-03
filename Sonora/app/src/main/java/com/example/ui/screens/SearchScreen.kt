package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.SearchTab
import com.example.data.model.Track
import com.example.data.model.YouTubeSearchResult
import com.example.ui.MusicViewModel
import com.example.ui.components.GenreCard
import com.example.ui.components.TrackItem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val query by viewModel.searchQuery.collectAsState()
    val activeTab by viewModel.searchTab.collectAsState()
    val youTubeResults by viewModel.youTubeResults.collectAsState()
    val catalogTracks = viewModel.allCatalogTracks
    val artists = viewModel.featuredArtists
    val albums = viewModel.popularAlbums
    val genres = viewModel.genres
    val currentTrack by viewModel.playbackManager.currentTrack.collectAsState()

    // Filtered lists
    val filteredTracks = remember(query, catalogTracks) {
        if (query.isBlank()) catalogTracks
        else catalogTracks.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true) ||
            it.album.contains(query, ignoreCase = true) ||
            it.genre.contains(query, ignoreCase = true)
        }
    }

    val filteredArtists = remember(query, artists) {
        if (query.isBlank()) artists
        else artists.filter { it.name.contains(query, ignoreCase = true) || it.genre.contains(query, ignoreCase = true) }
    }

    val filteredAlbums = remember(query, albums) {
        if (query.isBlank()) albums
        else albums.filter { it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .statusBarsPadding()
    ) {
        // Search Header
        Text(
            text = "Buscar",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

    val isSearchingYouTube by viewModel.isSearchingYouTube.collectAsState()

        // Search Input Bar
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = { Text("¿Qué te apetece escuchar? (YouTube en vivo)", color = TextSecondary) },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary)
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.triggerExplicitYouTubeSearch(query) }),
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    if (isSearchingYouTube) {
                        CircularProgressIndicator(
                            color = SpotifyGreen,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (query.isNotBlank()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Limpiar", tint = TextSecondary)
                        }
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceElevated,
                unfocusedContainerColor = SurfaceElevated,
                focusedBorderColor = SpotifyGreen,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Bar
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = activeTab == SearchTab.ALL,
                    onClick = { viewModel.onSearchTabSelected(SearchTab.ALL) },
                    label = { Text("Todo") },
                    colors = chipColors()
                )
            }
            item {
                FilterChip(
                    selected = activeTab == SearchTab.TRACKS,
                    onClick = { viewModel.onSearchTabSelected(SearchTab.TRACKS) },
                    label = { Text("Canciones") },
                    colors = chipColors()
                )
            }
            item {
                FilterChip(
                    selected = activeTab == SearchTab.YOUTUBE,
                    onClick = { viewModel.onSearchTabSelected(SearchTab.YOUTUBE) },
                    label = { Text("YouTube & Smartools") },
                    colors = chipColors(),
                    leadingIcon = {
                        Icon(Icons.Filled.SmartDisplay, contentDescription = null, tint = AccentRed, modifier = Modifier.size(16.dp))
                    }
                )
            }
            item {
                FilterChip(
                    selected = activeTab == SearchTab.ARTISTS,
                    onClick = { viewModel.onSearchTabSelected(SearchTab.ARTISTS) },
                    label = { Text("Artistas") },
                    colors = chipColors()
                )
            }
            item {
                FilterChip(
                    selected = activeTab == SearchTab.ALBUMS,
                    onClick = { viewModel.onSearchTabSelected(SearchTab.ALBUMS) },
                    label = { Text("Álbumes") },
                    colors = chipColors()
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Content
        if (query.isBlank() && activeTab != SearchTab.YOUTUBE) {
            // "Explorar todo" genre cards grid when search is empty
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Explorar todo",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(genres) { genre ->
                        GenreCard(
                            name = genre.name,
                            colorHex = genre.colorHex,
                            imageUrl = genre.imageUrl,
                            onClick = {
                                viewModel.onSearchQueryChanged(genre.name)
                            }
                        )
                    }
                }
            }
        } else {
            // Search Results List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp)
            ) {
                // If YouTube tab is selected or in ALL tab with query
                if (activeTab == SearchTab.YOUTUBE || (activeTab == SearchTab.ALL && query.isNotBlank())) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Vídeos de YouTube & Smartools",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Descargas autorizadas",
                                style = MaterialTheme.typography.labelSmall.copy(color = SpotifyGreen)
                            )
                        }
                    }
                    if (isSearchingYouTube) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = SpotifyGreen,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Buscando en YouTube en vivo...",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else if (youTubeResults.isEmpty() && query.isNotBlank()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No se encontraron vídeos para \"$query\"",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { viewModel.triggerExplicitYouTubeSearch(query) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                                ) {
                                    Icon(Icons.Filled.Search, contentDescription = null, tint = BackgroundBlack)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Reintentar búsqueda en YouTube", color = BackgroundBlack, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        items(youTubeResults) { yt ->
                            YouTubeResultItem(
                                result = yt,
                                onPlayClick = { viewModel.playYouTubeAsAudio(yt) },
                                onSmartoolsClick = {
                                    viewModel.launchSmartoolsDownloader(
                                        context = context,
                                        videoId = yt.videoId,
                                        trackTitle = yt.title
                                    )
                                }
                            )
                        }
                    }
                }

                // Native Tracks
                if (activeTab == SearchTab.ALL || activeTab == SearchTab.TRACKS) {
                    item {
                        Text(
                            text = "Canciones",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    if (filteredTracks.isEmpty()) {
                        item {
                            Text(
                                text = "No se encontraron canciones para '$query'",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    } else {
                        items(filteredTracks) { track ->
                            TrackItem(
                                track = track,
                                isPlayingThis = currentTrack?.id == track.id,
                                viewModel = viewModel,
                                onTrackClick = { viewModel.playTrack(track, filteredTracks) }
                            )
                        }
                    }
                }

                // Artists
                if (activeTab == SearchTab.ALL || activeTab == SearchTab.ARTISTS) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Artistas",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(filteredArtists) { artist ->
                        Surface(
                            color = Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openArtist(artist) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = artist.imageUrl,
                                    contentDescription = artist.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(50))
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
                                        text = "Artista • ${artist.monthlyListeners} oyentes",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                    )
                                }
                            }
                        }
                    }
                }

                // Albums
                if (activeTab == SearchTab.ALL || activeTab == SearchTab.ALBUMS) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Álbumes",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(filteredAlbums) { album ->
                        Surface(
                            color = Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openAlbum(album) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = album.coverUrl,
                                    contentDescription = album.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(52.dp)
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
                                        text = "${album.artist} • ${album.year}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
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

@Composable
fun YouTubeResultItem(
    result: YouTubeSearchResult,
    onPlayClick: () -> Unit,
    onSmartoolsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onPlayClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(70.dp, 48.dp)) {
                AsyncImage(
                    model = result.thumbnailUrl,
                    contentDescription = result.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(4.dp))
                )
                Surface(
                    color = BackgroundPureBlack.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                ) {
                    Text(
                        text = result.durationText,
                        color = TextPrimary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    ),
                    maxLines = 1
                )
                Text(
                    text = result.channelTitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary
                    ),
                    maxLines = 1
                )
            }

            // Smartools Downloader Action Button
            Button(
                onClick = onSmartoolsClick,
                colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                shape = RoundedCornerShape(50.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = null,
                    tint = BackgroundBlack,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Descargar",
                    color = BackgroundBlack,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall
                )
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
