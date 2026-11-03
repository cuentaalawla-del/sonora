package com.example.data.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.data.db.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.util.UUID

class MusicRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.musicDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            seedInitialDataIfNeeded()
        }
    }

    // Top hits directly from Spotify Global & Spanish Charts
    val allCatalogTracks: List<Track> = listOf(
        Track(
            id = "spotify_track_1",
            title = "MONACO",
            artist = "Bad Bunny",
            album = "nadie sabe lo que va a pasar mañana",
            durationSeconds = 267,
            streamUrl = "https://www.youtube.com/watch?v=1fI48iN0X28",
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
            youtubeId = "1fI48iN0X28",
            genre = "Latino Trap",
            playCount = 1420950300L
        ),
        Track(
            id = "spotify_track_2",
            title = "Quevedo: Bzrp Music Sessions, Vol. 52 (Quédate)",
            artist = "Bizarrap & Quevedo",
            album = "Bzrp Music Sessions",
            durationSeconds = 198,
            streamUrl = "https://www.youtube.com/watch?v=A_g3lMcWVy0",
            coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
            youtubeId = "A_g3lMcWVy0",
            genre = "Urbano",
            playCount = 1890200400L
        ),
        Track(
            id = "spotify_track_3",
            title = "LALA",
            artist = "Myke Towers",
            album = "LA VIDA ES UNA",
            durationSeconds = 197,
            streamUrl = "https://www.youtube.com/watch?v=Q_0W09H3M54",
            coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
            youtubeId = "Q_0W09H3M54",
            genre = "Reggaetón",
            playCount = 980400100L
        ),
        Track(
            id = "spotify_track_4",
            title = "Columbia",
            artist = "Quevedo",
            album = "DONDE QUIERO ESTAR",
            durationSeconds = 186,
            streamUrl = "https://www.youtube.com/watch?v=7s_c_rS0Qsw",
            coverUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=600&auto=format&fit=crop&q=80",
            youtubeId = "7s_c_rS0Qsw",
            genre = "Urbano Latino",
            playCount = 850320100L
        ),
        Track(
            id = "spotify_track_5",
            title = "Blinding Lights",
            artist = "The Weeknd",
            album = "After Hours",
            durationSeconds = 200,
            streamUrl = "https://www.youtube.com/watch?v=4NRXx6U8ABQ",
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            youtubeId = "4NRXx6U8ABQ",
            genre = "Synthpop / Pop",
            playCount = 4200000000L
        ),
        Track(
            id = "spotify_track_6",
            title = "Starboy (feat. Daft Punk)",
            artist = "The Weeknd",
            album = "Starboy",
            durationSeconds = 230,
            streamUrl = "https://www.youtube.com/watch?v=dqt8Z1k0oWQ",
            coverUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=600&auto=format&fit=crop&q=80",
            youtubeId = "dqt8Z1k0oWQ",
            genre = "R&B / Pop",
            playCount = 3100000000L
        ),
        Track(
            id = "spotify_track_7",
            title = "DESPECHÁ",
            artist = "Rosalía",
            album = "MOTOMAMI +",
            durationSeconds = 157,
            streamUrl = "https://www.youtube.com/watch?v=o_y7_bLwEAg",
            coverUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=600&auto=format&fit=crop&q=80",
            youtubeId = "o_y7_bLwEAg",
            genre = "Mambo Urbano",
            playCount = 1100000000L
        ),
        Track(
            id = "spotify_track_8",
            title = "TQG",
            artist = "Karol G & Shakira",
            album = "MAÑANA SERÁ BONITO",
            durationSeconds = 199,
            streamUrl = "https://www.youtube.com/watch?v=jZXh4Z9H3eY",
            coverUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=600&auto=format&fit=crop&q=80",
            youtubeId = "jZXh4Z9H3eY",
            genre = "Reggaetón",
            playCount = 1050000000L
        ),
        Track(
            id = "spotify_track_9",
            title = "LUNA",
            artist = "Feid & ATL Jacob",
            album = "FERXXOCALIPSIS",
            durationSeconds = 196,
            streamUrl = "https://www.youtube.com/watch?v=mDsk9jH3nK0",
            coverUrl = "https://images.unsplash.com/photo-1487180144351-b8472da7d491?w=600&auto=format&fit=crop&q=80",
            youtubeId = "mDsk9jH3nK0",
            genre = "Reggaetón",
            playCount = 920000000L
        ),
        Track(
            id = "spotify_track_10",
            title = "Flowers",
            artist = "Miley Cyrus",
            album = "Endless Summer Vacation",
            durationSeconds = 200,
            streamUrl = "https://www.youtube.com/watch?v=G7KNmW9a750",
            coverUrl = "https://images.unsplash.com/photo-1445985543469-72338c05e8e8?w=600&auto=format&fit=crop&q=80",
            youtubeId = "G7KNmW9a750",
            genre = "Pop",
            playCount = 2100000000L
        ),
        Track(
            id = "spotify_track_11",
            title = "As It Was",
            artist = "Harry Styles",
            album = "Harry's House",
            durationSeconds = 167,
            streamUrl = "https://www.youtube.com/watch?v=H5v3kku4y6Q",
            coverUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600&auto=format&fit=crop&q=80",
            youtubeId = "H5v3kku4y6Q",
            genre = "Indie Pop",
            playCount = 3300000000L
        ),
        Track(
            id = "spotify_track_12",
            title = "Cruel Summer",
            artist = "Taylor Swift",
            album = "Lover",
            durationSeconds = 178,
            streamUrl = "https://www.youtube.com/watch?v=ic8j13piAhQ",
            coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600&auto=format&fit=crop&q=80",
            youtubeId = "ic8j13piAhQ",
            genre = "Pop",
            playCount = 2400000000L
        ),
        Track(
            id = "spotify_track_13",
            title = "Ella Baila Sola",
            artist = "Eslabon Armado & Peso Pluma",
            album = "DESVELADO",
            durationSeconds = 165,
            streamUrl = "https://www.youtube.com/watch?v=lZIA7c0tF-g",
            coverUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=600&auto=format&fit=crop&q=80",
            youtubeId = "lZIA7c0tF-g",
            genre = "Corridos Tumbados",
            playCount = 1300000000L
        ),
        Track(
            id = "spotify_track_14",
            title = "Dákiti",
            artist = "Bad Bunny & Jhayco",
            album = "El Último Tour Del Mundo",
            durationSeconds = 205,
            streamUrl = "https://www.youtube.com/watch?v=TmKh7lAwnBI",
            coverUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=600&auto=format&fit=crop&q=80",
            youtubeId = "TmKh7lAwnBI",
            genre = "Reggaetón",
            playCount = 2100000000L
        ),
        Track(
            id = "spotify_track_15",
            title = "Shakira: Bzrp Music Sessions, Vol. 53",
            artist = "Bizarrap & Shakira",
            album = "Bzrp Music Sessions",
            durationSeconds = 213,
            streamUrl = "https://www.youtube.com/watch?v=CocEMWJ79SE",
            coverUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=600&auto=format&fit=crop&q=80",
            youtubeId = "CocEMWJ79SE",
            genre = "Pop Urbano",
            playCount = 1200000000L
        ),
        Track(
            id = "spotify_track_16",
            title = "Levitating",
            artist = "Dua Lipa",
            album = "Future Nostalgia",
            durationSeconds = 203,
            streamUrl = "https://www.youtube.com/watch?v=TUVcZfQe-Kw",
            coverUrl = "https://images.unsplash.com/photo-1511192336575-5a79af67a629?w=600&auto=format&fit=crop&q=80",
            youtubeId = "TUVcZfQe-Kw",
            genre = "Disco Pop",
            playCount = 2200000000L
        )
    )

    val curatedPlaylists: List<Playlist> = listOf(
        Playlist(
            id = "playlist_top_espana",
            name = "Top 50: España",
            description = "Tu actualización diaria con las canciones más escuchadas ahora mismo en España.",
            coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
            trackCount = 10,
            isCustom = false,
            tracks = listOf(
                allCatalogTracks[0], // MONACO
                allCatalogTracks[1], // Quevedo BZRP
                allCatalogTracks[2], // LALA
                allCatalogTracks[3], // Columbia
                allCatalogTracks[6], // DESPECHÁ
                allCatalogTracks[7], // TQG
                allCatalogTracks[8], // LUNA
                allCatalogTracks[12], // Ella Baila Sola
                allCatalogTracks[13], // Dákiti
                allCatalogTracks[14]  // Shakira BZRP
            )
        ),
        Playlist(
            id = "playlist_top_global",
            name = "Top 50: Global",
            description = "Tu actualización diaria con las canciones más reproducidas a nivel mundial en Spotify.",
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
            trackCount = 10,
            isCustom = false,
            tracks = listOf(
                allCatalogTracks[4], // Blinding Lights
                allCatalogTracks[11], // Cruel Summer
                allCatalogTracks[9], // Flowers
                allCatalogTracks[10], // As It Was
                allCatalogTracks[0], // MONACO
                allCatalogTracks[5], // Starboy
                allCatalogTracks[15], // Levitating
                allCatalogTracks[1], // Quevedo BZRP
                allCatalogTracks[7], // TQG
                allCatalogTracks[2]  // LALA
            )
        ),
        Playlist(
            id = "playlist_viva_latino",
            name = "Viva Latino",
            description = "El pulso de la música latina actual. Los lanzamientos más potentes y los ritmos que mandan.",
            coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
            trackCount = 8,
            isCustom = false,
            tracks = listOf(
                allCatalogTracks[0], allCatalogTracks[1], allCatalogTracks[2], allCatalogTracks[3],
                allCatalogTracks[6], allCatalogTracks[7], allCatalogTracks[8], allCatalogTracks[13]
            )
        ),
        Playlist(
            id = "playlist_exitos_espana",
            name = "Éxitos España",
            description = "Los grandes hits de la música en español que están arrasando en las listas y clubes.",
            coverUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=600&auto=format&fit=crop&q=80",
            trackCount = 7,
            isCustom = false,
            tracks = listOf(
                allCatalogTracks[1], allCatalogTracks[3], allCatalogTracks[6],
                allCatalogTracks[0], allCatalogTracks[8], allCatalogTracks[2], allCatalogTracks[14]
            )
        ),
        Playlist(
            id = "playlist_this_is_bad_bunny",
            name = "This Is Bad Bunny",
            description = "Lo mejor del Conejo Malo en una sola lista indispensable.",
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            trackCount = 2,
            isCustom = false,
            tracks = listOf(allCatalogTracks[0], allCatalogTracks[13])
        ),
        Playlist(
            id = "playlist_todays_top_hits",
            name = "Today's Top Hits",
            description = "Los éxitos que dominan el mundo hoy en streaming.",
            coverUrl = "https://images.unsplash.com/photo-1445985543469-72338c05e8e8?w=600&auto=format&fit=crop&q=80",
            trackCount = 6,
            isCustom = false,
            tracks = listOf(
                allCatalogTracks[4], allCatalogTracks[9], allCatalogTracks[10],
                allCatalogTracks[11], allCatalogTracks[15], allCatalogTracks[0]
            )
        )
    )

    val popularAlbums: List<Album> = listOf(
        Album(
            id = "album_bad_bunny_nadie",
            title = "nadie sabe lo que va a pasar mañana",
            artist = "Bad Bunny",
            year = "2023",
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
            tracksCount = 22,
            tracks = listOf(allCatalogTracks[0], allCatalogTracks[13])
        ),
        Album(
            id = "album_quevedo_donde",
            title = "DONDE QUIERO ESTAR",
            artist = "Quevedo",
            year = "2023",
            coverUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=600&auto=format&fit=crop&q=80",
            tracksCount = 16,
            tracks = listOf(allCatalogTracks[3])
        ),
        Album(
            id = "album_weeknd_after_hours",
            title = "After Hours",
            artist = "The Weeknd",
            year = "2020",
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            tracksCount = 14,
            tracks = listOf(allCatalogTracks[4])
        ),
        Album(
            id = "album_rosalia_motomami",
            title = "MOTOMAMI +",
            artist = "Rosalía",
            year = "2022",
            coverUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=600&auto=format&fit=crop&q=80",
            tracksCount = 24,
            tracks = listOf(allCatalogTracks[6])
        ),
        Album(
            id = "album_karol_manana",
            title = "MAÑANA SERÁ BONITO",
            artist = "Karol G",
            year = "2023",
            coverUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=600&auto=format&fit=crop&q=80",
            tracksCount = 17,
            tracks = listOf(allCatalogTracks[7])
        ),
        Album(
            id = "album_feid_ferxxo",
            title = "FERXXOCALIPSIS",
            artist = "Feid",
            year = "2023",
            coverUrl = "https://images.unsplash.com/photo-1487180144351-b8472da7d491?w=600&auto=format&fit=crop&q=80",
            tracksCount = 10,
            tracks = listOf(allCatalogTracks[8])
        )
    )

    val featuredArtists: List<Artist> = listOf(
        Artist(
            id = "artist_bad_bunny",
            name = "Bad Bunny",
            monthlyListeners = "78.920.450",
            imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=600&auto=format&fit=crop&q=80",
            bio = "Artista puertorriqueño global con múltiples números 1 mundiales y álbumes históricos en Spotify.",
            genre = "Trap Latino / Reggaetón",
            popularTracks = listOf(allCatalogTracks[0], allCatalogTracks[13]),
            albums = listOf(popularAlbums[0])
        ),
        Artist(
            id = "artist_quevedo",
            name = "Quevedo",
            monthlyListeners = "34.210.890",
            imageUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=600&auto=format&fit=crop&q=80",
            bio = "Cantante y rapero español de Gran Canaria que rompió récords globales con su sesión con Bizarrap.",
            genre = "Urbano / Reggaetón",
            popularTracks = listOf(allCatalogTracks[1], allCatalogTracks[3]),
            albums = listOf(popularAlbums[1])
        ),
        Artist(
            id = "artist_the_weeknd",
            name = "The Weeknd",
            monthlyListeners = "105.400.120",
            imageUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=600&auto=format&fit=crop&q=80",
            bio = "El cantante canadiense más escuchado en la historia de Spotify, creador de 'Blinding Lights'.",
            genre = "R&B / Synthpop",
            popularTracks = listOf(allCatalogTracks[4], allCatalogTracks[5]),
            albums = listOf(popularAlbums[2])
        ),
        Artist(
            id = "artist_rosalia",
            name = "Rosalía",
            monthlyListeners = "29.150.300",
            imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=600&auto=format&fit=crop&q=80",
            bio = "Cantante, compositora y productora española aclamada internacionalmente por reinventar el flamenco y el pop urbano.",
            genre = "Flamenco Pop / Urbano",
            popularTracks = listOf(allCatalogTracks[6]),
            albums = listOf(popularAlbums[3])
        ),
        Artist(
            id = "artist_karol_g",
            name = "Karol G",
            monthlyListeners = "52.840.670",
            imageUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=600&auto=format&fit=crop&q=80",
            bio = "La Bichota de Colombia, líder femenina indiscutible de la música latina en las plataformas de streaming.",
            genre = "Reggaetón / Urbano",
            popularTracks = listOf(allCatalogTracks[7]),
            albums = listOf(popularAlbums[4])
        ),
        Artist(
            id = "artist_feid",
            name = "Feid",
            monthlyListeners = "42.610.200",
            imageUrl = "https://images.unsplash.com/photo-1487180144351-b8472da7d491?w=600&auto=format&fit=crop&q=80",
            bio = "El Ferxxo, compositor e intérprete colombiano que ha revolucionado la cultura y el sonido urbano actual.",
            genre = "Reggaetón",
            popularTracks = listOf(allCatalogTracks[8]),
            albums = listOf(popularAlbums[5])
        ),
        Artist(
            id = "artist_bizarrap",
            name = "Bizarrap",
            monthlyListeners = "38.790.300",
            imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            bio = "DJ y productor argentino cuyas Music Sessions se convierten en fenómenos mundiales instantáneos.",
            genre = "Electrónica / Trap",
            popularTracks = listOf(allCatalogTracks[1], allCatalogTracks[14]),
            albums = listOf(popularAlbums[1])
        ),
        Artist(
            id = "artist_taylor_swift",
            name = "Taylor Swift",
            monthlyListeners = "98.340.000",
            imageUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600&auto=format&fit=crop&q=80",
            bio = "Superestrella mundial del pop con récords históricos en giras mundiales y reproducciones continuas.",
            genre = "Pop",
            popularTracks = listOf(allCatalogTracks[11]),
            albums = emptyList()
        )
    )

    val genres: List<Genre> = listOf(
        Genre("g_latino", "Latino & Reggaetón", 0xFFE13300, "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=300"),
        Genre("g_pop", "Pop Internacional", 0xFF2E77D0, "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=300"),
        Genre("g_urbano", "Urbano & Trap", 0xFFBC5900, "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=300"),
        Genre("g_electronic", "Electrónica & Dance", 0xFF8C1932, "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=300"),
        Genre("g_rock", "Rock & Alternativo", 0xFFE91429, "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=300"),
        Genre("g_lofi", "Chill & Lo-Fi", 0xFF477D95, "https://images.unsplash.com/photo-1445985543469-72338c05e8e8?w=300"),
        Genre("g_hiphop", "Hip-Hop", 0xFFF59B23, "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300"),
        Genre("g_indie", "Indie & Acústico", 0xFF148A08, "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=300")
    )

    private suspend fun seedInitialDataIfNeeded() {
        val entities = allCatalogTracks.map { it.toEntity() }
        dao.insertTracks(entities)

        val initialPlaylist = PlaylistEntity(
            id = "playlist_fav_user",
            name = "Mis Éxitos Favoritos",
            description = "Tus canciones favoritas guardadas de Spotify.",
            coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500",
            isCustom = true
        )
        dao.insertPlaylist(initialPlaylist)
        dao.addTrackToPlaylist(PlaylistTrackCrossRef(initialPlaylist.id, "spotify_track_1", 0))
        dao.addTrackToPlaylist(PlaylistTrackCrossRef(initialPlaylist.id, "spotify_track_2", 1))
        dao.addTrackToPlaylist(PlaylistTrackCrossRef(initialPlaylist.id, "spotify_track_5", 2))

        dao.addFavorite(FavoriteEntity("spotify_track_1"))
        dao.addFavorite(FavoriteEntity("spotify_track_2"))
        dao.addFavorite(FavoriteEntity("spotify_track_5"))

        dao.addToHistory(HistoryEntity(trackId = "spotify_track_1"))
        dao.addToHistory(HistoryEntity(trackId = "spotify_track_2"))
    }

    val userPlaylistsFlow: Flow<List<Playlist>> = dao.getAllPlaylists().map { entities ->
        entities.map { it.toDomain() }
    }

    val favoriteTracksFlow: Flow<List<Track>> = dao.getFavoriteTracks().map { list ->
        list.map { it.toDomain() }
    }

    val historyTracksFlow: Flow<List<Track>> = dao.getHistoryTracks().map { list ->
        list.map { it.toDomain() }
    }

    fun isFavorite(trackId: String): Flow<Boolean> = dao.isFavoriteFlow(trackId)

    suspend fun toggleFavorite(track: Track): Boolean {
        val currentlyFav = dao.isFavorite(track.id)
        if (currentlyFav) {
            dao.removeFavorite(track.id)
            return false
        } else {
            dao.insertTrack(track.toEntity())
            dao.addFavorite(FavoriteEntity(track.id))
            return true
        }
    }

    suspend fun addToHistory(track: Track) {
        dao.insertTrack(track.toEntity())
        dao.addToHistory(HistoryEntity(trackId = track.id))
    }

    suspend fun createPlaylist(name: String, description: String): String {
        val id = "playlist_custom_" + UUID.randomUUID().toString().take(8)
        val entity = PlaylistEntity(
            id = id,
            name = name.ifBlank { "Mi Playlist" },
            description = description.ifBlank { "Creada por ti en Sonora" },
            coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500",
            isCustom = true
        )
        dao.insertPlaylist(entity)
        return id
    }

    suspend fun updatePlaylist(playlistId: String, name: String, description: String) {
        dao.updatePlaylist(playlistId, name, description)
    }

    suspend fun deletePlaylist(playlistId: String) {
        dao.deletePlaylist(playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: String, track: Track) {
        dao.insertTrack(track.toEntity())
        val count = dao.getTrackCountForPlaylist(playlistId)
        dao.addTrackToPlaylist(PlaylistTrackCrossRef(playlistId, track.id, count))
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        dao.removeTrackFromPlaylist(playlistId, trackId)
    }

    fun getTracksForPlaylist(playlistId: String): Flow<List<Track>> {
        val curated = curatedPlaylists.firstOrNull { it.id == playlistId }
        if (curated != null && !curated.isCustom) {
            return kotlinx.coroutines.flow.flowOf(curated.tracks)
        }
        return dao.getTracksForPlaylist(playlistId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // Real-time live YouTube Search integrating YouTube's search engine
    suspend fun searchYouTube(query: String): List<YouTubeSearchResult> {
        val clean = query.trim()
        if (clean.isBlank()) {
            return allCatalogTracks.map { track ->
                YouTubeSearchResult(
                    videoId = track.youtubeId ?: "1fI48iN0X28",
                    title = "${track.title} - ${track.artist} (Official Video)",
                    channelTitle = "${track.artist} Official",
                    durationText = track.formattedDuration,
                    thumbnailUrl = track.coverUrl,
                    streamUrl = track.streamUrl
                )
            }
        }
        return YouTubeSearchEngine.search(clean)
    }

    // Smartools Video Downloader Integration
    fun getSmartoolsDownloadUrl(youtubeId: String): String {
        val cleanId = youtubeId.ifBlank { "1fI48iN0X28" }
        val youtubeWatchUrl = "https://www.youtube.com/watch?v=$cleanId"
        return "https://smartools.pro/youtube-video-downloader/?url=" + URLEncoder.encode(youtubeWatchUrl, "UTF-8")
    }

    suspend fun importLocalAudio(uri: Uri): Track? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.')
                ?: "Audio Local"
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Desconocido"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Archivos Locales"
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationSec = (durationStr?.toLongOrNull() ?: 180000L) / 1000
            retriever.release()

            val track = Track(
                id = "local_" + UUID.randomUUID().toString(),
                title = title,
                artist = artist,
                album = album,
                durationSeconds = durationSec.toInt().coerceAtLeast(10),
                streamUrl = uri.toString(),
                coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500",
                youtubeId = "1fI48iN0X28",
                isLocal = true,
                genre = "Local"
            )
            dao.insertTrack(track.toEntity())
            track
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun Track.toEntity(): TrackEntity = TrackEntity(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationSeconds = durationSeconds,
        streamUrl = streamUrl,
        coverUrl = coverUrl,
        youtubeId = youtubeId,
        isLocal = isLocal,
        genre = genre
    )

    private fun TrackEntity.toDomain(): Track = Track(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationSeconds = durationSeconds,
        streamUrl = streamUrl,
        coverUrl = coverUrl,
        youtubeId = youtubeId,
        isLocal = isLocal,
        genre = genre
    )

    private fun PlaylistEntity.toDomain(): Playlist = Playlist(
        id = id,
        name = name,
        description = description,
        coverUrl = coverUrl,
        trackCount = 0,
        isCustom = isCustom
    )
}
