package com.playder.ui.screens.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.playder.LocalPlayerConnection
import com.playder.R
import com.playder.ui.component.SettingsItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


@Composable
fun crossmixTestItems(isVisible: Boolean = true): List<SettingsItem> {
    val playerConnection = LocalPlayerConnection.current
    val statusFlow: StateFlow<String> = remember(playerConnection) {
        playerConnection?.crossmixStatus ?: MutableStateFlow("")
    }
    val status by statusFlow.collectAsState()
    return listOf(
        SettingsItem(
            title = { Text(stringResource(R.string.loc_crossmix_status)) },
            descriptionText = status.ifBlank { stringResource(R.string.loc_crossmix_status_empty) },
            icon = painterResource(R.drawable.info),
            iconShape = androidx.compose.foundation.shape.CircleShape,
            isVisible = isVisible,
        ),
        SettingsItem(
            title = { Text(stringResource(R.string.loc_crossmix_test_song)) },
            highlightKey = "Test on this song",
            descriptionText = stringResource(R.string.loc_crossmix_test_song_desc),
            detail = stringResource(R.string.loc_crossmix_test_song_detail),
            icon = painterResource(R.drawable.fast_forward),
            iconShape = androidx.compose.foundation.shape.CircleShape,
            enabled = playerConnection != null,
            isVisible = isVisible,
            onClick = { playerConnection?.testCrossmix() },
        ),
    )
}
