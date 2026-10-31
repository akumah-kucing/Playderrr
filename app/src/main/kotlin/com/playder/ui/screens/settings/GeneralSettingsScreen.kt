package com.playder.ui.screens.settings

import android.app.Activity
import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.playder.R
import com.playder.constants.DisableVideoAutoplayKey
import com.playder.constants.DontSkipOnFailureKey
import com.playder.constants.LocalSongAutoMetadataKey
import com.playder.constants.OfflineModeKey
import com.playder.constants.StreamFailureTimeoutKey
import com.playder.ui.screens.audiolab.LabCard
import com.playder.ui.screens.audiolab.LabSlider
import com.playder.ui.component.AppSwitch
import com.playder.ui.component.SettingsGroup
import com.playder.ui.component.SettingsGroupStyle
import com.playder.ui.component.SettingsItem
import com.playder.ui.component.SubSettingsScaffold
import com.playder.ui.component.SubSettingsSearchBar
import com.playder.ui.theme.InterFontFamily
import com.playder.utils.dataStore
import com.playder.utils.rememberPreference
import com.playder.constants.AppLanguageKey
import com.playder.constants.SYSTEM_DEFAULT
import com.playder.constants.canonicalizeAppLanguageTag
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.launch
import java.util.Locale

private val SupportedAppLanguageTags = listOf(
    "ar", "bn", "cs", "de", "el", "en", "en-CA", "es", "fa", "fil", "fr",
    "hi", "id", "it", "ja", "ko", "ms", "nl", "pl", "pt", "ro", "ru", "sk",
    "th", "tr", "uk", "vi", "zh-CN", "zh-TW",
).map { tag ->
    val locale = Locale.forLanguageTag(tag)
    tag to locale.getDisplayName(locale)
}.sortedBy { it.second }

@Composable
fun GeneralSettingsScreen(navController: NavController) {
    var query by remember { mutableStateOf("") }
    var showLanguagePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    val (offlineMode, onOfflineModeChange) = rememberPreference(
        OfflineModeKey,
        defaultValue = false
    )
    val (localSongAutoMetadata, onLocalSongAutoMetadataChange) = rememberPreference(
        LocalSongAutoMetadataKey,
        defaultValue = true
    )
    val (disableVideoAutoplay, onDisableVideoAutoplayChange) = rememberPreference(
        DisableVideoAutoplayKey,
        defaultValue = false
    )

    val (dontSkipOnFailure, onDontSkipOnFailureChange) = rememberPreference(
        DontSkipOnFailureKey,
        defaultValue = false
    )

    val (failureTimeout, onFailureTimeoutChange) = rememberPreference(
        StreamFailureTimeoutKey,
        defaultValue = 6
    )
    val (appLanguage, _) = rememberPreference(
        AppLanguageKey,
        defaultValue = SYSTEM_DEFAULT
    )
    val selectedLanguageTag = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.getSystemService(LocaleManager::class.java)
            .applicationLocales
            .toLanguageTags()
            .ifBlank { null }
    } else {
        appLanguage.takeUnless { it == SYSTEM_DEFAULT }
    })?.let(::canonicalizeAppLanguageTag)
    val selectedLanguageName = selectedLanguageTag?.let { tag ->
        SupportedAppLanguageTags.firstOrNull { it.first.equals(tag, ignoreCase = true) }?.second
            ?: Locale.forLanguageTag(tag).getDisplayName(Locale.forLanguageTag(tag))
    } ?: stringResource(R.string.follow_system_language)

    fun selectLanguage(tag: String?) {
        showLanguagePicker = false
        scope.launch {
            context.dataStore.edit { preferences ->
                preferences[AppLanguageKey] = tag ?: SYSTEM_DEFAULT
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.getSystemService(LocaleManager::class.java).applicationLocales =
                    LocaleList.forLanguageTags(tag.orEmpty())
            } else {
                activity?.recreate()
            }
        }
    }

    SubSettingsScaffold(
        title = stringResource(R.string.general_settings),
        onBack = { navController.navigateUp() },
        searchBar = { SubSettingsSearchBar(query = query, onQueryChange = { query = it }) },
    ) {
        Text(
            text = stringResource(R.string.general_settings_intro),
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
        )

        val toggleItems = listOf(
            SettingsItem(
                title = { Text(stringResource(R.string.app_language)) },
                highlightKey = "app language bahasa",
                descriptionText = selectedLanguageName,
                onClick = { showLanguagePicker = true },
            ),
            SettingsItem(
                title = { Text(stringResource(R.string.disable_update_notice)) },
                highlightKey = "Disable App Updates notice",
                descriptionText = stringResource(R.string.disable_update_notice_description),
                icon = painterResource(R.drawable.commit),
                iconShape = CircleShape,
                onClick = { },
                trailingContent = {
                    AppSwitch(
                        checked = false,
                        onCheckedChange = { },
                        enabled = false,
                    )
                },
            ),
            SettingsItem(
                title = { Text(stringResource(R.string.offline_mode)) },
                highlightKey = "Offline mode",
                descriptionText = stringResource(R.string.offline_mode_description),
                icon = painterResource(R.drawable.download),
                iconShape = CircleShape,
                onClick = { onOfflineModeChange(!offlineMode) },
                trailingContent = {
                    AppSwitch(
                        checked = offlineMode,
                        onCheckedChange = onOfflineModeChange,
                    )
                },
            ),
            SettingsItem(
                title = { Text(stringResource(R.string.local_song_auto_metadata)) },
                highlightKey = "Local song auto metadata",
                descriptionText = stringResource(R.string.local_song_auto_metadata_description),
                icon = painterResource(R.drawable.cached),
                iconShape = CircleShape,
                onClick = { onLocalSongAutoMetadataChange(!localSongAutoMetadata) },
                trailingContent = {
                    AppSwitch(
                        checked = localSongAutoMetadata,
                        onCheckedChange = onLocalSongAutoMetadataChange,
                    )
                },
            ),
            SettingsItem(
                title = { Text(stringResource(R.string.disable_video_autoplay)) },
                highlightKey = "Disable auto-playing YouTube video",
                descriptionText = stringResource(R.string.disable_video_autoplay_description),
                icon = painterResource(R.drawable.slow_motion_video),
                iconShape = CircleShape,
                onClick = { onDisableVideoAutoplayChange(!disableVideoAutoplay) },
                trailingContent = {
                    AppSwitch(
                        checked = disableVideoAutoplay,
                        onCheckedChange = onDisableVideoAutoplayChange,
                    )
                },
            ),
            SettingsItem(
                title = { Text(stringResource(R.string.dont_skip_on_failure)) },
                highlightKey = "Don't skip on failure",
                descriptionText = stringResource(R.string.dont_skip_on_failure_description),
                detail = stringResource(R.string.dont_skip_on_failure_detail),
                icon = painterResource(R.drawable.skip_next),
                iconShape = CircleShape,
                onClick = { onDontSkipOnFailureChange(!dontSkipOnFailure) },
                trailingContent = {
                    AppSwitch(
                        checked = dontSkipOnFailure,
                        onCheckedChange = onDontSkipOnFailureChange,
                    )
                },
            ),
        ).filter {
            query.isBlank() || it.highlightKey?.contains(query, ignoreCase = true) == true ||
                it.descriptionText?.contains(query, ignoreCase = true) == true
        }

        if (toggleItems.isNotEmpty()) {
            SettingsGroup(
                style = SettingsGroupStyle.Grouped,
                items = toggleItems,
            )
        }

        if (!dontSkipOnFailure && (query.isBlank() || "skip timeout failure".contains(query, ignoreCase = true))) {
            Spacer(Modifier.height(14.dp))
            LabCard(padding = 14.dp) {
                LabSlider(
                    label = stringResource(R.string.skip_timeout),
                    valueText = "${failureTimeout}s",
                    value = failureTimeout.toFloat(),
                    onValueChange = { onFailureTimeoutChange(it.toInt()) },
                    valueRange = 5f..20f,
                    steps = 14,
                )
            }
        }

        Spacer(Modifier.height(140.dp))
    }

    if (showLanguagePicker) {
        AlertDialog(
            onDismissRequest = { showLanguagePicker = false },
            title = { Text(stringResource(R.string.app_language)) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    item(key = SYSTEM_DEFAULT) {
                        LanguageOption(
                            label = stringResource(R.string.follow_system_language),
                            selected = selectedLanguageTag == null,
                            onClick = { selectLanguage(null) },
                        )
                    }
                    items(SupportedAppLanguageTags, key = { it.first }) { (tag, name) ->
                        LanguageOption(
                            label = name,
                            selected = selectedLanguageTag.equals(tag, ignoreCase = true),
                            onClick = { selectLanguage(tag) },
                        )
                    }
                }
            },
            confirmButton = {},
        )
    }
}

@Composable
private fun LanguageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
