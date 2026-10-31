// settings → modules: the sources playder plays from besides youtube, each with its account, switch, quality and a playback test

package com.playder.ui.screens.settings.modules

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.playder.LocalPlayerConnection
import com.playder.R
import com.playder.module.AccountState
import com.playder.module.DiagnosticStep
import com.playder.module.TrackQuery
import com.playder.modules.ModuleEntry
import com.playder.ui.component.SubSettingsScaffold
import com.playder.ui.screens.audiolab.LabCard
import com.playder.ui.screens.audiolab.LabChip
import com.playder.ui.screens.audiolab.LabChips
import com.playder.ui.screens.audiolab.LabColors
import com.playder.ui.screens.audiolab.LabSectionLabel
import com.playder.ui.screens.audiolab.LabTextAction
import com.playder.ui.screens.audiolab.LabToggleRow
import com.playder.ui.screens.settings.importsync.CaptureWebView
import com.playder.ui.theme.InterFontFamily
import com.playder.viewmodels.ModulesViewModel
import java.util.Locale

const val ModulesRoute = "modules"
const val ModuleRoutePattern = "module/{id}"
const val ModuleSignInRoutePattern = "module_signin/{id}"
fun moduleRoute(id: String) = "module/$id"
fun moduleSignInRoute(id: String) = "module_signin/$id"

@Composable
internal fun localizedModuleChoiceLabel(moduleId: String, label: String): String =
    if (moduleId == "applemusic") {
        stringResource(if (label == "Region") R.string.loc_audio_quality_region_label else R.string.loc_audio_streaming)
    } else label

@Composable
internal fun localizedModuleOptionLabel(moduleId: String, optionId: String, label: String): String =
    if (moduleId == "applemusic" && optionId == "auto") {
        stringResource(R.string.loc_audio_quality_region_account)
    } else if (moduleId == "applemusic" && optionId.length == 2) {
        Locale("", optionId.uppercase()).getDisplayCountry(Locale.getDefault()).ifBlank { label }
    } else label

@Composable
internal fun localizedModuleOptionDescription(moduleId: String, optionId: String, description: String): String =
    if (moduleId != "applemusic") description else when {
        description == "Apple Music's standard stereo stream" ->
            stringResource(R.string.loc_audio_quality_apple_stream)
        description == "A quarter of the data, for slow or metered networks" ->
            stringResource(R.string.loc_audio_quality_metered_desc)
        description == "Searches the catalog of your Apple account's country" ->
            stringResource(R.string.loc_audio_quality_region_account_desc)
        description.startsWith("Searches the ") && optionId.length == 2 ->
            stringResource(
                R.string.loc_audio_quality_region_desc,
                Locale("", optionId.uppercase()).getDisplayCountry(Locale.getDefault()),
            )
        else -> description
    }

@Composable
private fun moduleDescription(id: String, fallback: String): String = when (id) {
    "applemusic" -> stringResource(R.string.loc_module_apple_description)
    "monochrome" -> stringResource(R.string.loc_module_monochrome_description)
    else -> fallback
}

private fun logoFor(id: String): Int? = when (id) {
    "applemusic" -> R.drawable.provider_apple_music
    "monochrome" -> R.drawable.provider_monochrome
    else -> null
}

@Composable
private fun statusOf(entry: ModuleEntry): String = when (val account = entry.account) {
    AccountState.SignedOut -> stringResource(R.string.loc_module_signin)
    is AccountState.Expired -> "${stringResource(R.string.loc_module_signin_again)} · ${account.reason}"
    is AccountState.SignedIn -> stringResource(
        if (entry.enabled) R.string.loc_module_signed_in_on else R.string.loc_module_signed_in_off,
        stringResource(if (entry.enabled) R.string.loc_module_state_on else R.string.loc_module_state_off),
        account.label,
    )
}

@Composable
fun ModulesScreen(navController: NavController, viewModel: ModulesViewModel = hiltViewModel()) {
    val entries by viewModel.entries.collectAsState()
    SubSettingsScaffold(title = stringResource(R.string.modules_category), onBack = { navController.navigateUp() }) {
        Text(
            stringResource(R.string.loc_modules_description),
            fontFamily = InterFontFamily, fontSize = 14.sp, lineHeight = 19.sp, color = LabColors.Secondary,
        )
        Spacer(Modifier.height(16.dp))
        entries.forEach { entry ->
            val id = entry.module.info.id
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(LabColors.Card)
                    .clickable { navController.navigate(moduleRoute(id)) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ModuleLogo(id)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(entry.module.info.name, fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                    Text(statusOf(entry), fontFamily = InterFontFamily, fontSize = 12.sp, color = LabColors.Secondary)
                    Text(
                        "${stringResource(if (entry.packageName == null) R.string.loc_module_builtin else R.string.loc_module_installed)} · v${entry.module.info.version}",
                        fontFamily = InterFontFamily, fontSize = 11.sp, color = LabColors.Faint,
                    )
                }
                Icon(painterResource(R.drawable.navigate_next), null, tint = LabColors.Secondary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun ModuleLogo(id: String, size: Int = 46) {
    val logo = logoFor(id)
    Box(
        modifier = Modifier.size(size.dp).clip(RoundedCornerShape((size * 0.26f).dp)).background(LabColors.Row),
        contentAlignment = Alignment.Center,
    ) {
        if (logo != null) {
            Image(painterResource(logo), null, contentScale = ContentScale.Crop, modifier = Modifier.size(size.dp))
        } else {
            Icon(painterResource(R.drawable.music_note), null, tint = Color.White, modifier = Modifier.size((size / 2).dp))
        }
    }
}

@Composable
fun ModuleScreen(navController: NavController, id: String, viewModel: ModulesViewModel = hiltViewModel()) {
    val entries by viewModel.entries.collectAsState()
    val steps by viewModel.steps.collectAsState()
    val testing by viewModel.testing.collectAsState()
    val entry = entries.firstOrNull { it.module.info.id == id }
    val playerConnection = LocalPlayerConnection.current
    val playing = playerConnection?.mediaMetadata?.collectAsState()?.value

    SubSettingsScaffold(
        title = entry?.module?.info?.name ?: stringResource(R.string.loc_module_title),
        backLabel = stringResource(R.string.loc_module_back_modules),
        onBack = { navController.navigateUp() },
    ) {
        if (entry == null) return@SubSettingsScaffold
        val module = entry.module
        val signedIn = entry.account is AccountState.SignedIn

        LabCard(padding = 16.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ModuleLogo(id, size = 56)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(module.info.name, fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                    Text(statusOf(entry), fontFamily = InterFontFamily, fontSize = 13.sp, color = LabColors.Secondary)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                moduleDescription(id, module.info.description),
                fontFamily = InterFontFamily, fontSize = 13.sp, lineHeight = 18.sp, color = LabColors.Secondary,
            )
            Spacer(Modifier.height(14.dp))
            if (!signedIn && module.signIn != null) {
                PrimaryButton(
                    text = if (entry.account is AccountState.Expired) stringResource(R.string.loc_module_signin_again)
                    else stringResource(R.string.loc_module_signin_to, module.info.name),
                    onClick = { navController.navigate(moduleSignInRoute(id)) },
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabTextAction(
                        stringResource(if (module.signIn != null) R.string.loc_module_check_account else R.string.loc_module_check_server),
                        R.drawable.refresh,
                    ) { viewModel.verify(id) }
                    Spacer(Modifier.weight(1f))
                    if (module.signIn != null) LabTextAction(stringResource(R.string.loc_module_signout), R.drawable.close) { viewModel.signOut(id) }
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        LabCard(padding = 10.dp) {
            LabToggleRow(
                title = stringResource(R.string.loc_module_play_from, module.info.name),
                subtitle = stringResource(R.string.loc_module_play_from_detail, module.info.name),
                checked = entry.enabled && signedIn,
                enabled = signedIn,
                onCheckedChange = { viewModel.setEnabled(id, it) },
            )
        }
        Spacer(Modifier.height(16.dp))

        module.choices.forEach { choice ->
            if (choice.options.isEmpty()) return@forEach
            // a pick the module no longer offers (spatial, before it went) reads as the default
            val picked = entry.picks[choice.id]?.takeIf { p -> choice.options.any { it.id == p } } ?: choice.options.first().id
            LabSectionLabel(localizedModuleChoiceLabel(id, choice.label))
            LabChips(
                chips = choice.options.map { LabChip(it.id, localizedModuleOptionLabel(id, it.id, it.label)) },
                selectedKey = picked,
                onSelect = { viewModel.setPick(id, choice.id, it) },
            )
            choice.options.firstOrNull { it.id == picked }?.let { option ->
                Text(
                    localizedModuleOptionDescription(id, option.id, option.description),
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = LabColors.Secondary,
                    modifier = Modifier.padding(top = 8.dp, start = 6.dp))
            }
            Spacer(Modifier.height(16.dp))
        }

        LabSectionLabel(stringResource(R.string.loc_module_test_playback))
        LabCard(padding = 14.dp) {
            val track = playing?.let {
                TrackQuery(
                    title = it.title,
                    artists = it.artists.map { a -> a.name },
                    album = it.album?.title,
                    durationMs = it.duration.takeIf { d -> d > 0 }?.times(1000L),
                )
            } ?: TrackQuery("Espresso", listOf("Sabrina Carpenter"), durationMs = 175_000)
            Text(
                stringResource(
                    R.string.loc_module_test_detail,
                    track.title,
                    if (module.signIn != null) stringResource(R.string.loc_module_license_detail) else "",
                ),
                fontFamily = InterFontFamily, fontSize = 13.sp, lineHeight = 18.sp, color = LabColors.Secondary,
            )
            Spacer(Modifier.height(12.dp))
            PrimaryButton(
                text = stringResource(if (testing) R.string.loc_module_testing else R.string.loc_module_run_test),
                enabled = signedIn && !testing,
            ) {
                viewModel.runTest(id, track)
            }
            Column(Modifier.animateContentSize()) {
                steps.forEach { StepRow(it) }
            }
            if (!testing && steps.isNotEmpty() && steps.all { it.ok == true }) {
                Text(
                    stringResource(R.string.loc_module_test_success, module.info.name),
                    fontFamily = InterFontFamily, fontSize = 13.sp, color = Color.White,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        if (id == "applemusic") Text(
            stringResource(R.string.loc_module_unavailable),
            fontFamily = InterFontFamily, fontSize = 12.sp, lineHeight = 17.sp, color = LabColors.Faint,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
        Spacer(Modifier.height(120.dp))
    }

}

@Composable
private fun StepRow(step: DiagnosticStep) {
    Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
            when (step.ok) {
                null -> CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(15.dp))
                true -> Icon(painterResource(R.drawable.check), null, tint = Color(0xFF8FD98B), modifier = Modifier.size(18.dp))
                false -> Icon(painterResource(R.drawable.close), null, tint = Color(0xFFFF8A80), modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(step.name, fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
            Text(step.detail, fontFamily = InterFontFamily, fontSize = 12.sp, lineHeight = 16.sp, color = LabColors.Secondary)
        }
    }
}

@Composable
private fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (enabled) Color.White else LabColors.Row)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
            color = if (enabled) Color.Black else LabColors.Faint)
    }
}

/** the service's own sign-in page; it closes itself once the module has its account */
@Composable
fun ModuleSignInScreen(navController: NavController, id: String, viewModel: ModulesViewModel = hiltViewModel()) {
    val entries by viewModel.entries.collectAsState()
    val module = entries.firstOrNull { it.module.info.id == id }?.module ?: return
    val spec = module.signIn ?: return
    DisposableEffect(Unit) {
        viewModel.resetSignIn()
        onDispose { }
    }
    CaptureWebView(
        title = spec.title,
        subtitle = spec.subtitle,
        startUrl = spec.startUrl,
        playerOrigins = spec.sessionOrigins,
        desktopSite = spec.desktopSite,
        onHeader = { name, value, url ->
            viewModel.onSignInHeader(id, name, value, url) { navController.navigateUp() }
        },
        onTick = {},
        onExit = { navController.navigateUp() },
    )
}
