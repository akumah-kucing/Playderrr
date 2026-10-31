// LibraryImportedTab.kt
//
// The Library's Imported tab: the import banner, then what each service brought in, as songs
// (All music) and as playlists, and Manage imports at the bottom. Songs YouTube sync keeps up to
// date count under YouTube here, apart from the songs liked in Playder itself.

package com.playder.ui.screens.library

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.playder.R
import com.playder.db.entities.Playlist
import com.playder.importer.ImportProgress
import com.playder.importer.ImportSource
import com.playder.ui.screens.search.SearchHorizontalPadding
import com.playder.viewmodels.ImportedSourceCard

/** What one service brought in, for its two cards. */
@Immutable
class ImportedService(
    val source: ImportSource,
    val songCount: Int,
    val songCovers: List<String>,
    val playlists: List<Playlist>,
) {
    val isEmpty: Boolean get() = songCount == 0 && playlists.isEmpty()
}

/**
 * The four services the mock lists, always, then any other one that has brought something in.
 * A service with nothing yet still gets its cards: they read 0 and open the import sheet.
 */
internal fun importedServices(
    songSources: List<ImportedSourceCard>,
    playlists: List<Playlist>,
    playlistSources: Map<String, ImportSource>,
): List<ImportedService> {
    val playlistsBySource = playlists.groupBy { playlistSources[it.id] }
    val songsBySource = songSources.associateBy { it.source }
    val extra = (songsBySource.keys + playlistsBySource.keys.filterNotNull())
        .filter { it.isListed && it !in ImportedTabServices }
        .sortedBy { it.ordinal }
    return (ImportedTabServices + extra).map { source ->
        val card = songsBySource[source]
        ImportedService(source, card?.songCount ?: 0, card?.covers.orEmpty(), playlistsBySource[source].orEmpty())
    }
}

internal fun LazyListScope.libraryImportedTab(
    importProgress: ImportProgress,
    services: List<ImportedService>,
    onOpenImportSheet: () -> Unit,
    onOpenImportProgress: () -> Unit,
    onOpenSongs: (ImportSource) -> Unit,
    onOpenPlaylists: (ImportSource) -> Unit,
    onManage: () -> Unit,
) {
    item(key = "imported_banner") {
        LibraryImportBanner(
            progress = importProgress,
            onOpenSheet = onOpenImportSheet,
            onOpenProgress = onOpenImportProgress,
            modifier = Modifier.padding(horizontal = SearchHorizontalPadding),
        )
        Spacer(modifier = Modifier.height(22.dp))
    }

    item(key = "imported_songs") {
        LibraryCapsLabel(stringResource(R.string.all_music))
        Spacer(modifier = Modifier.height(10.dp))
        LibraryCardGrid(
            services.map { service ->
                LibraryCardEntry(
                    title = service.source.cardLabel,
                    count = service.songCount,
                    covers = service.songCovers,
                    onClick = { if (service.songCount == 0) onOpenImportSheet() else onOpenSongs(service.source) },
                    icon = { ImportSourceIcon(service.source, size = 18.dp) },
                )
            },
        )
        Spacer(modifier = Modifier.height(22.dp))
    }

    item(key = "imported_playlists") {
        LibraryCapsLabel(stringResource(R.string.playlists))
        Spacer(modifier = Modifier.height(10.dp))
        LibraryCardGrid(
            services.map { service ->
                LibraryCardEntry(
                    title = service.source.cardLabel,
                    count = service.playlists.size,
                    covers = service.playlists.take(2).flatMap { it.thumbnails },
                    onClick = { if (service.playlists.isEmpty()) onOpenImportSheet() else onOpenPlaylists(service.source) },
                    icon = { ImportSourceIcon(service.source, size = 18.dp) },
                )
            },
        )
    }

    item(key = "imported_manage") {
        Spacer(modifier = Modifier.height(22.dp))
        LibraryManagePill(text = stringResource(R.string.manage_imports), onClick = onManage)
    }
}
