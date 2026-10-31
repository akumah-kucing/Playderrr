// audio lab's equalizer: one curve shaped as a graph or as sliders, with an optional headphone correction

package com.playder.ui.screens.equalizer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.playder.R
import com.playder.eq.EqPreset
import com.playder.eq.EqPresets
import com.playder.eq.EqSettings
import com.playder.ui.component.ActionPromptDialog
import com.playder.ui.component.SubSettingsScaffold
import com.playder.ui.component.TextFieldDialog
import com.playder.ui.screens.audiolab.LabCard
import com.playder.ui.screens.audiolab.LabChip
import com.playder.ui.screens.audiolab.LabChips
import com.playder.ui.screens.audiolab.LabSectionLabel
import com.playder.ui.screens.audiolab.LabSegmented
import com.playder.ui.screens.audiolab.LabSlider
import com.playder.ui.screens.audiolab.LabTextAction
import com.playder.ui.screens.audiolab.LabToggleRow
import com.playder.ui.screens.audiolab.ListenToAudio
import com.playder.ui.screens.audiolab.animatedDim
import com.playder.viewmodels.AudioLabViewModel
import com.playder.viewmodels.isFlat
import kotlin.math.roundToInt

@Composable
fun EqualizerScreen(
    navController: NavController,
    viewModel: AudioLabViewModel = hiltViewModel(),
) {
    val eq by viewModel.eq.collectAsState()
    val presets by viewModel.presets.collectAsState()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var selectedNode by rememberSaveable { mutableIntStateOf(0) }
    var savingPreset by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<EqPreset?>(null) }
    val tabs = listOf(
        stringResource(R.string.loc_eq_graph_tab),
        stringResource(R.string.loc_eq_bands_tab),
        stringResource(R.string.loc_eq_autoeq_tab),
    )

    // the graph's spectrum listens while this page is open
    ListenToAudio()

    SubSettingsScaffold(
        title = stringResource(R.string.loc_audio_equalizer_title),
        backLabel = stringResource(R.string.loc_audio_lab_title),
        onBack = { navController.navigateUp() },
    ) {
        LabCard(padding = 10.dp) {
            LabToggleRow(
                title = stringResource(R.string.loc_audio_equalizer_title),
                subtitle = statusLine(eq, presets),
                checked = eq.enabled,
                onCheckedChange = viewModel::setEnabled,
                leadingDot = true,
            )
        }
        Spacer(Modifier.height(14.dp))
        LabSegmented(options = tabs, selectedIndex = tab, onSelect = { tab = it })
        Spacer(Modifier.height(18.dp))

        val dim = animatedDim(!eq.enabled)
        Column(Modifier.fillMaxWidth().graphicsLayer { alpha = dim }) {
            AnimatedContent(
                targetState = tab,
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (fadeIn(tween(220, 60)) + slideInHorizontally(spring(dampingRatio = 0.9f, stiffness = 420f)) { it / 8 * dir }) togetherWith fadeOut(tween(120))
                },
                label = "eqTab",
            ) { page ->
                Column(Modifier.fillMaxWidth()) {
                    when (page) {
                        0, 1 -> {
                            Presets(
                                eq = eq,
                                presets = presets,
                                onPick = viewModel::applyPreset,
                                onReset = viewModel::reset,
                                onSave = { savingPreset = true },
                                onLongPress = { id -> deleting = presets.firstOrNull { it.id == id } },
                            )
                            Spacer(Modifier.height(18.dp))
                            if (page == 0) {
                                EqGraphSection(
                                    nodes = eq.nodes,
                                    selected = selectedNode,
                                    onSelect = { selectedNode = it },
                                    onMove = { i, f, g -> viewModel.updateNode(i) { it.copy(frequency = f, gain = g) } },
                                    onAdd = { f, g -> viewModel.addNode(f, g).takeIf { it >= 0 }?.let { selectedNode = it } },
                                    onUpdate = viewModel::updateNode,
                                    onRemove = { i ->
                                        viewModel.removeNode(i)
                                        selectedNode = (i - 1).coerceAtLeast(0)
                                    },
                                    canAdd = eq.nodes.size < AudioLabViewModel.MaxNodes,
                                )
                            } else {
                                EqBandsSection(
                                    nodes = eq.nodes,
                                    onGain = { i, g -> viewModel.updateNode(i) { it.copy(gain = g) } },
                                )
                            }
                        }
                        else -> AutoEqTab(viewModel = viewModel, eq = eq)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Output(eq = eq, headroom = viewModel.autoHeadroom(eq), viewModel = viewModel)
        }
        Spacer(Modifier.height(120.dp))
    }

    if (savingPreset) {
        TextFieldDialog(
            title = { Text(stringResource(R.string.loc_eq_save_preset)) },
            placeholder = { Text(stringResource(R.string.loc_eq_preset_placeholder)) },
            onDone = viewModel::savePreset,
            onDismiss = { savingPreset = false },
        )
    }
    deleting?.let { preset ->
        ActionPromptDialog(
            title = stringResource(R.string.loc_eq_delete_preset, preset.name),
            onDismiss = { deleting = null },
            onCancel = { deleting = null },
            onConfirm = {
                viewModel.deletePreset(preset.id)
                deleting = null
            },
        )
    }
}

@Composable
private fun statusLine(eq: EqSettings, presets: List<EqPreset>): String {
    if (!eq.enabled) return stringResource(R.string.loc_eq_off)
    val curve = eq.presetId?.let { id ->
        EqPresets.BuiltIn.firstOrNull { it.id == id }?.let { localizedPresetName(it.id, it.name) }
            ?: presets.firstOrNull { it.id == id }?.name
    } ?: stringResource(R.string.loc_eq_custom)
    val correction = eq.correction?.takeIf { eq.correctionEnabled }?.model
    return when {
        eq.isFlat() -> stringResource(R.string.loc_eq_on_flat)
        correction != null && eq.presetId == EqPresets.FlatId -> stringResource(R.string.loc_eq_on_curve, correction)
        correction != null -> stringResource(R.string.loc_eq_on_curve_correction, curve, correction)
        else -> stringResource(R.string.loc_eq_on_curve, curve)
    }
}

@Composable
private fun localizedPresetName(id: String, fallback: String): String {
    val resource = when (id) {
        "flat" -> R.string.loc_eq_preset_flat
        "acoustic" -> R.string.loc_eq_preset_acoustic
        "bass_boost" -> R.string.loc_eq_preset_bass_boost
        "bass_cut" -> R.string.loc_eq_preset_bass_cut
        "classical" -> R.string.loc_eq_preset_classical
        "dance" -> R.string.loc_eq_preset_dance
        "deep" -> R.string.loc_eq_preset_deep
        "electronic" -> R.string.loc_eq_preset_electronic
        "hiphop" -> R.string.loc_eq_preset_hiphop
        "jazz" -> R.string.loc_eq_preset_jazz
        "loudness" -> R.string.loc_eq_preset_loudness
        "lounge" -> R.string.loc_eq_preset_lounge
        "piano" -> R.string.loc_eq_preset_piano
        "pop" -> R.string.loc_eq_preset_pop
        "rnb" -> R.string.loc_eq_preset_rnb
        "rock" -> R.string.loc_eq_preset_rock
        "small_speakers" -> R.string.loc_eq_preset_small_speakers
        "spoken" -> R.string.loc_eq_preset_spoken
        "treble_boost" -> R.string.loc_eq_preset_treble_boost
        "treble_cut" -> R.string.loc_eq_preset_treble_cut
        "vocal" -> R.string.loc_eq_preset_vocal
        else -> return fallback
    }
    return stringResource(resource)
}

@Composable
private fun Presets(
    eq: EqSettings,
    presets: List<EqPreset>,
    onPick: (String) -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    onLongPress: (String) -> Unit,
) {
    LabSectionLabel(stringResource(R.string.loc_eq_presets)) {
        LabTextAction(stringResource(R.string.loc_eq_reset), R.drawable.refresh, onClick = onReset)
        LabTextAction(stringResource(R.string.loc_eq_save), R.drawable.add, onClick = onSave)
    }
    val chips = buildList {
        if (eq.presetId == null) add(LabChip("custom", stringResource(R.string.loc_eq_custom)))
        presets.forEach { add(LabChip(it.id, it.name, dot = androidx.compose.ui.graphics.Color.White)) }
        EqPresets.BuiltIn.forEach { add(LabChip(it.id, localizedPresetName(it.id, it.name))) }
    }
    LabChips(
        chips = chips,
        selectedKey = eq.presetId ?: "custom",
        onSelect = { if (it != "custom") onPick(it) },
        onLongPress = { id -> if (presets.any { it.id == id }) onLongPress(id) },
    )
}

@Composable
private fun Output(eq: EqSettings, headroom: Double, viewModel: AudioLabViewModel) {
    LabSectionLabel(stringResource(R.string.loc_eq_output))
    LabCard(padding = 16.dp) {
        LabSlider(
            label = stringResource(R.string.loc_eq_preamp),
            valueText = (if (eq.preamp > 0) "+" else "") + String.format("%.1f dB", eq.preamp),
            value = eq.preamp.toFloat(),
            onValueChange = { viewModel.setPreamp((it * 2).roundToInt() / 2.0) },
            valueRange = -12f..6f,
        )
        Spacer(Modifier.height(6.dp))
        LabToggleRow(
            title = stringResource(R.string.loc_eq_auto_preamp),
            subtitle = if (eq.autoPreamp) {
                if (headroom < -0.05) stringResource(R.string.loc_eq_auto_preamp_lowering, String.format("%.1f", -headroom))
                else stringResource(R.string.loc_eq_auto_preamp_no_lowering)
            } else stringResource(R.string.loc_eq_auto_preamp_off),
            checked = eq.autoPreamp,
            onCheckedChange = viewModel::setAutoPreamp,
        )
    }
}
