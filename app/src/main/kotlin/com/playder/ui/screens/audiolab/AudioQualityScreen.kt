// audio lab's quality page: streaming tiers per provider, download quality and the provider order

package com.playder.ui.screens.audiolab

import android.content.Context
import android.net.ConnectivityManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.playder.module.AccountState
import com.playder.module.applemusic.AppleMusicModule
import com.playder.module.monochrome.MonochromeModule
import com.playder.modules.SourceModules
import com.playder.ui.screens.settings.modules.localizedModuleOptionDescription
import com.playder.ui.screens.settings.modules.localizedModuleOptionLabel
import com.playder.viewmodels.ModulesViewModel
import com.playder.R
import com.playder.constants.AudioQuality
import com.playder.constants.AudioQualityKey
import com.playder.eq.audio.AudioTap
import com.playder.ui.theme.InterFontFamily
import com.playder.utils.rememberEnumPreference
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin

private class QualityOption(val quality: AudioQuality, val title: String, val description: String, val format: String)

/** what the picture shows: the quality of whichever provider plays first */
private class Effective(val quality: AudioQuality, val title: String, val format: String)

@Composable
fun AudioQualityScreen(navController: NavController, modules: ModulesViewModel = hiltViewModel()) {
    val (stored, onQuality) = rememberEnumPreference(AudioQualityKey, defaultValue = AudioQuality.AUTO)
    val (downloadQuality, onDownloadQuality) = rememberEnumPreference(
        com.playder.constants.DownloadQualityKey,
        defaultValue = com.playder.constants.DownloadQuality.HIGH,
    )
    // the tiers Monochrome used to serve from here belong to its module now
    val quality = if (stored.usesMonochrome) AudioQuality.HIGH else stored
    val entries by modules.entries.collectAsState()
    val order by modules.order.collectAsState()
    val youTubeOn by modules.youTubeEnabled.collectAsState()
    val context = LocalContext.current
    val metered = remember { (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.isActiveNetworkMetered == true }
    val youTubeOptions = listOf(
        QualityOption(AudioQuality.AUTO, stringResource(R.string.audio_quality_auto), stringResource(R.string.loc_audio_quality_auto_desc), stringResource(R.string.loc_audio_quality_format_adaptive)),
        QualityOption(AudioQuality.HIGH, stringResource(R.string.audio_quality_high), stringResource(R.string.loc_audio_quality_high_desc), "~160 kbps · Opus"),
        QualityOption(AudioQuality.MEDIUM, stringResource(R.string.loc_audio_quality_medium_label), stringResource(R.string.loc_audio_quality_medium_desc), "~128 kbps · AAC"),
        QualityOption(AudioQuality.LOW, stringResource(R.string.audio_quality_low), stringResource(R.string.loc_audio_quality_low_desc), "~50–70 kbps · Opus"),
    )

    val usable = { id: String ->
        if (id == SourceModules.YouTubeId) youTubeOn
        else entries.any { it.module.info.id == id && it.enabled && it.account is AccountState.SignedIn }
    }
    // the first provider in the order that's on plays everything it has
    val first = order.firstOrNull(usable)
    val effective = when (first) {
        null -> Effective(AudioQuality.LOW, stringResource(R.string.loc_audio_effect_none), stringResource(R.string.loc_audio_effect_enable_provider))
        MonochromeModule.Id -> Effective(AudioQuality.LOSSLESS, "Monochrome", "FLAC · lossless")
        AppleMusicModule.Id -> {
            val pick = entries.firstOrNull { it.module.info.id == first }?.picks?.get(AppleMusicModule.ChoiceQuality)
            if (pick == AppleMusicModule.OptionHe64) Effective(AudioQuality.MEDIUM, "Apple Music", "HE-AAC 64")
            else Effective(AudioQuality.HIGH, "Apple Music", "AAC 256")
        }
        else -> {
            val tier = if (quality == AudioQuality.AUTO) (if (metered) AudioQuality.MEDIUM else AudioQuality.HIGH) else quality
            val option = youTubeOptions.first { it.quality == tier }
            Effective(tier, "YouTube · ${option.title}", option.format)
        }
    }

    AudioLabPage(
        title = LabTitle("Audio", stringResource(R.string.loc_audio_quality), firstSize = 40.sp, secondSize = 64.sp),
        onBack = { navController.navigateUp() },
        subtitle = "${effective.title} · ${effective.format}",
    ) {
        LabSheetTitle(stringResource(R.string.loc_audio_streaming))
        LabGroup {
            ProviderCaption(
                logo = R.drawable.provider_youtube_music,
                name = "YouTube Music",
                status = when {
                    youTubeOn -> stringResource(R.string.loc_audio_status_play_other)
                    order.none(usable) -> stringResource(R.string.loc_audio_status_off_provider)
                    else -> stringResource(R.string.loc_audio_status_off_skipped)
                },
                on = youTubeOn,
            )
            youTubeOptions.forEach { option ->
                QualityRow(option.title, option.description, selected = quality == option.quality, enabled = youTubeOn) { onQuality(option.quality) }
            }

            // each module's own qualities, under its name
            entries.forEach { entry ->
                val id = entry.module.info.id
                val active = entry.enabled && entry.account is AccountState.SignedIn
                Spacer(Modifier.height(6.dp))
                ProviderCaption(logo = providerLogo(id), name = entry.module.info.name, status = moduleStatus(entry), on = active)
                val qualityChoice = entry.module.choices.firstOrNull { it.id == "quality" }
                if (qualityChoice != null) {
                    val picked = entry.picks[qualityChoice.id]?.takeIf { p -> qualityChoice.options.any { it.id == p } }
                        ?: qualityChoice.options.first().id
                    qualityChoice.options.forEach { option ->
                        QualityRow(
                            localizedModuleOptionLabel(id, option.id, option.label),
                            localizedModuleOptionDescription(id, option.id, option.description),
                            selected = picked == option.id,
                            enabled = active,
                        ) {
                            modules.setPick(id, qualityChoice.id, option.id)
                        }
                    }
                } else if (id == MonochromeModule.Id) {
                    QualityRow(
                        stringResource(R.string.loc_audio_quality_lossless_label),
                        stringResource(R.string.loc_audio_quality_lossless_desc),
                        selected = true,
                        enabled = active,
                    ) {}
                }
                LabTextAction(stringResource(R.string.loc_audio_provider_settings, entry.module.info.name), R.drawable.navigate_next) {
                    navController.navigate(com.playder.ui.screens.settings.modules.moduleRoute(id))
                }
            }
        }

        // downloads are saved apart from what plays: lossless downloads monochrome's flac whatever the provider order says, since a download is kept
        Spacer(Modifier.height(24.dp))
        LabSheetTitle(stringResource(R.string.loc_audio_quality_download_title))
        LabGroup {
            com.playder.constants.DownloadQuality.entries.forEach { option ->
                val (title, description) = when (option) {
                    com.playder.constants.DownloadQuality.LOSSLESS -> stringResource(R.string.loc_audio_download_lossless_title) to stringResource(R.string.loc_audio_download_lossless_desc)
                    com.playder.constants.DownloadQuality.HIGH -> stringResource(R.string.loc_audio_download_high_title) to stringResource(R.string.loc_audio_download_high_desc)
                    com.playder.constants.DownloadQuality.MEDIUM -> stringResource(R.string.loc_audio_download_medium_title) to stringResource(R.string.loc_audio_download_medium_desc)
                    com.playder.constants.DownloadQuality.LOW -> stringResource(R.string.loc_audio_download_low_title) to stringResource(R.string.loc_audio_download_low_desc)
                }
                QualityRow(title, description, selected = downloadQuality == option, enabled = true) {
                    onDownloadQuality(option)
                }
            }
        }
        Footnote(stringResource(R.string.loc_audio_download_note))

        Spacer(Modifier.height(24.dp))
        LabSheetTitle(stringResource(R.string.loc_audio_provider))
        ProviderOrder(
            order = order,
            name = modules::nameOf,
            logo = ::providerLogo,
            usable = usable,
            status = { id ->
                if (id == SourceModules.YouTubeId) {
                    if (youTubeOn) stringResource(R.string.loc_module_state_on) else stringResource(R.string.loc_audio_off_tap)
                } else {
                    entries.firstOrNull { it.module.info.id == id }?.let { moduleStatus(it) } ?: stringResource(R.string.loc_audio_not_installed)
                }
            },
            onTap = { id ->
                if (id == SourceModules.YouTubeId) {
                    modules.setEnabled(id, !youTubeOn)
                } else {
                    val entry = entries.firstOrNull { it.module.info.id == id }
                    // not signed in: there's nothing to switch on yet, so its sign-in opens
                    if (entry == null || entry.account !is AccountState.SignedIn) {
                        navController.navigate(com.playder.ui.screens.settings.modules.moduleRoute(id))
                    } else {
                        modules.setEnabled(id, !entry.enabled)
                    }
                }
            },
            onReorder = modules::setOrder,
        )
        Footnote(
            stringResource(R.string.loc_audio_provider_note),
        )
    }
}

@Composable
private fun moduleStatus(entry: com.playder.modules.ModuleEntry): String = when {
    entry.account is AccountState.SignedOut -> stringResource(R.string.loc_audio_signin_to_play)
    entry.account is AccountState.Expired -> stringResource(R.string.loc_module_signin_again)
    entry.enabled -> stringResource(R.string.loc_module_state_on)
    else -> stringResource(R.string.loc_audio_off_tap)
}

@Composable
private fun Footnote(text: String) {
    Text(
        text,
        fontFamily = InterFontFamily, fontSize = 12.sp, lineHeight = 16.sp, color = LabColors.Secondary,
        modifier = Modifier.padding(top = 10.dp, start = 6.dp, end = 6.dp),
    )
}

internal fun providerLogo(id: String): Int? = when (id) {
    AppleMusicModule.Id -> R.drawable.provider_apple_music
    MonochromeModule.Id -> R.drawable.provider_monochrome
    SourceModules.YouTubeId -> R.drawable.provider_youtube_music
    else -> null
}

/** a provider's name over its qualities in the streaming group, dimmed while it's off */
@Composable
private fun ProviderCaption(logo: Int?, name: String, status: String, on: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.graphicsLayer { alpha = if (on) 1f else 0.45f }) { ProviderLogo(logo, size = 24) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(name, fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            Text(status, fontFamily = InterFontFamily, fontSize = 11.sp, lineHeight = 14.sp, color = LabColors.Secondary)
        }
    }
}

@Composable
internal fun ProviderLogo(logo: Int?, size: Int) {
    Box(
        modifier = Modifier.size(size.dp).clip(RoundedCornerShape((size * 0.26f).dp)).background(Color(0xFFD9D9D9)),
        contentAlignment = Alignment.Center,
    ) {
        if (logo != null) {
            androidx.compose.foundation.Image(
                painterResource(logo), null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.size(size.dp),
            )
        }
    }
}

@Composable
private fun QualityRow(title: String, description: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (selected) Color(0xFF2A2A2E) else Color(0xFF111113), tween(200), label = "row")
    val alpha by animateFloatAsState(if (enabled) 1f else 0.35f, tween(250), label = "enabled")
    val check by animateFloatAsState(if (selected) 1f else 0f, spring(dampingRatio = 0.5f, stiffness = 500f), label = "check")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha }
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(1.dp, if (selected) Color.White.copy(alpha = 0.22f) else Color.Transparent, RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            Text(description, fontFamily = InterFontFamily, fontSize = 11.sp, lineHeight = 14.sp, color = LabColors.Secondary)
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier.size(22.dp).graphicsLayer { scaleX = check; scaleY = check; this.alpha = check },
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.check), null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}
