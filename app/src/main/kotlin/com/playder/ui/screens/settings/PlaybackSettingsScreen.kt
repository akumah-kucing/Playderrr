package com.playder.ui.screens.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.playder.R
import com.playder.constants.PersistentQueueKey
import com.playder.constants.SkipSilenceKey
import com.playder.constants.StopPlaybackOnTaskRemovedKey
import com.playder.ui.component.AppSwitch
import com.playder.ui.component.SettingsGroup
import com.playder.ui.component.SettingsGroupStyle
import com.playder.ui.component.SettingsItem
import com.playder.ui.component.SubSettingsScaffold
import com.playder.ui.component.SubSettingsSearchBar
import com.playder.utils.rememberPreference

@Composable
fun PlaybackSettingsScreen(navController: NavController) {
    var query by remember { mutableStateOf("") }

    val (stopPlaybackOnTaskRemoved, onStopPlaybackOnTaskRemovedChange) = rememberPreference(
        StopPlaybackOnTaskRemovedKey,
        defaultValue = false
    )
    val (persistentQueue, onPersistentQueueChange) = rememberPreference(
        PersistentQueueKey,
        defaultValue = true
    )
    val (skipSilence, onSkipSilenceChange) = rememberPreference(
        SkipSilenceKey,
        defaultValue = false
    )

    val keepPlaying = !stopPlaybackOnTaskRemoved

    SubSettingsScaffold(
        title = stringResource(R.string.playback_category),
        onBack = { navController.navigateUp() },
        searchBar = { SubSettingsSearchBar(query = query, onQueryChange = { query = it }) },
    ) {
        val items = listOf(
            SettingsItem(
                title = { Text(stringResource(R.string.loc_playback_keep_playing)) },
                highlightKey = stringResource(R.string.loc_playback_keep_playing),
                descriptionText = stringResource(R.string.loc_playback_keep_playing_desc),
                detail = stringResource(R.string.loc_playback_keep_playing_detail),
                icon = painterResource(R.drawable.clear_all),
                iconShape = CircleShape,
                onClick = { onStopPlaybackOnTaskRemovedChange(!stopPlaybackOnTaskRemoved) },
                trailingContent = {
                    AppSwitch(
                        checked = keepPlaying,
                        onCheckedChange = { onStopPlaybackOnTaskRemovedChange(!it) },
                    )
                },
            ),
            SettingsItem(
                title = { Text(stringResource(R.string.loc_playback_save_state)) },
                highlightKey = stringResource(R.string.loc_playback_save_state),
                descriptionText = stringResource(R.string.loc_playback_save_state_desc),
                detail = stringResource(R.string.loc_playback_save_state_detail),
                icon = painterResource(R.drawable.cached),
                iconShape = CircleShape,
                onClick = { onPersistentQueueChange(!persistentQueue) },
                trailingContent = {
                    AppSwitch(
                        checked = persistentQueue,
                        onCheckedChange = onPersistentQueueChange,
                    )
                },
            ),
            SettingsItem(
                title = { Text(stringResource(R.string.loc_playback_skip_silence)) },
                highlightKey = stringResource(R.string.loc_playback_skip_silence),
                descriptionText = stringResource(R.string.loc_playback_skip_silence_desc),
                icon = painterResource(R.drawable.skip_next),
                iconShape = CircleShape,
                onClick = { onSkipSilenceChange(!skipSilence) },
                trailingContent = {
                    AppSwitch(
                        checked = skipSilence,
                        onCheckedChange = onSkipSilenceChange,
                    )
                },
            ),
        ).filter {
            query.isBlank() || it.highlightKey?.contains(query, ignoreCase = true) == true ||
                it.descriptionText?.contains(query, ignoreCase = true) == true
        }

        if (items.isNotEmpty()) {
            SettingsGroup(
                style = SettingsGroupStyle.Grouped,
                items = items,
            )
        }

        Spacer(Modifier.height(140.dp))
    }
}
