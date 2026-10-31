// LibraryImportedScreen.kt
//
// "Imported from Spotify": every song one service brought in, in the same A-Z list as Songs, with the
// service's own icon where the Library home has the avatar.

package com.playder.ui.screens.library

import com.playder.ui.screens.search.AvatarSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.playder.LocalPlayerConnection
import com.playder.extensions.toMediaItem
import com.playder.playback.queues.ListQueue
import com.playder.ui.component.LocalMenuState
import com.playder.ui.component.SongSwipeBox
import com.playder.ui.component.librarySwipeAction
import com.playder.ui.component.queueSwipeAction
import com.playder.ui.component.removeImportSwipeAction
import com.playder.ui.menu.SongMenu
import com.playder.viewmodels.LibraryImportedViewModel

@Composable
fun LibraryImportedScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
) {
    val viewModel: LibraryImportedViewModel = hiltViewModel()
    val songs by viewModel.songs.collectAsState()
    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current
    val source = viewModel.source

    val label = source?.label ?: androidx.compose.ui.res.stringResource(com.playder.R.string.library_unknown_source)
    val importedQueueTitle =
        androidx.compose.ui.res.stringResource(com.playder.R.string.library_imported_from_title, label)
    val sourceIcon: (@Composable () -> Unit)? = if (source != null) {
        { ImportSourceIcon(source, size = AvatarSize) }
    } else {
        null
    }

    LibraryEntityListScreen(
        title = source?.label ?: androidx.compose.ui.res.stringResource(com.playder.R.string.library_tab_imported),
        subtitle = androidx.compose.ui.res.pluralStringResource(
            com.playder.R.plurals.library_imported_song_count,
            songs.size,
            songs.size,
            label,
        ),
        searchPlaceholder = androidx.compose.ui.res.stringResource(com.playder.R.string.library_search_imported_hint, label),
        items = songs,
        idOf = { it.id },
        nameOf = { it.song.title },
        subtitleOf = { it.artists.joinToString { a -> a.name }.ifBlank { null } },
        thumbnailOf = { it.song.thumbnailUrl },
        playingIdOf = { it.id },
        onClick = { song ->
            // The order on screen, so the queue starts where the user tapped and runs down the list.
            val ordered = sortForLibraryIndex(songs) { it.song.title }
            playerConnection.playQueue(
                ListQueue(
                    title = importedQueueTitle,
                    items = ordered.map { it.toMediaItem() },
                    startIndex = ordered.indexOf(song).coerceAtLeast(0),
                ),
            )
        },
        onLongClick = { song ->
            menuState.show {
                SongMenu(
                    originalSong = song,
                    navController = navController,
                    onDismiss = menuState::dismiss,
                )
            }
        },
        modifier = modifier,
        pureBlack = pureBlack,
        rowWrapper = { song, row ->
            SongSwipeBox(
                slab = false,
                library = { if (source != null) removeImportSwipeAction(song.song, source) else librarySwipeAction(song.song) },
                queue = { queueSwipeAction { song.toMediaItem() } },
            ) {
                row()
            }
        },
        trailing = sourceIcon,
    )
}
