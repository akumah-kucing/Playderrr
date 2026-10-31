// LibraryArtistsScreen.kt

package com.playder.ui.screens.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.playder.R
import com.playder.ui.component.LocalMenuState
import com.playder.ui.menu.ArtistMenu
import com.playder.viewmodels.LibraryHomeViewModel
import com.playder.viewmodels.LibraryShelfKind

/**
 * The library's artists, or with a [shelf] the Local or Cached tab's: only the artists of that
 * tab's songs, each with how many of its songs are there.
 */
@Composable
fun LibraryArtistsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
    shelf: LibraryShelfKind? = null,
) {
    val viewModel: LibraryHomeViewModel = hiltViewModel()
    val artists by (shelf?.let(viewModel::shelf)?.artists ?: viewModel.artists).collectAsStateWithLifecycle()
    val menuState = LocalMenuState.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LibraryEntityListScreen(
        title = stringResource(R.string.artists),
        subtitle = when (shelf) {
            LibraryShelfKind.Local -> stringResource(R.string.artists_on_device)
            LibraryShelfKind.Cached -> stringResource(R.string.artists_in_cache)
            null -> stringResource(R.string.library_artists_subtitle)
        },
        searchPlaceholder = stringResource(R.string.find_artist_hint),
        items = artists,
        idOf = { it.id },
        nameOf = { it.artist.name },
        subtitleOf = {
            val count = context.resources.getQuantityString(R.plurals.library_song_count, it.songCount, it.songCount)
            when (shelf) {
                LibraryShelfKind.Local -> context.getString(R.string.songs_on_device, count)
                LibraryShelfKind.Cached -> context.getString(R.string.songs_in_cache, count)
                null -> count
            }
        },
        thumbnailOf = { it.artist.thumbnailUrl },
        roundThumbnails = true,
        onClick = { artist -> navController.navigate("artist/${artist.id}") },
        onLongClick = { artist ->
            menuState.show {
                ArtistMenu(
                    originalArtist = artist,
                    coroutineScope = coroutineScope,
                    onDismiss = menuState::dismiss,
                )
            }
        },
        modifier = modifier,
        pureBlack = pureBlack,
    )
}
