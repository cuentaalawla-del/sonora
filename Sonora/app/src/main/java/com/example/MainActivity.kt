package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.RepeatMode
import com.example.ui.MusicViewModel
import com.example.ui.Screen
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SonoraTheme {
                SonoraApp()
            }
        }
    }
}

@Composable
fun SonoraApp(viewModel: MusicViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isFullScreenPlayerVisible by viewModel.isFullScreenPlayerVisible.collectAsState()
    val isQueueDialogVisible by viewModel.isQueueDialogVisible.collectAsState()
    val trackToAddToPlaylist by viewModel.trackToAddToPlaylist.collectAsState()
    val showCreatePlaylistDialog by viewModel.showCreatePlaylistDialog.collectAsState()
    val smartoolsTrack by viewModel.smartoolsTargetTrack.collectAsState()
    val smartoolsYouTube by viewModel.smartoolsTargetYouTube.collectAsState()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()
    val selectedArtist by viewModel.selectedArtist.collectAsState()
    val selectedAlbum by viewModel.selectedAlbum.collectAsState()
    val currentPlayingTrack by viewModel.playbackManager.currentTrack.collectAsState()

    // Handle Android system Back button
    BackHandler {
        if (!viewModel.handleBack()) {
            // Let system handle back (exit app)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        val isWideScreen = maxWidth > 600.dp

        // Persistent attached YouTube engine view so WebKit processes audio & playback on all screens
        // YouTube requires the embedded player to be at least 200x200 px; a 4dp view makes it
        // refuse to start (position stuck at 0:00). It sits behind the UI and is almost transparent.
        Box(
            modifier = Modifier
                .size(220.dp)
                .alpha(0.01f)
        ) {
            AndroidView(
                factory = { ctx ->
                    viewModel.playbackManager.youTubeEngine.getOrCreateWebView(ctx)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // Optional Navigation Rail for Wide Screens / Tablets
            if (isWideScreen) {
                NavigationRail(
                    containerColor = BackgroundBlack,
                    contentColor = TextPrimary,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Icon(
                        imageVector = Icons.Filled.GraphicEq,
                        contentDescription = "Sonora",
                        tint = SpotifyGreen,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    NavigationRailItem(
                        selected = currentScreen == Screen.HOME,
                        onClick = { viewModel.navigateTo(Screen.HOME) },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
                        label = { Text("Inicio") }
                    )
                    NavigationRailItem(
                        selected = currentScreen == Screen.SEARCH,
                        onClick = { viewModel.navigateTo(Screen.SEARCH) },
                        icon = { Icon(Icons.Filled.Search, contentDescription = "Buscar") },
                        label = { Text("Buscar") }
                    )
                    NavigationRailItem(
                        selected = currentScreen == Screen.LIBRARY,
                        onClick = { viewModel.navigateTo(Screen.LIBRARY) },
                        icon = { Icon(Icons.Filled.LibraryMusic, contentDescription = "Tu biblioteca") },
                        label = { Text("Tu biblioteca") }
                    )
                    NavigationRailItem(
                        selected = currentScreen == Screen.PREMIUM,
                        onClick = { viewModel.navigateTo(Screen.PREMIUM) },
                        icon = { Icon(Icons.Filled.WorkspacePremium, contentDescription = "Premium") },
                        label = { Text("Premium") }
                    )
                }
            }

            // Main Screen Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                when (currentScreen) {
                    Screen.HOME -> HomeScreen(viewModel = viewModel)
                    Screen.SEARCH -> SearchScreen(viewModel = viewModel)
                    Screen.LIBRARY -> LibraryScreen(viewModel = viewModel)
                    Screen.PREMIUM -> PremiumScreen(viewModel = viewModel)
                    Screen.PLAYLIST_DETAIL -> {
                        selectedPlaylist?.let { pl ->
                            PlaylistDetailScreen(playlist = pl, viewModel = viewModel)
                        } ?: HomeScreen(viewModel = viewModel)
                    }
                    Screen.ARTIST_DETAIL -> {
                        selectedArtist?.let { art ->
                            ArtistDetailScreen(artist = art, viewModel = viewModel)
                        } ?: HomeScreen(viewModel = viewModel)
                    }
                    Screen.ALBUM_DETAIL -> {
                        selectedAlbum?.let { alb ->
                            AlbumDetailScreen(album = alb, viewModel = viewModel)
                        } ?: HomeScreen(viewModel = viewModel)
                    }
                }

                // Persistent Bottom Layout (MiniPlayer + Bottom Navigation Bar)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    // Mini Player (Only if a track is active)
                    if (currentPlayingTrack != null && !isFullScreenPlayerVisible) {
                        MiniPlayer(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Mobile Bottom Navigation Bar
                    if (!isWideScreen) {
                        NavigationBar(
                            containerColor = BackgroundBlack.copy(alpha = 0.96f),
                            contentColor = TextPrimary,
                            windowInsets = WindowInsets.navigationBars,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == Screen.HOME,
                                onClick = { viewModel.navigateTo(Screen.HOME) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == Screen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                        contentDescription = "Inicio"
                                    )
                                },
                                label = { Text("Inicio") },
                                colors = navItemColors()
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.SEARCH,
                                onClick = { viewModel.navigateTo(Screen.SEARCH) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "Buscar"
                                    )
                                },
                                label = { Text("Buscar") },
                                colors = navItemColors()
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.LIBRARY,
                                onClick = { viewModel.navigateTo(Screen.LIBRARY) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == Screen.LIBRARY) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic,
                                        contentDescription = "Tu biblioteca"
                                    )
                                },
                                label = { Text("Tu biblioteca") },
                                colors = navItemColors()
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.PREMIUM,
                                onClick = { viewModel.navigateTo(Screen.PREMIUM) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == Screen.PREMIUM) Icons.Filled.WorkspacePremium else Icons.Outlined.WorkspacePremium,
                                        contentDescription = "Premium"
                                    )
                                },
                                label = { Text("Premium") },
                                colors = navItemColors()
                            )
                        }
                    }
                }
            }
        }

        // Full Screen Player Animated Overlay
        AnimatedVisibility(
            visible = isFullScreenPlayerVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            FullScreenPlayer(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Queue Dialog
        if (isQueueDialogVisible) {
            QueueDialog(viewModel = viewModel)
        }

        // Add to Playlist Dialog
        trackToAddToPlaylist?.let { track ->
            AddToPlaylistDialog(
                track = track,
                viewModel = viewModel,
                onDismiss = { viewModel.dismissAddToPlaylistDialog() }
            )
        }

        // Create Playlist Dialog
        if (showCreatePlaylistDialog) {
            CreatePlaylistDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.dismissCreatePlaylist() }
            )
        }

        // Smartools Integration Dialog
        if (smartoolsTrack != null || smartoolsYouTube != null) {
            SmartoolsDialog(
                track = smartoolsTrack,
                youtubeResult = smartoolsYouTube,
                viewModel = viewModel,
                onDismiss = { viewModel.dismissSmartoolsDialog() }
            )
        }
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = TextPrimary,
    selectedTextColor = TextPrimary,
    unselectedIconColor = TextSecondary,
    unselectedTextColor = TextSecondary,
    indicatorColor = SurfaceElevated
)
