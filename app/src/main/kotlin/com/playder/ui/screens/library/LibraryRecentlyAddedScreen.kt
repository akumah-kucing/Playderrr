// LibraryRecentlyAddedScreen.kt
//
// Songs only - every library song, or with a shelf the Local or Cached tab's songs. The underlying
// query orders by when each song entered the library (a local one: when it was downloaded or
// imported; a cached one: when it was last played), which is the one timeline shared across the library; albums/artists/playlists carry no
// comparable timestamp, so a combined "recently added" feed would need new queries rather than a
// merge of what exists.

package com.playder.ui.screens.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.playder.LocalPlayerConnection
import com.playder.R
import com.playder.extensions.toMediaItem
import com.playder.playback.queues.ListQueue
import com.playder.viewmodels.LibraryHomeViewModel
import com.playder.viewmodels.LibraryShelfKind

@Composable
fun LibraryRecentlyAddedScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
    shelf: LibraryShelfKind? = null,
) {
    val viewModel: LibraryHomeViewModel = hiltViewModel()
    // with the lifecycle: the Cached shelf looks at the cache every couple of seconds while watched
    val songs by (shelf?.let(viewModel::shelf)?.songs ?: viewModel.songs).collectAsStateWithLifecycle()
    val queueTitle = stringResource(R.string.library_recently_added_queue_title)
    val playerConnection = LocalPlayerConnection.current ?: return

    val entries = remember(songs) {
        songs.map {
            LibraryGridEntry(
                id = it.id,
                title = it.title,
                subtitle = it.artists.joinToString { a -> a.name }.ifBlank { null },
                thumbnailUrl = it.song.thumbnailUrl,
            )
        }
    }

    fun play(startIndex: Int, shuffle: Boolean) {
        if (songs.isEmpty()) return
        val ordered = if (shuffle) songs.shuffled() else songs
        playerConnection.playQueue(
            ListQueue(
                title = queueTitle,
                items = ordered.map { it.toMediaItem() },
                startIndex = if (shuffle) 0 else startIndex,
            ),
        )
    }

    LibraryGridScreen(
        title = stringResource(R.string.library_title_added),
        subtitle = stringResource(R.string.library_subtitle_recently),
        searchPlaceholder = stringResource(R.string.library_recently_added_search_hint),
        entries = entries,
        onOpen = { entry -> play(songs.indexOfFirst { it.id == entry.id }.coerceAtLeast(0), shuffle = false) },
        onPlay = { play(0, shuffle = false) },
        onShuffle = { play(0, shuffle = true) },
        modifier = modifier,
        pureBlack = pureBlack,
    )
}
