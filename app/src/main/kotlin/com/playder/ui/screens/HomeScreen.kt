// HomeScreen.kt

package com.playder.ui.screens

import com.playder.ui.utils.stableSystemBars
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import com.playder.ui.component.GlassState
import com.playder.ui.component.glassRoot
import com.playder.ui.component.GlassPillBackground
import com.playder.ui.component.ProgressiveGlassBackground
import com.playder.ui.component.BlurDirection
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.LazyGridPrefetchStrategy
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListPrefetchStrategy
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.em
import com.playder.ui.component.BlurEffectCache
import com.playder.ui.theme.InterFontFamily
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.graphics.TransformOrigin

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.palette.graphics.Palette
import coil3.imageLoader
import coil3.request.allowHardware
import coil3.toBitmap
import com.playder.ui.theme.PlayerColorExtractor
import androidx.compose.ui.graphics.toArgb
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.models.YTItem
import com.playder.constants.ProfilePicUriKey
import com.playder.constants.ThumbnailCornerRadius
import com.playder.db.entities.Album
import com.playder.db.entities.Artist
import com.playder.db.entities.LocalItem
import com.playder.db.entities.Playlist
import com.playder.db.entities.Song
import com.playder.extensions.toMediaItem
import com.playder.LocalPlayerAwareWindowInsets
import com.playder.LocalPlayerConnection
import com.playder.models.ArtistGroup
import com.playder.models.toMediaMetadata
import com.playder.playback.queues.ListQueue
import com.playder.playback.queues.YouTubeQueue
import com.playder.R
import com.playder.ui.component.ArtistListCard
import com.playder.ui.component.BonesHost
import com.playder.ui.component.HeroCarousel
import com.playder.ui.component.HeroHeightFraction
import com.playder.ui.component.HomeCardCornerRadius
import com.playder.ui.component.HomeCardSpacing
import com.playder.ui.component.HomeContentInset
import com.playder.ui.component.HomeCardHighlight
import com.playder.ui.component.HomeCoverCard
import com.playder.ui.component.homeRowHeight
import com.playder.ui.component.HomeSectionBones
import com.playder.ui.component.HeroCarouselBones
import com.playder.ui.launch.launchMarkTarget
import com.playder.ui.launch.launchRise
import com.playder.ui.launch.rememberLaunchHold
import com.playder.ui.launch.ReportLaunchHomeComposed
import com.playder.ui.component.HomeSectionTitle
import com.playder.ui.component.HomeTrackRow
import com.playder.ui.component.HomeVideoCard
import com.playder.utils.ArtistVideos
import com.playder.utils.FeedVideoKind
import com.playder.utils.HomeVideo
import com.playder.viewmodels.HomeCategory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.flow.drop
import com.playder.ui.component.LikedSongsThumbnail
import com.playder.ui.component.LocalBottomSheetPageState
import com.playder.ui.component.LocalMenuState
import com.playder.ui.component.PlaylistThumbnail
import com.playder.ui.component.RevealSeenState
import com.playder.ui.component.containerTransformSource
import com.playder.ui.component.rememberRevealSeenState
import com.playder.ui.component.revealOnAppear
import com.playder.ui.menu.AlbumMenu
import com.playder.ui.menu.ArtistMenu
import com.playder.ui.menu.SongMenu
import com.playder.ui.menu.YouTubeAlbumMenu
import com.playder.ui.menu.YouTubeArtistMenu
import com.playder.ui.menu.YouTubePlaylistMenu
import com.playder.ui.menu.YouTubeSongMenu
import com.playder.ui.utils.SnapLayoutInfoProvider
import com.playder.ui.utils.resize
import com.playder.utils.joinByBullet
import com.playder.utils.rememberPreference
import com.playder.viewmodels.CommunityPlaylistItem
import com.playder.viewmodels.HomeViewModel
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.playder.ui.component.navigateToTab

sealed class HomeSection(val id: String) {
    data object RecentlyPlayed : HomeSection("recently_played")
    data object MostPlayed : HomeSection("most_played")
    data object History : HomeSection("history")
    data object DontForgetTheseSongs : HomeSection("dont_forget_these_songs")
    data object AccountPlaylists : HomeSection("account_playlists")
    data object SpeedDial : HomeSection("speed_dial")
    data object FromTheCommunity : HomeSection("from_the_community")
    data object ArtistList : HomeSection("artist_list")
    data object AllTimeHits : HomeSection("all_time_hits")
    data object LiveShows : HomeSection("live_shows")
    data object MusicVideos : HomeSection("music_videos")
    data class Category(val categoryId: String) : HomeSection("category_$categoryId")

    data class HomePageSection(val index: Int, val title: String) : HomeSection("home_page_section_${index}_$title")
}

/** a Home row that fades and slides up the first time it scrolls into view */
private fun LazyListScope.homeItem(
    key: String,
    revealSeen: RevealSeenState,
    contentType: String,
    content: @Composable () -> Unit,
) {
    item(key = key, contentType = contentType) {
        Box(modifier = Modifier.revealOnAppear(key, revealSeen, cascade = true)) {
            content()
        }
    }
}

// a row scrolling into view prefetches 2 cards by default, but 3 to 4 are showing, so the rest composed in the frame that revealed them
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun rememberCardRowState(): LazyListState =
    rememberLazyListState(prefetchStrategy = remember { LazyListPrefetchStrategy(nestedPrefetchItemCount = 4) })

// same for the song grids, which show about two columns of three rows
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun rememberTrackGridState(): LazyGridState =
    rememberLazyGridState(prefetchStrategy = remember { LazyGridPrefetchStrategy(nestedPrefetchItemCount = 6) })

/** a horizontally scrolling row of cards, lined up with the section title. cards cascade in once. */
@Composable
private fun <T> HomeCardRow(
    rowKey: String,
    items: List<T>,
    revealSeen: RevealSeenState,
    key: (T) -> Any,
    content: @Composable (T) -> Unit,
) {
    LazyRow(
        state = rememberCardRowState(),
        contentPadding = PaddingValues(horizontal = HomeContentInset),
        horizontalArrangement = Arrangement.spacedBy(HomeCardSpacing),
    ) {
        itemsIndexed(items = items, key = { _, item -> key(item) }) { index, item ->
            Box(
                modifier = Modifier.revealOnAppear(
                    key = "$rowKey/${key(item)}",
                    seenState = revealSeen,
                    delayMillis = min(index, 4) * 60,
                )
            ) {
                content(item)
            }
        }
    }
}

/** the paged "list" and "Rank" sections: columns of three song rows */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T> HomeTrackGrid(
    items: List<T>,
    state: LazyGridState,
    rowSpacing: Dp,
    columnWidth: Dp,
    key: (Int, T) -> Any,
    content: @Composable (index: Int, item: T) -> Unit,
) {
    val rows = min(3, items.size)
    val snapLayoutInfoProvider = remember(state) {
        SnapLayoutInfoProvider(lazyGridState = state, positionInLayout = { _, _ -> 0f })
    }
    LazyHorizontalGrid(
        state = state,
        rows = GridCells.Fixed(rows),
        flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
        contentPadding = PaddingValues(horizontal = HomeContentInset),
        horizontalArrangement = Arrangement.spacedBy(39.dp),
        verticalArrangement = Arrangement.spacedBy(rowSpacing),
        modifier = Modifier
            .fillMaxWidth()
            .height(homeRowHeight() * rows + rowSpacing * (rows - 1)),
    ) {
        itemsIndexed(items = items, key = key) { index, item ->
            Box(modifier = Modifier.width(columnWidth)) {
                content(index, item)
            }
        }
    }
}

@Composable
fun CommunityPlaylistCard(
    item: CommunityPlaylistItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var extractedColors by remember { mutableStateOf<List<Color>>(emptyList()) }

    LaunchedEffect(item.songs) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val thumbnails = item.songs.take(3).mapNotNull { it.thumbnail }
                val colorsList = mutableListOf<Color>()
                for (thumb in thumbnails) {
                    val request = ImageRequest.Builder(context)
                        .data(thumb)
                        .size(100, 100)
                        .allowHardware(false)
                        .build()
                    val result = context.imageLoader.execute(request)
                    val bitmap = result.image?.toBitmap()

                    if (bitmap != null) {
                        val palette = Palette.from(bitmap)
                            .maximumColorCount(4)
                            .resizeBitmapArea(100 * 100)
                            .generate()
                        val colors = PlayerColorExtractor.extractGradientColors(
                            palette = palette,
                            fallbackColor = android.graphics.Color.DKGRAY
                        )
                        colorsList.addAll(colors.take(2))
                    }
                }
                if (colorsList.isNotEmpty()) {
                    extractedColors = colorsList.distinct().take(3)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Card(
        modifier = modifier
            .width(276.dp)
            .height(344.dp)
            .containerTransformSource("playlist-${item.playlist.id}", coverUrl = item.playlist.thumbnail)
            .clip(RoundedCornerShape(HomeCardCornerRadius)),
        shape = RoundedCornerShape(HomeCardCornerRadius),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val premiumDarkColors = listOf(
                listOf(Color(0xFF2B1B38), Color(0xFF151521), Color.Black),
                listOf(Color(0xFF1B2838), Color(0xFF0F1521), Color.Black),
                listOf(Color(0xFF381B24), Color(0xFF1F0F15), Color.Black),
                listOf(Color(0xFF1B382D), Color(0xFF0F1F17), Color.Black),
                listOf(Color(0xFF2D2B55), Color(0xFF151521), Color.Black)
            )
            val fallbackColors = premiumDarkColors[(item.playlist.id.hashCode() and 0x7FFFFFFF) % premiumDarkColors.size]

            val color1 by animateColorAsState(
                targetValue = extractedColors.getOrNull(0)?.let {
                    Color(androidx.core.graphics.ColorUtils.blendARGB(it.toArgb(), android.graphics.Color.BLACK, 0.65f))
                } ?: fallbackColors[0],
                animationSpec = tween(800), label = ""
            )
            val color2 by animateColorAsState(
                targetValue = extractedColors.getOrNull(1)?.let {
                    Color(androidx.core.graphics.ColorUtils.blendARGB(it.toArgb(), android.graphics.Color.BLACK, 0.75f))
                } ?: fallbackColors[1],
                animationSpec = tween(800), label = ""
            )
            val color3 by animateColorAsState(
                targetValue = extractedColors.getOrNull(2)?.let {
                    Color(androidx.core.graphics.ColorUtils.blendARGB(it.toArgb(), android.graphics.Color.BLACK, 0.85f))
                } ?: fallbackColors[2],
                animationSpec = tween(800), label = ""
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(color1, color2, color3)
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = 35f
                            scaleX = 1.3f
                            scaleY = 1.3f
                            translationX = 40.dp.toPx()
                            translationY = -30.dp.toPx()
                        }
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item.songs.take(3).forEach { song ->
                                AsyncImage(
                                    model = song.thumbnail.resize(256, 256),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(84.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item.songs.drop(3).take(2).forEach { song ->
                                AsyncImage(
                                    model = song.thumbnail.resize(256, 256),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(84.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f),
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 28.dp, end = 16.dp, top = 16.dp)
            ) {
                Text(
                    text = item.playlist.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = item.playlist.songCountText ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AllTimeHitsCard(
    item: YTItem,
    modifier: Modifier = Modifier,
) {
    val artistName = when (item) {
        is SongItem -> item.artists.joinToString(", ") { it.name }
        is AlbumItem -> item.artists?.joinToString(", ") { it.name } ?: ""
        is ArtistItem -> item.title
        is PlaylistItem -> item.author?.name ?: ""
    }

    Box(
        modifier = modifier
            .height(160.dp)
            .clip(RoundedCornerShape(HomeCardCornerRadius))
    ) {
        AsyncImage(
            model = item.thumbnail?.resize(320, 320),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                    )
                )
        )
        Text(
            text = artistName,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
private val HomeMarkWidth = 31.dp
private val HomeMarkHeight = 34.dp
private val HomeTopBarRise = 16.dp

/**
 * Home before the player service is up: the top bar, hero and bones the real Home shows while it
 * loads, in exactly the same places, so the swap to the real thing can't be seen. On a launch the
 * intro lands on this logo when it's the first one laid out.
 */
@Composable
private fun HomeShell() {
    val profilePicUri by rememberPreference(ProfilePicUriKey, "")
    val profileImageUrl = profilePicUri
        .takeIf { it.isNotBlank() }
        ?.let { if (it.contains("://")) it else "file://$it" }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            HeroCarouselBones(modifier = Modifier.launchRise(order = 1))
            BonesHost(modifier = Modifier.launchRise(order = 2)) {
                repeat(2) {
                    HomeSectionBones()
                    Spacer(modifier = Modifier.height(34.dp))
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = WindowInsets.stableSystemBars.asPaddingValues().calculateTopPadding() + 14.dp,
                    bottom = 16.dp,
                    start = HomeContentInset,
                    end = 30.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_playder_mark),
                contentDescription = "Playder",
                tint = Color.White,
                modifier = Modifier
                    .launchMarkTarget()
                    .size(HomeMarkWidth, HomeMarkHeight)
            )
            Box(
                modifier = Modifier
                    .launchRise(order = 0, distance = HomeTopBarRise)
                    .height(44.dp),
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUrl != null) {
                    AsyncImage(
                        model = profileImageUrl,
                        placeholder = painterResource(R.drawable.person),
                        error = painterResource(R.drawable.person),
                        contentDescription = androidx.compose.ui.res.stringResource(R.string.profile_accessibility_description),
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.person),
                            contentDescription = androidx.compose.ui.res.stringResource(R.string.profile_accessibility_description),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val bottomSheetPageState = LocalBottomSheetPageState.current
    val playerBottomSheetState = com.playder.ui.component.LocalPlayerBottomSheetState.current
    // the player service comes up a beat after the first frame; Home's skeleton doesn't wait for it
    val playerConnection = LocalPlayerConnection.current ?: run {
        HomeShell()
        return
    }
    val haptic = LocalHapticFeedback.current

    // read only where they're used - inside each card, track row and the hero - never out here: a
    // play/pause or a new song used to recompose the whole of Home, every row and card on screen,
    // which landed as a hitch whenever the track changed mid-scroll
    val isPlayingState = playerConnection.isEffectivelyPlaying.collectAsState()
    val mediaMetadataState = playerConnection.mediaMetadata.collectAsState()
    // handed to the cards as a lambda so only the active cover's indicator reads it
    val isPlayingNow: () -> Boolean = remember(isPlayingState) { { isPlayingState.value } }

    val forgottenFavorites by viewModel.forgottenFavorites.collectAsState()
    val keepListening by viewModel.keepListening.collectAsState()
    val accountPlaylists by viewModel.accountPlaylists.collectAsState()
    val localPlaylists by viewModel.localPlaylists.collectAsState()
    val libraryArtists by viewModel.libraryArtists.collectAsState()
    // playlists the user made (or imported), put forward in the hero; Play loads and queues them
    val heroScope = rememberCoroutineScope()
    val playlistPicks = remember(localPlaylists) {
        localPlaylists.orEmpty()
            .filter { it.playlist.isEditable && it.songCount > 0 && it.id != "liked" }
            .shuffled()
            .take(2)
            .map { playlist ->
                com.playder.ui.component.PlaylistPickItem(
                    id = playlist.id,
                    name = playlist.playlist.name,
                    thumbnailUrl = playlist.playlist.thumbnailUrl ?: playlist.thumbnails.firstOrNull(),
                ) {
                    heroScope.launch(Dispatchers.IO) {
                        val songs = viewModel.database.playlistSongs(playlist.id).first()
                            .sortedBy { it.map.position }
                            .map { it.song.toMediaItem() }
                        if (songs.isEmpty()) return@launch
                        withContext(Dispatchers.Main) {
                            playerConnection.playQueue(ListQueue(title = playlist.playlist.name, items = songs))
                        }
                    }
                }
            }
    }
    // a new install's hero: what was just imported, or else a song from each artist picked in
    // onboarding (the artists themselves if their songs couldn't be fetched)
    val starterSongs by viewModel.starterSongs.collectAsState()
    val starterItems = remember(localPlaylists, libraryArtists, starterSongs) {
        val imported = localPlaylists.orEmpty()
            .filter { it.songCount > 0 && it.id != "liked" }
            .take(5)
            .map {
                com.playder.ui.component.StarterItem(
                    kind = com.playder.ui.component.StarterItem.Kind.Playlist,
                    id = it.id,
                    title = it.playlist.name,
                    detail = "${it.songCount} songs",
                    thumbnailUrl = it.thumbnails.firstOrNull() ?: it.playlist.thumbnailUrl,
                )
            }
        val songs = starterSongs
        imported.ifEmpty {
            when {
                // still being asked for; the hero holds its bones meanwhile
                songs == null -> emptyList()
                songs.isNotEmpty() -> songs.map {
                    com.playder.ui.component.StarterSongItem(it.song, it.artistName, it.artistThumbnail)
                }
                else -> libraryArtists.map {
                    com.playder.ui.component.StarterItem(
                        kind = com.playder.ui.component.StarterItem.Kind.Artist,
                        id = it.id,
                        title = it.artist.name,
                        detail = "",
                        thumbnailUrl = it.artist.thumbnailUrl,
                    )
                }
            }
        }
    }
    val homePage by viewModel.homePage.collectAsState()
    val dailyDiscover by viewModel.dailyDiscover.collectAsState()
    val similarRecommendations by viewModel.similarRecommendations.collectAsState()
    val communityPlaylists by viewModel.communityPlaylists.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val mostPlayedSongsForHome by viewModel.mostPlayedSongsForHome.collectAsState()
    val recentHistorySongs by viewModel.recentHistorySongs.collectAsState()
    val artistListItems by viewModel.artistListItems.collectAsState()
    val allTimeHits by viewModel.allTimeHits.collectAsState()
    val liveRow by viewModel.liveRow.collectAsState()
    val musicVideoRow by viewModel.musicVideoRow.collectAsState()
    val feedVideoKinds by viewModel.feedVideoKinds.collectAsState()
    val homeCategories by viewModel.homeCategories.collectAsState()

    val speedDialItems by viewModel.speedDialItems.collectAsState()
    val lastPlayedSong by viewModel.lastPlayedSong.collectAsState()

    val isLoading: Boolean by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    // keepListening stays null until the first local load lands, which also covers the moment
    // before load() has flipped isLoading on
    // right after a launch Home shows its bones through the intro and reveals its content after
    val launchHold = rememberLaunchHold()
    ReportLaunchHomeComposed()
    val isFirstLoad = isLoading || keepListening == null || launchHold
    val revealSeen = rememberRevealSeenState()

    // the gacha's reel: songs you play most, favourites you've drifted from, recommendations and
    // recent plays, all mixed together (only ones with a cover, so the reel never shows a blank)
    val gachaPool = remember(mostPlayedSongsForHome, forgottenFavorites, dailyDiscover, recentHistorySongs, keepListening) {
        buildList {
            mostPlayedSongsForHome?.forEach { add(it.toMediaMetadata()) }
            forgottenFavorites?.forEach { add(it.toMediaMetadata()) }
            dailyDiscover?.forEach { discover -> (discover.recommendation as? SongItem)?.let { add(it.toMediaMetadata()) } }
            recentHistorySongs?.forEach { add(it.toMediaMetadata()) }
            keepListening?.filterIsInstance<Song>()?.forEach { add(it.toMediaMetadata()) }
        }
            .filter { !it.thumbnailUrl.isNullOrEmpty() }
            .distinctBy { it.id }
    }

    val quickPicksLazyGridState = rememberTrackGridState()
    val forgottenFavoritesLazyGridState = rememberTrackGridState()

    val accountName by viewModel.accountName.collectAsState()
    val accountImageUrl by viewModel.accountImageUrl.collectAsState()
    val profilePicUri by rememberPreference(ProfilePicUriKey, "")

    val localProfileImageUrl = profilePicUri
        .takeIf { it.isNotBlank() }
        ?.let { if (it.contains("://")) it else "file://$it" }
    val profileImageUrl = localProfileImageUrl ?: accountImageUrl
    val context = LocalContext.current
    val profileImageRequest = remember(context, profileImageUrl) {
        profileImageUrl?.let { imageUrl ->
            ImageRequest.Builder(context)
                .data(imageUrl)
                .diskCachePolicy(CachePolicy.ENABLED)
                .diskCacheKey(imageUrl)
                .build()
        }
    }


    val scope = rememberCoroutineScope()

    val lazylistState = rememberLazyListState()

    val configuration = LocalConfiguration.current
    val carouselHeightDp = (configuration.screenHeightDp * HeroHeightFraction).dp
    val carouselHeightPx = with(LocalDensity.current) { carouselHeightDp.toPx() }

    val firstItemScrollOffset by remember {
        derivedStateOf {
            if (lazylistState.firstVisibleItemIndex == 0) {
                lazylistState.firstVisibleItemScrollOffset.toFloat()
            } else {
                carouselHeightPx
            }
        }
    }
    val heroScrollProgress by remember {
        derivedStateOf {
            (firstItemScrollOffset / carouselHeightPx).coerceIn(0f, 1f)
        }
    }

    val heroScrollProgressProvider = remember { { heroScrollProgress } }
    // the glass layer only switches on or off once, so reading the raw progress would redraw the list on every scroll frame
    val heroGlassActive = remember { derivedStateOf { heroScrollProgress > 0f } }

    // Home's own entry, the one a tap on its tab flags (onTabTapped only does that while Home is the
    // current page). Following the current entry instead recomposed all of Home on every navigation,
    // right in the heavy first frame of each page opening over it
    val backStackEntry = androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner.current
        as? androidx.navigation.NavBackStackEntry
    val scrollToTop = remember(backStackEntry) {
        backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)
    }?.collectAsState()

    LaunchedEffect(scrollToTop?.value) {
        if (scrollToTop?.value == true) {
            lazylistState.animateScrollToItem(0)
            backStackEntry?.savedStateHandle?.set("scrollToTop", false)
        }
    }

    LaunchedEffect(Unit) {
        // keyed on the page token as well as the scroll position: a short feed never scrolls, so
        // when the fresh token replaced the stale cached one nothing asked for the next page and
        // Home stopped after a section or two
        snapshotFlow {
            val layoutInfo = lazylistState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            homePage?.continuation?.takeIf { lastVisibleIndex >= layoutInfo.totalItemsCount - 3 }
        }
            .distinctUntilChanged()
            .collect { continuation ->
                if (continuation != null) viewModel.loadMoreYouTubeItems(continuation)
            }
    }

    NetworkReload(
        onReload = viewModel::refresh
    )

    // no pull to refresh: Home refreshes itself from what you play. the view model follows the
    // player for skips, and decides whether a refresh is due when the player is closed or Home
    // comes back into view
    LaunchedEffect(playerConnection) { viewModel.attachPlayer(playerConnection) }
    LaunchedEffect(playerBottomSheetState) {
        val sheet = playerBottomSheetState ?: return@LaunchedEffect
        snapshotFlow { sheet.isExpanded }
            .distinctUntilChanged()
            .drop(1)
            .collect { expanded -> if (!expanded) viewModel.onListeningBreak() }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onListeningBreak() }

    val playSong: (Song) -> Unit = { song ->
        if (song.id == mediaMetadataState.value?.id) {
            playerConnection.togglePlayPause()
        } else {
            playerConnection.playQueue(YouTubeQueue.radio(song.toMediaMetadata()))
        }
    }
    val showSongMenu: (Song) -> Unit = { song ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        menuState.show {
            SongMenu(originalSong = song, navController = navController, onDismiss = menuState::dismiss)
        }
    }
    val openLocalPlaylist: (String) -> Unit = { id ->
        if (id == "liked") navController.navigate("auto_playlist/liked") else navController.navigate("local_playlist/$id")
    }

    // every square card on Home that comes from the library
    val localItemCard: @Composable (LocalItem) -> Unit = { localItem ->
        val (onClick, onLongClick) = when (localItem) {
            is Song -> Pair<() -> Unit, () -> Unit>({ playSong(localItem) }, { showSongMenu(localItem) })
            is Album -> Pair<() -> Unit, () -> Unit>(
                { navController.navigate("album/${localItem.id}") },
                {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    menuState.show {
                        AlbumMenu(originalAlbum = localItem, navController = navController, onDismiss = menuState::dismiss)
                    }
                }
            )
            is Artist -> Pair<() -> Unit, () -> Unit>(
                { navController.navigate("artist/${localItem.id}") },
                {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    menuState.show {
                        ArtistMenu(originalArtist = localItem, coroutineScope = scope, onDismiss = menuState::dismiss)
                    }
                }
            )
            is Playlist -> Pair<() -> Unit, () -> Unit>(
                { openLocalPlaylist(localItem.id) },
                { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
            )
        }
        val playlist = localItem as? Playlist
        HomeCoverCard(
            title = localItem.title,
            subtitle = when (localItem) {
                is Song -> localItem.artists.joinToString(", ") { it.name }
                is Album -> localItem.artists.joinToString(", ") { it.name }
                is Playlist -> pluralStringResource(R.plurals.n_song, localItem.songCount, localItem.songCount)
                is Artist -> null
            },
            // a library playlist's own thumbnailUrl is always null; its covers live in thumbnails
            thumbnailUrl = playlist?.thumbnails?.firstOrNull() ?: localItem.thumbnailUrl,
            circular = localItem is Artist,
            isActive = when (localItem) {
                is Song -> localItem.id == mediaMetadataState.value?.id
                is Album -> localItem.id == mediaMetadataState.value?.album?.id
                else -> false
            },
            isPlaying = isPlayingNow,
            sharedElementKey = when (localItem) {
                is Album -> "album-${localItem.id}"
                is Playlist -> "playlist-${localItem.id}"
                else -> null
            },
            cover = when {
                playlist == null -> null
                playlist.id == "liked" -> { size ->
                    LikedSongsThumbnail(size = size, shape = RoundedCornerShape(HomeCardCornerRadius))
                }
                else -> { size ->
                    PlaylistThumbnail(
                        thumbnails = playlist.thumbnails,
                        size = size,
                        shape = RoundedCornerShape(HomeCardCornerRadius),
                        placeHolder = {
                            Icon(
                                painter = painterResource(R.drawable.queue_music),
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(size / 3)
                            )
                        },
                    )
                }
            },
            modifier = Modifier.combinedClickable(
                interactionSource = null,
                indication = HomeCardHighlight,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        )
    }

    // the same card for anything that came from YouTube
    val ytItemCard: @Composable (YTItem) -> Unit = { item ->
        HomeCoverCard(
            title = item.title,
            subtitle = when (item) {
                is SongItem -> item.artists.joinToString(", ") { it.name }
                is AlbumItem -> joinByBullet(item.artists?.joinToString(", ") { it.name }, item.year?.toString())
                is ArtistItem -> null
                is PlaylistItem -> joinByBullet(item.author?.name, item.songCountText)
            },
            thumbnailUrl = item.thumbnail,
            circular = item is ArtistItem,
            isActive = item.id in listOf(mediaMetadataState.value?.album?.id, mediaMetadataState.value?.id),
            isPlaying = isPlayingNow,
            sharedElementKey = when (item) {
                is AlbumItem -> "album-${item.id}"
                is PlaylistItem -> "playlist-${item.id}"
                else -> null
            },
            modifier = Modifier.combinedClickable(
                interactionSource = null,
                indication = HomeCardHighlight,
                onClick = {
                    when (item) {
                        is SongItem -> playerConnection.playQueue(
                            YouTubeQueue(item.endpoint ?: WatchEndpoint(videoId = item.id), item.toMediaMetadata())
                        )
                        is AlbumItem -> navController.navigate("album/${item.id}")
                        is ArtistItem -> navController.navigate("artist/${item.id}")
                        is PlaylistItem -> when {
                            item.id == "liked" -> navController.navigate("auto_playlist/liked")
                            item.author?.name == "Local Playlist" -> navController.navigate("local_playlist/${item.id}")
                            else -> navController.navigate("online_playlist/${item.id}")
                        }
                    }
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    menuState.show {
                        when (item) {
                            is SongItem -> YouTubeSongMenu(
                                song = item,
                                navController = navController,
                                onDismiss = menuState::dismiss
                            )
                            is AlbumItem -> YouTubeAlbumMenu(
                                albumItem = item,
                                navController = navController,
                                onDismiss = menuState::dismiss
                            )
                            is ArtistItem -> YouTubeArtistMenu(
                                artist = item,
                                onDismiss = menuState::dismiss
                            )
                            is PlaylistItem -> YouTubePlaylistMenu(
                                playlist = item,
                                coroutineScope = scope,
                                onDismiss = menuState::dismiss,
                                onImportedPlaylist = { playlistId ->
                                    navController.navigate("local_playlist/$playlistId")
                                }
                            )
                        }
                    }
                }
            ),
        )
    }

    // the wide banner for every video row: Live Shows, Music Videos and YouTube's other video shelves.
    // a video plays as a video; the player shows it unless that's switched off in General settings
    val videoCard: @Composable (HomeVideo, Dp) -> Unit = { video, width ->
        HomeVideoCard(
            title = video.title,
            subtitle = video.subtitle,
            videoId = video.item.id,
            fallbackThumbnailUrl = video.item.thumbnail,
            width = width,
            isActive = video.item.id == mediaMetadataState.value?.id,
            isPlaying = isPlayingNow,
            modifier = Modifier.combinedClickable(
                interactionSource = null,
                indication = HomeCardHighlight,
                onClick = {
                    if (video.item.id == mediaMetadataState.value?.id) {
                        playerConnection.togglePlayPause()
                    } else {
                        playerConnection.playQueue(
                            YouTubeQueue(
                                video.item.endpoint ?: WatchEndpoint(videoId = video.item.id),
                                video.item.toMediaMetadata()
                            )
                        )
                    }
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    menuState.show {
                        YouTubeSongMenu(
                            song = video.item,
                            navController = navController,
                            onDismiss = menuState::dismiss
                        )
                    }
                },
            ),
        )
    }

    // each row's card is written once up here and passed straight in: a fresh lambda at the call site
    // makes every composed card recompose whenever the feed list updates, e.g. while loading more
    val communityCard: @Composable (CommunityPlaylistItem) -> Unit = { item ->
        CommunityPlaylistCard(
            item = item,
            onClick = { navController.navigate("online_playlist/${item.playlist.id}") }
        )
    }

    val artistGroupCard: @Composable (ArtistGroup) -> Unit = { group ->
        ArtistListCard(
            group = group,
            onClick = {
                group.artistId?.let {
                    com.playder.utils.ArtistPageCache.putPicks(it, group)
                    navController.navigate("artist/$it/similar")
                }
            },
            onItemClick = { item ->
                when (item) {
                    is SongItem -> playerConnection.playQueue(
                        YouTubeQueue(
                            item.endpoint ?: WatchEndpoint(videoId = item.id),
                            item.toMediaMetadata()
                        )
                    )
                    is AlbumItem -> navController.navigate("album/${item.id}")
                    is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                    else -> {}
                }
            }
        )
    }

    val hitsCard: @Composable (YTItem) -> Unit = { item ->
        AllTimeHitsCard(
            item = item,
            modifier = Modifier
                .width(160.dp)
                .combinedClickable(
                    onClick = {
                        when (item) {
                            is SongItem -> playerConnection.playQueue(
                                YouTubeQueue(
                                    item.endpoint ?: WatchEndpoint(videoId = item.id),
                                    item.toMediaMetadata()
                                )
                            )
                            is AlbumItem -> navController.navigate("album/${item.id}")
                            is ArtistItem -> navController.navigate("artist/${item.id}")
                            is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                )
        )
    }

    // the song rows of the paged grids: Most Played numbers them, History and Don't Forget don't
    val rankedTrackRow: @Composable (Int, Song) -> Unit = { index, song ->
        HomeTrackRow(
            thumbnailUrl = song.song.thumbnailUrl,
            songId = song.id,
            title = song.song.title,
            subtitle = song.artists.joinToString(", ") { it.name },
            isActive = song.id == mediaMetadataState.value?.id,
            isPlaying = isPlayingNow,
            rank = index + 1,
            onClick = { playSong(song) },
            onLongClick = { showSongMenu(song) },
            onMoreClick = { showSongMenu(song) },
        )
    }

    val trackRow: @Composable (Int, Song) -> Unit = { _, song ->
        HomeTrackRow(
            thumbnailUrl = song.song.thumbnailUrl,
            songId = song.id,
            title = song.song.title,
            subtitle = song.artists.joinToString(", ") { it.name },
            isActive = song.id == mediaMetadataState.value?.id,
            isPlaying = isPlayingNow,
            onClick = { playSong(song) },
            onLongClick = { showSongMenu(song) },
            onMoreClick = { showSongMenu(song) },
        )
    }

    val homeSections by remember {
        derivedStateOf {
            val list = mutableListOf<HomeSection>()

            // the recommendation rows are dealt out between Home's own rows, the way Apple Music
            // and Spotify mix "because you like" shelves in with your history
            val categories = homeCategories.orEmpty()
            var nextCategory = 0
            fun addCategories(count: Int = 1) = repeat(count) {
                categories.getOrNull(nextCategory)?.let {
                    list.add(HomeSection.Category(it.id))
                    nextCategory++
                }
            }

            if (recentlyPlayed?.isNotEmpty() == true) list.add(HomeSection.RecentlyPlayed)
            if (mostPlayedSongsForHome?.isNotEmpty() == true) list.add(HomeSection.MostPlayed)
            addCategories()
            if (communityPlaylists?.isNotEmpty() == true) list.add(HomeSection.FromTheCommunity)
            if (speedDialItems.isNotEmpty()) list.add(HomeSection.SpeedDial)
            if (recentHistorySongs?.isNotEmpty() == true) list.add(HomeSection.History)
            addCategories()
            // the two video banners sit apart, so the big frames break the feed up instead of piling up
            if (liveRow?.isNotEmpty() == true) list.add(HomeSection.LiveShows)
            addCategories()
            if (artistListItems?.isNotEmpty() == true) list.add(HomeSection.ArtistList)
            addCategories()
            if (accountPlaylists?.isNotEmpty() == true || localPlaylists?.isNotEmpty() == true) list.add(HomeSection.AccountPlaylists)
            addCategories()
            if (forgottenFavorites?.isNotEmpty() == true) list.add(HomeSection.DontForgetTheseSongs)
            addCategories()
            if (musicVideoRow?.isNotEmpty() == true) list.add(HomeSection.MusicVideos)
            addCategories(categories.size)

            // YouTube's live and music video shelves already sit inside the rows above
            val homePageSections = homePage?.sections.orEmpty()
                .mapIndexed { index, section -> index to section }
                .filter { (index, _) ->
                    feedVideoKinds[index] != FeedVideoKind.Live && feedVideoKinds[index] != FeedVideoKind.MusicVideos
                }
            homePageSections.firstOrNull()?.let { (index, section) -> list.add(HomeSection.HomePageSection(index, section.title)) }
            if (allTimeHits?.isNotEmpty() == true) list.add(HomeSection.AllTimeHits)
            homePageSections.drop(1).forEach { (index, section) ->
                list.add(HomeSection.HomePageSection(index, section.title))
            }

            list
        }
    }

    LaunchedEffect(mostPlayedSongsForHome) {
        quickPicksLazyGridState.scrollToItem(0)
    }

    LaunchedEffect(forgottenFavorites) {
        forgottenFavoritesLazyGridState.scrollToItem(0)
    }

    val baseTypography = MaterialTheme.typography
    val homeTypography = remember(baseTypography) {
        val tightenSp = 0.6f
        fun tighten(style: androidx.compose.ui.text.TextStyle) =
            style.copy(letterSpacing = (style.letterSpacing.value - tightenSp).sp)
        baseTypography.copy(
            titleLarge = tighten(baseTypography.titleLarge),
            titleMedium = tighten(baseTypography.titleMedium),
            titleSmall = tighten(baseTypography.titleSmall),
            bodyLarge = tighten(baseTypography.bodyLarge),
            bodyMedium = tighten(baseTypography.bodyMedium),
            bodySmall = tighten(baseTypography.bodySmall),
            labelLarge = tighten(baseTypography.labelLarge),
            labelMedium = tighten(baseTypography.labelMedium),
            labelSmall = tighten(baseTypography.labelSmall),
        )
    }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        shapes = MaterialTheme.shapes,
        typography = homeTypography,
    ) {
    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart
        ) {
            // a song column is 304 of 401 in the Figma, so the next one peeks in from the edge
            val trackColumnWidth = if (maxWidth * 0.475f >= 320.dp) maxWidth * 0.475f else maxWidth * 0.76f
            // a video banner is 314 of 402, so the next one peeks in the same way
            val videoCardWidth = minOf(maxWidth * 0.78f, 340.dp)
            val videoRowCard: @Composable (HomeVideo) -> Unit = { video -> videoCard(video, videoCardWidth) }

            val homeGlassState = remember { GlassState() }

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val backgroundColor = Color.Black
                CompositionLocalProvider(
                    com.playder.ui.component.LocalGridItemPadding provides 0.dp
                ) {
                    LazyColumn(
                        modifier = Modifier.glassRoot(
                            homeGlassState,
                            isActive = { heroGlassActive.value }
                        ),
                        state = lazylistState,
                        contentPadding = PaddingValues(
                            bottom = LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateBottomPadding()
                        )
                    ) {
                item(key = "hero", contentType = "hero") {
                    HeroCarousel(
                        keepListening = keepListening,
                        lastPlayedSong = mediaMetadataState.value ?: lastPlayedSong,
                        dailyDiscover = dailyDiscover,
                        playerConnection = playerConnection,
                        navController = navController,
                        heroScrollProgressProvider = heroScrollProgressProvider,
                        isLoading = isFirstLoad || starterSongs == null,
                        hold = launchHold,
                        gachaPool = gachaPool,
                        starterItems = starterItems,
                        playlistPicks = playlistPicks,
                        similarRecommendations = similarRecommendations,
                        onRepeat = mostPlayedSongsForHome,
                        forgottenFavorites = forgottenFavorites,
                        modifier = Modifier.launchRise(order = 1),
                    )
                }

                // nothing but bones while the launch intro plays; the rows reveal once it lands
                if (!launchHold) homeSections.forEach { section ->
                    when (section) {
                        HomeSection.SpeedDial -> {
                            speedDialItems.takeIf { it.isNotEmpty() }?.let { items ->
                                homeItem("speed_dial_title", revealSeen, "title") {
                                    HomeSectionTitle(
                                        title = stringResource(R.string.home_speed_dial),
                                        onClick = { navController.navigate("section_detail/speed_dial") }
                                    )
                                }
                                homeItem("speed_dial_list", revealSeen, "cardRow") {
                                    HomeCardRow("speed_dial", items, revealSeen, key = { it.id }, content = ytItemCard)
                                }
                            }
                        }
                        HomeSection.RecentlyPlayed -> {
                            recentlyPlayed?.takeIf { it.isNotEmpty() }?.let { items ->
                                homeItem("recently_played_title", revealSeen, "title") {
                                    HomeSectionTitle(
                                        title = stringResource(R.string.home_recently_played),
                                        onClick = { navController.navigate("section_detail/recently_played") }
                                    )
                                }
                                homeItem("recently_played_list", revealSeen, "cardRow") {
                                    HomeCardRow("recently_played", items, revealSeen, key = { it.id }, content = localItemCard)
                                }
                            }
                        }
                        HomeSection.MostPlayed -> {
                            mostPlayedSongsForHome?.takeIf { it.isNotEmpty() }?.let { mostPlayed ->
                                homeItem("most_played_title", revealSeen, "title") {
                                    val mostPlayedTitle = stringResource(R.string.home_most_played)
                                    HomeSectionTitle(
                                        title = mostPlayedTitle,
                                        onClick = { navController.navigate("section_detail/most_played") },
                                        onPlayAllClick = {
                                            playerConnection.playQueue(
                                                ListQueue(
                                                    title = mostPlayedTitle,
                                                    items = mostPlayed.map { it.toMediaItem() }
                                                )
                                            )
                                        }
                                    )
                                }
                                homeItem("most_played_list", revealSeen, "trackGrid") {
                                    HomeTrackGrid(
                                        items = mostPlayed,
                                        state = quickPicksLazyGridState,
                                        rowSpacing = 6.dp,
                                        columnWidth = trackColumnWidth,
                                        key = { _, song -> song.id },
                                        content = rankedTrackRow,
                                    )
                                }
                            }
                        }
                        HomeSection.History -> {
                            recentHistorySongs?.takeIf { it.isNotEmpty() }?.let { history ->
                                homeItem("history_title", revealSeen, "title") {
                                    HomeSectionTitle(
                                        title = stringResource(R.string.home_history_title),
                                        onClick = { navController.navigate("history") }
                                    )
                                }
                                homeItem("history_list", revealSeen, "trackGrid") {
                                    HomeTrackGrid(
                                        items = history,
                                        state = rememberTrackGridState(),
                                        rowSpacing = 6.dp,
                                        columnWidth = trackColumnWidth,
                                        key = { index, song -> "history_${index}_${song.id}" },
                                        content = trackRow,
                                    )
                                }
                            }
                        }
                        HomeSection.FromTheCommunity -> {
                            communityPlaylists?.takeIf { it.isNotEmpty() }?.let { playlists ->
                                homeItem("community_playlists_title", revealSeen, "title") {
                                    HomeSectionTitle(
                                        title = stringResource(R.string.from_the_community),
                                        onClick = { navController.navigate("section_detail/from_the_community") }
                                    )
                                }
                                homeItem("community_playlists_content", revealSeen, "cardRow") {
                                    HomeCardRow("community_playlists", playlists, revealSeen, key = { it.playlist.id }, content = communityCard)
                                }
                            }
                        }
                        HomeSection.ArtistList -> {
                            artistListItems?.takeIf { it.isNotEmpty() }?.let { items ->
                                homeItem("artist_list_title", revealSeen, "title") {
                                    // just a title: each box below is its own artist's page
                                    HomeSectionTitle(title = stringResource(R.string.home_artist_list))
                                }
                                homeItem("artist_list_content", revealSeen, "cardRow") {
                                    HomeCardRow("artist_list", items, revealSeen, key = { "${it.artistId}_${it.artistName}" }, content = artistGroupCard)
                                }
                            }
                        }
                        HomeSection.AllTimeHits -> {
                            allTimeHits?.takeIf { it.isNotEmpty() }?.let { hits ->
                                homeItem("all_time_hits_title", revealSeen, "title") {
                                    HomeSectionTitle(
                                        title = stringResource(R.string.home_all_time_hits),
                                        onClick = { navController.navigate("section_detail/all_time_hits") }
                                    )
                                }
                                homeItem("all_time_hits_list", revealSeen, "cardRow") {
                                    HomeCardRow("all_time_hits", hits, revealSeen, key = { it.id }, content = hitsCard)
                                }
                            }
                        }
                        HomeSection.LiveShows, HomeSection.MusicVideos -> {
                            val isLive = section == HomeSection.LiveShows
                            (if (isLive) liveRow else musicVideoRow)?.takeIf { it.isNotEmpty() }?.let { videos ->
                                homeItem("${section.id}_title", revealSeen, "title") {
                                    HomeSectionTitle(
                                        title = stringResource(if (isLive) R.string.home_live_shows else R.string.home_music_videos),
                                        onClick = { navController.navigate("section_detail/${section.id}") }
                                    )
                                }
                                homeItem("${section.id}_list", revealSeen, "cardRow") {
                                    HomeCardRow(section.id, videos, revealSeen, key = { it.item.id }, content = videoRowCard)
                                }
                            }
                        }
                        is HomeSection.Category -> {
                            homeCategories?.firstOrNull { it.id == section.categoryId }?.let { category ->
                                homeItem("${section.id}_title", revealSeen, "title") {
                                    val categorySongs = category.items.filterIsInstance<SongItem>()
                                    val title = when (category.kind) {
                                        HomeCategory.Kind.NewFromArtists -> stringResource(R.string.home_new_from_artists)
                                        else -> category.title
                                    }
                                    HomeSectionTitle(
                                        label = when (category.kind) {
                                            HomeCategory.Kind.BecauseYouLike -> stringResource(R.string.home_because_you_like_title)
                                            HomeCategory.Kind.MoreLikeArtist -> stringResource(R.string.home_more_like_title)
                                            HomeCategory.Kind.YourGenre -> stringResource(R.string.home_because_you_listen_to_title)
                                            HomeCategory.Kind.NewGenre -> stringResource(R.string.home_try_something_new_title)
                                            HomeCategory.Kind.NewFromArtists -> null
                                        },
                                        title = title,
                                        thumbnail = category.thumbnailUrl?.let { thumbnailUrl ->
                                            {
                                                AsyncImage(
                                                    model = thumbnailUrl.resize(90, 90),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(30.dp)
                                                        .clip(
                                                            if (category.kind == HomeCategory.Kind.MoreLikeArtist) {
                                                                CircleShape
                                                            } else {
                                                                RoundedCornerShape(ThumbnailCornerRadius)
                                                            }
                                                        )
                                                )
                                            }
                                        },
                                        onClick = category.endpoint?.let { endpoint ->
                                            {
                                                if (category.kind == HomeCategory.Kind.MoreLikeArtist) {
                                                    navController.navigate("artist/${endpoint.browseId}")
                                                } else {
                                                    navController.navigate("genre/${endpoint.browseId}?params=${endpoint.params}")
                                                }
                                            }
                                        },
                                        onPlayAllClick = if (categorySongs.size >= 2) {
                                            {
                                                playerConnection.playQueue(
                                                    ListQueue(
                                                        title = title,
                                                        items = categorySongs.map { it.toMediaMetadata().toMediaItem() }
                                                    )
                                                )
                                            }
                                        } else null,
                                    )
                                }
                                homeItem("${section.id}_list", revealSeen, "cardRow") {
                                    HomeCardRow(section.id, category.items, revealSeen, key = { it.id }, content = ytItemCard)
                                }
                            }
                        }
                        HomeSection.AccountPlaylists -> {
                            if (!localPlaylists.isNullOrEmpty() || !accountPlaylists.isNullOrEmpty()) {
                                homeItem("account_playlists_title", revealSeen, "title") {
                                    Box {
                                        HomeSectionTitle(
                                            label = stringResource(R.string.playlists),
                                            title = accountName,
                                            thumbnail = {
                                                if (profileImageUrl != null) {
                                                    AsyncImage(
                                                        model = profileImageRequest,
                                                        placeholder = painterResource(id = R.drawable.person),
                                                        error = painterResource(id = R.drawable.person),
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .size(30.dp)
                                                            .clip(CircleShape)
                                                    )
                                                } else {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.person),
                                                        contentDescription = null,
                                                        tint = Color.White.copy(alpha = 0.69f),
                                                        modifier = Modifier.size(30.dp)
                                                    )
                                                }
                                            },
                                        )

                                    }
                                }

                                homeItem("account_playlists_list", revealSeen, "cardRow") {
                                    LazyRow(
                                        state = rememberCardRowState(),
                                        contentPadding = PaddingValues(horizontal = HomeContentInset),
                                        horizontalArrangement = Arrangement.spacedBy(HomeCardSpacing)
                                    ) {
                                        localPlaylists?.let { items ->
                                            items(items = items, key = { it.id }) { item ->
                                                localItemCard(item)
                                            }
                                        }
                                        accountPlaylists?.let { items ->
                                            items(items = items, key = { it.id }) { item ->
                                                ytItemCard(item)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        HomeSection.DontForgetTheseSongs -> {
                            forgottenFavorites?.takeIf { it.isNotEmpty() }?.let { forgottenFavorites ->
                                homeItem("forgotten_favorites_title", revealSeen, "title") {
                                    val forgottenFavoritesTitle = stringResource(R.string.home_dont_forget_songs)
                                    HomeSectionTitle(
                                        title = forgottenFavoritesTitle,
                                        onClick = { navController.navigate("section_detail/forgotten_favorites") },
                                        onPlayAllClick = {
                                            playerConnection.playQueue(
                                                ListQueue(
                                                    title = forgottenFavoritesTitle,
                                                    items = forgottenFavorites.map { it.toMediaItem() }
                                                )
                                            )
                                        }
                                    )
                                }
                                homeItem("forgotten_favorites_list", revealSeen, "trackGrid") {
                                    HomeTrackGrid(
                                        items = forgottenFavorites,
                                        state = forgottenFavoritesLazyGridState,
                                        rowSpacing = 6.dp,
                                        columnWidth = trackColumnWidth,
                                        key = { _, song -> song.id },
                                        content = trackRow,
                                    )
                                }
                            }
                        }
                        is HomeSection.HomePageSection -> {
                            val sectionData = homePage?.sections?.getOrNull(section.index)
                            sectionData?.let {
                                val sectionSongs = sectionData.items.filterIsInstance<SongItem>()

                                homeItem("home_section_title_${section.id}", revealSeen, "title") {
                                    HomeSectionTitle(
                                        title = sectionData.title,
                                        label = sectionData.label,
                                        thumbnail = sectionData.thumbnail?.let { thumbnailUrl ->
                                            {
                                                val shape = if (sectionData.endpoint?.isArtistEndpoint == true) {
                                                    CircleShape
                                                } else {
                                                    RoundedCornerShape(ThumbnailCornerRadius)
                                                }
                                                AsyncImage(
                                                    model = thumbnailUrl,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(30.dp)
                                                        .clip(shape)
                                                )
                                            }
                                        },
                                        onClick = sectionData.endpoint?.let { endpoint ->
                                            {
                                                when {
                                                    endpoint.browseId == "FEmusic_moods_and_genres" ->
                                                        navController.navigate("mood_and_genres")
                                                    endpoint.params != null ->
                                                        navController.navigate("youtube_browse/${endpoint.browseId}?params=${endpoint.params}")
                                                    else ->
                                                        navController.navigate("browse/${endpoint.browseId}")
                                                }
                                            }
                                        },
                                        onPlayAllClick = if (sectionSongs.isNotEmpty()) {
                                            {
                                                playerConnection.playQueue(
                                                    ListQueue(
                                                        title = sectionData.title,
                                                        items = sectionSongs.map { it.toMediaMetadata().toMediaItem() }
                                                    )
                                                )
                                            }
                                        } else null,
                                    )
                                }

                                homeItem("home_section_list_${section.id}", revealSeen, "cardRow") {
                                    if (feedVideoKinds[section.index] == FeedVideoKind.Other) {
                                        HomeCardRow(
                                            "home_section_${section.id}",
                                            sectionSongs.map(ArtistVideos::feedVideo),
                                            revealSeen,
                                            key = { it.item.id },
                                            content = videoRowCard,
                                        )
                                    } else {
                                        HomeCardRow("home_section_${section.id}", sectionData.items, revealSeen, key = { it.id }, content = ytItemCard)
                                    }
                                }
                            }
                        }
                    }

                    item(key = "section_spacer_${section.id}", contentType = "spacer") {
                        Spacer(modifier = Modifier.height(34.dp))
                    }
                }

                // only while something is really on its way; a page token alone used to leave
                // these bones up forever when the next page couldn't load
                if (launchHold || (isFirstLoad && speedDialItems.isEmpty() && keepListening.isNullOrEmpty()) || isLoadingMore) {
                    item(key = "loading_bones", contentType = "bones") {
                        BonesHost(
                            modifier = Modifier
                                .animateItem(
                                    fadeInSpec = tween(220),
                                    placementSpec = null,
                                    fadeOutSpec = tween(220),
                                )
                                .launchRise(order = 2)
                        ) {
                            repeat(2) {
                                HomeSectionBones()
                                Spacer(modifier = Modifier.height(34.dp))
                            }
                        }
                    }
                }

                item(key = "bottom_spacer", contentType = "spacer") {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
            }

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {

                val showHeroOverlay by remember { derivedStateOf { heroScrollProgressProvider() > 0.01f } }
                if (showHeroOverlay) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer { alpha = heroScrollProgressProvider().coerceIn(0f, 1f) }
                    ) {
                        ProgressiveGlassBackground(
                            state = homeGlassState,
                            maxBlurRadius = {
                                val sheetProgress = playerBottomSheetState?.progress ?: 0f
                                50f * heroScrollProgressProvider().coerceIn(0f, 1f) * (1f - sheetProgress.coerceIn(0f, 1f))
                            },
                            foundationColor = backgroundColor,
                            direction = BlurDirection.BottomToTop,

                            steps = 3,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .drawWithCache {
                                // every stop is black, so the fade is built once and only its alpha follows the scroll
                                val fade = Brush.verticalGradient(
                                    0f to Color.Black.copy(alpha = 0.9f),
                                    0.3f to Color.Black.copy(alpha = 0.6f),
                                    0.6f to Color.Black.copy(alpha = 0.3f),
                                    1f to Color.Transparent
                                )
                                onDrawBehind {
                                    drawRect(brush = fade, alpha = heroScrollProgressProvider().coerceIn(0f, 1f))
                                }
                            }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = WindowInsets.stableSystemBars.asPaddingValues().calculateTopPadding() + 14.dp,
                            bottom = 16.dp,
                            start = HomeContentInset,
                            end = 30.dp
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isScrolled by remember { derivedStateOf { heroScrollProgress > 0.5f } }
                    val topBarProgress by animateFloatAsState(
                        targetValue = if (isScrolled) 1f else 0f,
                        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                        label = "topBarProgress"
                    )

                    // the brand mark over the hero, swapped for a bold "Home" once the hero scrolls away.
                    // it's also where the launch intro lands the splash mark
                    Box(contentAlignment = Alignment.CenterStart) {
                        Icon(
                            painter = painterResource(R.drawable.ic_playder_mark),
                            contentDescription = "Playder",
                            tint = Color.White,
                            modifier = Modifier
                                .launchMarkTarget()
                                .size(HomeMarkWidth, HomeMarkHeight)
                                .graphicsLayer {
                                    alpha = 1f - topBarProgress
                                    val scale = 1f - topBarProgress * 0.25f
                                    scaleX = scale
                                    scaleY = scale
                                    transformOrigin = TransformOrigin(0f, 0.5f)
                                    renderEffect = BlurEffectCache.get(topBarProgress * 15f)
                                }
                        )

                        Text(
                            text = stringResource(R.string.home),
                            style = TextStyle(
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                letterSpacing = (-0.06).em,
                            ),
                            color = Color.White,
                            maxLines = 1,
                            modifier = Modifier.graphicsLayer {
                                alpha = topBarProgress
                                renderEffect = BlurEffectCache.get((1f - topBarProgress) * 15f)
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .launchRise(order = 0, distance = HomeTopBarRise)
                            .graphicsLayer {
                                val scale = 1f - (topBarProgress * 0.1f)
                                scaleX = scale
                                scaleY = scale
                                transformOrigin = TransformOrigin(1f, 0.5f)
                            }
                            .height(44.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable { navController.navigateToTab("settings") },
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
            val avatarBoundsModifier = Modifier
                            if (profileImageUrl != null) {
                                AsyncImage(
                                    model = profileImageRequest,
                                    placeholder = painterResource(R.drawable.person),
                                    error = painterResource(R.drawable.person),
                                    contentDescription = androidx.compose.ui.res.stringResource(R.string.profile_accessibility_description),
                                    modifier = avatarBoundsModifier
                                        .size(36.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = avatarBoundsModifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.person),
                                        contentDescription = androidx.compose.ui.res.stringResource(R.string.profile_accessibility_description),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
