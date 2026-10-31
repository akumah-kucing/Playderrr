// LibraryManageSheets.kt
//
// "Manage imports", "Manage local" and "Manage cache": big tiles for the main actions and a row per
// other action, as in the mocks. Anything that takes music out of the library asks once more on a
// sheet pushed over this one.

package com.playder.ui.screens.library

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playder.R
import com.playder.db.entities.Song
import com.playder.importer.ImportSource
import com.playder.ui.component.PopupSheetHandle
import com.playder.ui.component.PopupSheetState
import com.playder.ui.component.SheetIcon
import com.playder.ui.component.SheetPillButton
import com.playder.ui.player.menu.MenuRowSurface
import com.playder.ui.screens.search.SearchColors
import com.playder.viewmodels.LibraryImportViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * [sources] are the services that have brought music in, one "Delete ... imports" row each.
 * [onResyncAll] checks every imported playlist for new songs: a long tile under the two others.
 */
fun PopupSheetState.showManageImportsSheet(
    sources: List<ImportSource>,
    viewModel: LibraryImportViewModel,
    onResyncAll: () -> Unit,
) {
    var sheet: PopupSheetHandle? = null
    fun confirmThen(title: String, message: String, action: String, run: () -> Unit) =
        showConfirm(title, message, action) {
            run()
            sheet?.dismiss()
        }
    sheet = showWithHandle {
        val deleteAllLabel = androidx.compose.ui.res.stringResource(R.string.library_delete_all_imports)
        val deleteAllTitle = androidx.compose.ui.res.stringResource(R.string.library_delete_all_imports_confirm)
        val deleteAllMessage = androidx.compose.ui.res.stringResource(R.string.library_delete_all_imports_message)
        val undoImportLabel = androidx.compose.ui.res.stringResource(R.string.library_undo_import_session)
        val undoImportTitle = androidx.compose.ui.res.stringResource(R.string.library_undo_last_import)
        val undoImportMessage = androidx.compose.ui.res.stringResource(R.string.library_undo_last_import_message)
        val deleteAction = androidx.compose.ui.res.stringResource(R.string.delete)
        val undoAction = androidx.compose.ui.res.stringResource(R.string.library_undo_import)
        ManageLayout(
            ManageAction(R.drawable.delete, deleteAllLabel) {
                confirmThen(
                    title = deleteAllTitle,
                    message = deleteAllMessage,
                    action = deleteAllLabel,
                    run = viewModel::deleteAllImports,
                )
            },
            ManageAction(R.drawable.undo, undoImportLabel) {
                confirmThen(
                    title = undoImportTitle,
                    message = undoImportMessage,
                    action = undoAction,
                    run = viewModel::undoLatestImport,
                )
            },
            wide = ManageAction(R.drawable.sync, androidx.compose.ui.res.stringResource(R.string.library_resync_all)) {
                sheet?.dismiss()
                onResyncAll()
            },
            rows = sources.map { source ->
                val deleteSourceLabel = androidx.compose.ui.res.stringResource(R.string.library_delete_source_imports, source.label)
                val deleteSourceTitle = androidx.compose.ui.res.stringResource(R.string.library_delete_source_imports_confirm, source.label)
                val deleteSourceMessage =
                    androidx.compose.ui.res.stringResource(R.string.library_delete_source_imports_message, source.label) +
                        if (source == ImportSource.YOUTUBE_MUSIC) {
                            " " + androidx.compose.ui.res.stringResource(R.string.library_youtube_sync_stays)
                        } else {
                            ""
                        }
                ManageAction(R.drawable.delete, deleteSourceLabel) {
                    confirmThen(
                        title = deleteSourceTitle,
                        message = deleteSourceMessage,
                        action = deleteAction,
                        run = { viewModel.deleteImportsFrom(source) },
                    )
                }
            },
        )
    }
}

/**
 * [onRescan], [onAddFolder] and [onRemoveFromList] open their own sheets or pickers, so this one
 * closes first. Re-scan also brings back missing covers and reads the picked folders again.
 */
fun PopupSheetState.showManageLocalSheet(
    viewModel: LibraryImportViewModel,
    songs: List<Song>,
    onRescan: () -> Unit,
    onAddFolder: () -> Unit,
    onRemoveFromList: () -> Unit,
) {
    var sheet: PopupSheetHandle? = null
    sheet = showWithHandle {
        val undoImportLabel = androidx.compose.ui.res.stringResource(R.string.library_undo_import_session)
        val undoImportAction = androidx.compose.ui.res.stringResource(R.string.library_undo_import)
        val undoLocalTitle = androidx.compose.ui.res.stringResource(R.string.library_undo_local_import)
        val undoLocalMessage = androidx.compose.ui.res.stringResource(R.string.library_undo_local_import_message)
        ManageLayout(
            ManageAction(R.drawable.refresh, androidx.compose.ui.res.stringResource(R.string.library_rescan_local)) {
                sheet?.dismiss()
                onRescan()
            },
            ManageAction(R.drawable.delete, androidx.compose.ui.res.stringResource(R.string.library_delete_from_list)) {
                sheet?.dismiss()
                onRemoveFromList()
            },
            wide = ManageAction(R.drawable.library_add, androidx.compose.ui.res.stringResource(R.string.library_add_music_folder)) {
                sheet?.dismiss()
                onAddFolder()
            },
            rows = listOf(
                ManageAction(R.drawable.info, androidx.compose.ui.res.stringResource(R.string.library_check_collection)) {
                    showLocalAuditSheet(songs)
                },
                ManageAction(R.drawable.undo, undoImportLabel) {
                    showConfirm(
                        title = undoLocalTitle,
                        message = undoLocalMessage,
                        action = undoImportAction,
                    ) {
                        viewModel.undoLatestLocalImport()
                        sheet?.dismiss()
                    }
                },
            ),
        )
    }
}

private fun PopupSheetState.showLocalAuditSheet(songs: List<Song>) {
    showWithHandle {
        LocalAuditContent(songs)
    }
}

private data class LocalAuditResult(
    val trackCount: Int,
    val possibleDuplicateGroups: Int,
    val missingTitles: Int,
    val missingArtists: Int,
    val missingArtwork: Int,
)

@Composable
private fun LocalAuditContent(songs: List<Song>) {
    val result = produceState<LocalAuditResult?>(initialValue = null) {
        value = withContext(Dispatchers.Default) { auditLocalSongs(songs) }
    }.value

    Text(text = androidx.compose.ui.res.stringResource(R.string.library_local_collection_check), color = SearchColors.Primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    Text(
        text = androidx.compose.ui.res.stringResource(R.string.library_local_collection_read_only),
        color = SearchColors.Secondary,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )
    Spacer(Modifier.height(12.dp))

    if (result == null) {
        Text(text = androidx.compose.ui.res.stringResource(R.string.library_checking_local_tracks), color = SearchColors.Secondary, fontSize = 14.sp)
    } else {
        AuditResultRow(androidx.compose.ui.res.stringResource(R.string.library_local_tracks_checked), result.trackCount)
        AuditResultRow(androidx.compose.ui.res.stringResource(R.string.library_possible_duplicate_groups), result.possibleDuplicateGroups)
        AuditResultRow(androidx.compose.ui.res.stringResource(R.string.library_missing_title), result.missingTitles)
        AuditResultRow(androidx.compose.ui.res.stringResource(R.string.library_missing_artist), result.missingArtists)
        AuditResultRow(androidx.compose.ui.res.stringResource(R.string.library_missing_artwork), result.missingArtwork)
    }
}

@Composable
private fun AuditResultRow(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, color = SearchColors.Primary, fontSize = 14.sp)
        Text(text = count.toString(), color = SearchColors.Secondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

private fun auditLocalSongs(songs: List<Song>): LocalAuditResult {
    val localSongs = songs.filter { it.song.isLocal }
    val possibleDuplicateGroups = localSongs.mapNotNull { song ->
        val title = auditMetadata(song.song.title) ?: return@mapNotNull null
        val artists = song.artists.mapNotNull { auditMetadata(it.name) }.sorted()
        if (artists.isEmpty()) null else "$title|${artists.joinToString("|")}"
    }.groupingBy { it }.eachCount().count { it.value > 1 }

    return LocalAuditResult(
        trackCount = localSongs.size,
        possibleDuplicateGroups = possibleDuplicateGroups,
        missingTitles = localSongs.count { auditMetadata(it.song.title) == null },
        missingArtists = localSongs.count { song -> song.artists.none { auditMetadata(it.name) != null } },
        missingArtwork = localSongs.count { it.song.thumbnailUrl.isNullOrBlank() },
    )
}

private fun auditMetadata(value: String?): String? {
    val normalized = value?.trim()?.lowercase(Locale.ROOT)?.takeIf { it.isNotEmpty() } ?: return null
    return normalized.takeUnless { it in setOf("<unknown>", "unknown", "unknown song", "unknown artist") }
}

/**
 * [cachedSongs] are the songs "Remove songs" lists. Clearing goes further and also drops songs that
 * only got partly cached; downloads are kept apart and never touched.
 */
fun PopupSheetState.showManageCachedSheet(
    cachedSongs: List<Song>,
    onClearCache: () -> Unit,
    onRemove: (Set<String>) -> Unit,
) {
    var sheet: PopupSheetHandle? = null
    sheet = showWithHandle {
        val clearCacheLabel = androidx.compose.ui.res.stringResource(R.string.library_clear_cache)
        val clearCacheTitle = androidx.compose.ui.res.stringResource(R.string.library_clear_cache_confirm)
        val clearCacheMessage = androidx.compose.ui.res.stringResource(R.string.library_clear_cache_message)
        ManageLayout(
            ManageAction(R.drawable.clear_all, clearCacheLabel) {
                showConfirm(
                    title = clearCacheTitle,
                    message = clearCacheMessage,
                    action = clearCacheLabel,
                ) {
                    onClearCache()
                    sheet?.dismiss()
                }
            },
            ManageAction(R.drawable.delete, androidx.compose.ui.res.stringResource(R.string.library_remove_songs)) {
                sheet?.dismiss()
                showRemoveCachedSheet(cachedSongs, onRemove)
            },
            rows = emptyList(),
        )
    }
}

/** One more look before music leaves the phone or the library, pushed over whatever asked. */
internal fun PopupSheetState.showConfirm(title: String, message: String, action: String, onConfirm: () -> Unit) {
    var sheet: PopupSheetHandle? = null
    sheet = showWithHandle(
        buttonBar = {
            SheetPillButton(
                text = action,
                onClick = {
                    sheet?.dismiss()
                    onConfirm()
                },
            )
        },
    ) {
        Text(text = title, color = SearchColors.Primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(text = message, color = SearchColors.Secondary, fontSize = 14.sp, lineHeight = 20.sp)
        Spacer(Modifier.height(12.dp))
    }
}

private class ManageAction(@DrawableRes val icon: Int, val label: String, val onClick: () -> Unit)

@Composable
private fun ManageLayout(first: ManageAction, second: ManageAction, wide: ManageAction? = null, rows: List<ManageAction>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(bottom = 16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ManageTile(first, Modifier.weight(1f))
            ManageTile(second, Modifier.weight(1f))
        }
        // a third big action spans the width under the two, rather than squeezing three into a row
        wide?.let { ManageTile(it, Modifier.fillMaxWidth()) }
        rows.forEach { ManageRow(it) }
    }
}

/** A tall tile: its icon in the top corner, the label at the bottom. */
@Composable
private fun ManageTile(action: ManageAction, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .heightIn(min = 92.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MenuRowSurface)
            .clickable(role = Role.Button, onClick = action.onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        ManageIcon(action.icon)
        Spacer(Modifier.height(14.dp))
        ManageLabel(action.label)
    }
}

@Composable
private fun ManageRow(action: ManageAction) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MenuRowSurface)
            .clickable(role = Role.Button, onClick = action.onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        ManageIcon(action.icon)
        Spacer(Modifier.width(14.dp))
        ManageLabel(action.label)
    }
}

/** The action's icon in plain white. It used to sit in a light circle; the sheets dropped those. */
@Composable
private fun ManageIcon(@DrawableRes icon: Int) {
    SheetIcon(icon, size = 22.dp)
}

@Composable
private fun ManageLabel(text: String) {
    Text(
        text = text,
        color = SearchColors.Primary,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}
