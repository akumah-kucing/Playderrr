package com.playder.ui.screens.settings

import android.os.Build
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.playder.R
import com.playder.constants.BlurStrengthPxKey
import com.playder.constants.CanvasThumbnailAnimationKey
import com.playder.constants.CanvasWifiOnlyKey
import com.playder.constants.DisableBlurKey
import com.playder.constants.EnableElementBlurAnimatorKey
import com.playder.constants.EnableProgressiveBlurKey
import com.playder.constants.LyricsHighBloomKey
import com.playder.constants.LyricsMotionStyle
import com.playder.constants.LyricsMotionStyleKey
import com.playder.constants.LyricsWaveAnimationKey
import com.playder.constants.PlayVideoBackgroundKey
import com.playder.constants.ShowFormatBadgeKey
import com.playder.constants.ShowFormatSampleRateKey
import com.playder.constants.YtVideoBackgroundLyricsSyncKey
import com.playder.ui.component.AppSwitch
import com.playder.ui.component.DefaultPlayerSeekBarSlider
import com.playder.ui.component.SettingsGroup
import com.playder.ui.component.SettingsGroupStyle
import com.playder.ui.component.SettingsItem
import com.playder.ui.component.SubSettingsScaffold
import com.playder.ui.component.SubSettingsSearchBar
import com.playder.ui.theme.InterFontFamily
import com.playder.utils.rememberEnumPreference
import com.playder.utils.rememberPreference
import kotlin.math.roundToInt

@Composable
fun AppearanceSettingsScreen(navController: NavController) {
    val playerConnection = com.playder.LocalPlayerConnection.current
    val context = androidx.compose.ui.platform.LocalContext.current
    var query by remember { mutableStateOf("") }

    val (playVideoBackground, onPlayVideoBackgroundChange) = rememberPreference(
        PlayVideoBackgroundKey,
        defaultValue = false
    )
    val (lyricsSync, onLyricsSyncChange) = rememberPreference(
        YtVideoBackgroundLyricsSyncKey,
        defaultValue = false
    )
    val (canvasEnabled, onCanvasEnabledChange) = rememberPreference(
        CanvasThumbnailAnimationKey,
        defaultValue = true
    )
    val (canvasWifiOnly, onCanvasWifiOnlyChange) = rememberPreference(
        CanvasWifiOnlyKey,
        defaultValue = true
    )
    val (disableBlur, onDisableBlurChange) = rememberPreference(
        DisableBlurKey,
        defaultValue = false
    )
    var blurQuality by com.playder.utils.rememberEnumPreference(
        com.playder.constants.BlurQualityKey,
        com.playder.constants.BlurQuality.HIGH,
    )
    val (progressiveBlur, onProgressiveBlurChange) = rememberPreference(
        EnableProgressiveBlurKey,
        defaultValue = true
    )
    val (elementBlurAnimator, onElementBlurAnimatorChange) = rememberPreference(
        EnableElementBlurAnimatorKey,
        defaultValue = true
    )
    val (blurStrengthPx, onBlurStrengthPxChange) = rememberPreference(
        BlurStrengthPxKey,
        defaultValue = 25f
    )
    val (lyricsWaveAnimation, onLyricsWaveAnimationChange) = rememberPreference(
        LyricsWaveAnimationKey,
        defaultValue = true
    )
    val (lyricsHighBloom, onLyricsHighBloomChange) = rememberPreference(
        LyricsHighBloomKey,
        defaultValue = true
    )
    val (formatBadge, onFormatBadgeChange) = rememberPreference(
        ShowFormatBadgeKey,
        defaultValue = true
    )
    val (formatSampleRate, onFormatSampleRateChange) = rememberPreference(
        ShowFormatSampleRateKey,
        defaultValue = false
    )
    val (lyricsMotionStyle, onLyricsMotionStyleChange) = rememberEnumPreference(
        LyricsMotionStyleKey,
        defaultValue = LyricsMotionStyle.MOTION,
    )

    SubSettingsScaffold(
        title = stringResource(R.string.appearance),
        onBack = { navController.navigateUp() },
        searchBar = { SubSettingsSearchBar(query = query, onQueryChange = { query = it }) },
    ) {
        Text(
            text = stringResource(R.string.loc_appearance_heading),
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
        )

        val playerPageItems = listOf(
            SettingsItem(
                title = { Text(stringResource(R.string.loc_customize_player)) },
                highlightKey = stringResource(R.string.loc_customize_player),
                descriptionText = stringResource(R.string.loc_customize_player_desc),
                icon = painterResource(R.drawable.album),
                iconShape = CircleShape,
                // the player is edited right where it plays: it opens and shrinks into its selector
                onClick = {
                    if (playerConnection?.player?.currentMediaItem != null) {
                        playerConnection.requestPlayerStudio()
                    } else {
                        android.widget.Toast.makeText(context, R.string.loc_customize_player_hint, android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
            ),
            SettingsItem(
                title = { Text(stringResource(R.string.loc_customize_lyrics)) },
                highlightKey = stringResource(R.string.loc_customize_lyrics),
                descriptionText = stringResource(R.string.loc_customize_lyrics_desc),
                icon = painterResource(R.drawable.lyrics),
                iconShape = CircleShape,
                onClick = { onLyricsWaveAnimationChange(!lyricsWaveAnimation) },
            ),
        ).filter {
            query.isBlank() || it.highlightKey?.contains(query, ignoreCase = true) == true ||
                it.descriptionText?.contains(query, ignoreCase = true) == true
        }

        if (playerPageItems.isNotEmpty()) {
            SettingsGroup(
                style = SettingsGroupStyle.Grouped,
                items = playerPageItems,
            )
            Spacer(Modifier.height(14.dp))
        }

        val playerToggleItems = listOf(
            SettingsItem(
                title = { Text(stringResource(R.string.loc_lyrics_animation)) },
                highlightKey = stringResource(R.string.loc_lyrics_animation),
                descriptionText = stringResource(
                    if (lyricsMotionStyle == LyricsMotionStyle.MOTION) R.string.loc_lyrics_motion_desc else R.string.loc_lyrics_legacy_desc
                ),
                icon = painterResource(R.drawable.lyrics),
                iconShape = CircleShape,
                onClick = {
                    onLyricsMotionStyleChange(
                        if (lyricsMotionStyle == LyricsMotionStyle.MOTION) LyricsMotionStyle.LEGACY else LyricsMotionStyle.MOTION
                    )
                },
                trailingContent = {
                    LyricsStyleToggle(selected = lyricsMotionStyle, onSelect = onLyricsMotionStyleChange)
                },
            ),
            SettingsItem(
                title = { Text(stringResource(R.string.loc_lyrics_shader)) },
                highlightKey = "Lyrics letter animation",
                // style 1's rise is drawn, not shaded, so it keeps working with glow off
                descriptionText = stringResource(R.string.loc_lyrics_shader_desc),
                icon = painterResource(R.drawable.biotech),
                iconShape = CircleShape,
                onClick = { onLyricsWaveAnimationChange(!lyricsWaveAnimation) },
                trailingContent = {
                    AppSwitch(
                        checked = lyricsWaveAnimation,
                        onCheckedChange = onLyricsWaveAnimationChange,
                    )
                },
            ),
            SettingsItem(
                title = { Text(stringResource(R.string.loc_high_quality_bloom)) },
                highlightKey = stringResource(R.string.loc_high_quality_bloom),
                descriptionText = stringResource(R.string.loc_high_quality_bloom_desc),
                icon = painterResource(R.drawable.sparks),
                iconShape = CircleShape,
                isVisible = lyricsWaveAnimation,
                isSubOption = true,
                onClick = { onLyricsHighBloomChange(!lyricsHighBloom) },
                trailingContent = {
                    AppSwitch(
                        checked = lyricsHighBloom,
                        onCheckedChange = onLyricsHighBloomChange,
                        enabled = lyricsWaveAnimation,
                    )
                },
            ),
            SettingsItem(
                title = { Text(stringResource(R.string.loc_audio_format_badge)) },
                highlightKey = stringResource(R.string.loc_audio_format_badge),
                descriptionText = stringResource(R.string.loc_audio_format_badge_desc),
                icon = painterResource(R.drawable.graphic_eq),
                iconShape = CircleShape,
                onClick = { onFormatBadgeChange(!formatBadge) },
                trailingContent = {
                    AppSwitch(
                        checked = formatBadge,
                        onCheckedChange = onFormatBadgeChange,
                    )
                },
            ),
            SettingsItem(
                title = { Text(stringResource(R.string.loc_show_sample_rate)) },
                highlightKey = stringResource(R.string.loc_show_sample_rate),
                descriptionText = stringResource(R.string.loc_sample_rate_desc),
                icon = painterResource(R.drawable.speed),
                iconShape = CircleShape,
                isVisible = formatBadge,
                isSubOption = true,
                onClick = { onFormatSampleRateChange(!formatSampleRate) },
                trailingContent = {
                    AppSwitch(
                        checked = formatSampleRate,
                        onCheckedChange = onFormatSampleRateChange,
                        enabled = formatBadge,
                    )
                },
            ),
            SettingsItem(
                title = { Text(stringResource(R.string.loc_youtube_background)) },
                highlightKey = "Yt video background",
                descriptionText = stringResource(R.string.loc_youtube_background_desc),
                icon = painterResource(R.drawable.slow_motion_video),
                iconShape = CircleShape,
                onClick = { onPlayVideoBackgroundChange(!playVideoBackground) },
                trailingContent = {
                    AppSwitch(
                        checked = playVideoBackground,
                        onCheckedChange = onPlayVideoBackgroundChange,
                    )
                },
            ),
            SettingsItem(
                title = { Text(stringResource(R.string.loc_timestamp_matching)) },
                descriptionText = stringResource(R.string.loc_based_on_subtitle),
                icon = painterResource(R.drawable.lyrics),
                iconShape = CircleShape,
                isVisible = playVideoBackground,
                isSubOption = true,
                onClick = { onLyricsSyncChange(!lyricsSync) },
                trailingContent = {
                    AppSwitch(
                        checked = lyricsSync,
                        onCheckedChange = onLyricsSyncChange,
                        enabled = playVideoBackground,
                    )
                },
            ),
            SettingsItem(
                title = { Text(stringResource(R.string.loc_animated_canvas)) },
                highlightKey = stringResource(R.string.loc_animated_canvas),
                descriptionText = stringResource(R.string.loc_animated_canvas_desc),
                icon = painterResource(R.drawable.sparks),
                iconShape = CircleShape,
                onClick = { onCanvasEnabledChange(!canvasEnabled) },
                trailingContent = {
                    AppSwitch(
                        checked = canvasEnabled,
                        onCheckedChange = onCanvasEnabledChange,
                    )
                },
            ),
            SettingsItem(
                title = { Text(stringResource(R.string.loc_canvas_wifi_only)) },
                descriptionText = stringResource(R.string.loc_skip_mobile_data),
                icon = painterResource(R.drawable.wifi_proxy),
                iconShape = CircleShape,
                isVisible = canvasEnabled,
                isSubOption = true,
                onClick = { onCanvasWifiOnlyChange(!canvasWifiOnly) },
                trailingContent = {
                    AppSwitch(
                        checked = canvasWifiOnly,
                        onCheckedChange = onCanvasWifiOnlyChange,
                        enabled = canvasEnabled,
                    )
                },
            ),
        ).filter {
            query.isBlank() || it.highlightKey?.contains(query, ignoreCase = true) == true ||
                it.descriptionText?.contains(query, ignoreCase = true) == true
        }

        if (playerToggleItems.isNotEmpty()) {
            SettingsGroup(
                style = SettingsGroupStyle.Grouped,
                items = playerToggleItems,
            )
            Spacer(Modifier.height(18.dp))
        }

        Text(
            text = stringResource(R.string.loc_global_appearance),
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
        )

        val globalToggleItems = listOf(
            SettingsItem(
                title = { Text(androidx.compose.ui.res.stringResource(com.playder.R.string.player_background_blur)) },
                highlightKey = "Blur",
                descriptionText = stringResource(R.string.loc_player_blur_desc),
                icon = painterResource(R.drawable.contrast),
                iconShape = CircleShape,
                onClick = { onDisableBlurChange(!disableBlur) },
                trailingContent = {
                    AppSwitch(
                        checked = !disableBlur,
                        onCheckedChange = { enabled -> onDisableBlurChange(!enabled) },
                    )
                },
            ),
            SettingsItem(
                title = { Text(stringResource(R.string.loc_blur_quality)) },
                highlightKey = stringResource(R.string.loc_blur_quality),
                descriptionText = if (blurQuality == com.playder.constants.BlurQuality.LOW) {
                    stringResource(R.string.loc_blur_low_desc)
                } else {
                    stringResource(R.string.loc_blur_high_desc)
                },
                icon = painterResource(R.drawable.tune),
                iconShape = CircleShape,
                isVisible = !disableBlur,
                isSubOption = true,
                onClick = {
                    blurQuality = if (blurQuality == com.playder.constants.BlurQuality.LOW) {
                        com.playder.constants.BlurQuality.HIGH
                    } else {
                        com.playder.constants.BlurQuality.LOW
                    }
                },
                trailingContent = {
                    Text(
                        text = stringResource(if (blurQuality == com.playder.constants.BlurQuality.LOW) R.string.loc_blur_low else R.string.loc_blur_high),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.70f),
                    )
                },
            ),
            SettingsItem(
                title = { Text(stringResource(R.string.loc_progressive_blur)) },
                descriptionText = stringResource(R.string.loc_progressive_blur_desc),
                icon = painterResource(R.drawable.gradient),
                iconShape = CircleShape,
                isVisible = !disableBlur,
                isSubOption = true,
                onClick = { onProgressiveBlurChange(!progressiveBlur) },
                trailingContent = {
                    AppSwitch(
                        checked = progressiveBlur,
                        onCheckedChange = onProgressiveBlurChange,
                        enabled = !disableBlur,
                    )
                },
            ),
            SettingsItem(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(stringResource(R.string.loc_blur_strength))
                        Text(
                            text = "${blurStrengthPx.roundToInt()}px",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.70f),
                        )
                    }
                },
                description = {
                    DefaultPlayerSeekBarSlider(
                        value = blurStrengthPx,
                        onValueChange = { onBlurStrengthPxChange(it) },
                        valueRange = 10f..50f,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                },
                icon = painterResource(R.drawable.tune),
                iconShape = CircleShape,
                isVisible = !disableBlur,
                isSubOption = true,
            ),
            SettingsItem(
                title = { Text(stringResource(R.string.loc_animation)) },
                highlightKey = stringResource(R.string.loc_animation),
                descriptionText = stringResource(R.string.loc_ui_animation_desc),
                icon = painterResource(R.drawable.speed),
                iconShape = CircleShape,
                onClick = { onElementBlurAnimatorChange(!elementBlurAnimator) },
                trailingContent = {
                    AppSwitch(
                        checked = elementBlurAnimator,
                        onCheckedChange = onElementBlurAnimatorChange,
                    )
                },
            ),
        ).filter {
            query.isBlank() || it.highlightKey?.contains(query, ignoreCase = true) == true ||
                it.descriptionText?.contains(query, ignoreCase = true) == true
        }

        if (globalToggleItems.isNotEmpty()) {
            SettingsGroup(
                style = SettingsGroupStyle.Grouped,
                items = globalToggleItems,
            )
        }

        Spacer(Modifier.height(140.dp))
    }
}

/** two-option pill for the lyrics animation style: "1" is the new motion, "2" the legacy wave */
@Composable
private fun LyricsStyleToggle(
    selected: LyricsMotionStyle,
    onSelect: (LyricsMotionStyle) -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        LyricsMotionStyle.entries.forEach { style ->
            val isSelected = style == selected
            val background by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color.Transparent,
                label = "lyricsStyleChip",
            )
            val content by animateColorAsState(
                targetValue = if (isSelected) Color.Black else Color.White.copy(alpha = 0.7f),
                label = "lyricsStyleChipText",
            )
            Text(
                text = style.displayName,
                color = content,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(background)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onSelect(style) }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
    }
}
