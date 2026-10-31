// TransferSection.kt

package com.playder.ui.screens.settings.storage

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.playder.R
import com.playder.ui.component.LocalMenuState
import com.playder.ui.screens.library.showConfirm
import com.playder.viewmodels.TransferStep
import com.playder.viewmodels.TransferViewModel
import kotlinx.coroutines.launch

private val ZipTypes = arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream")

/** export everything to one zip for a new phone, or bring such a zip back in */
@Composable
internal fun TransferSection(viewModel: TransferViewModel = hiltViewModel()) {
    val export by viewModel.export.collectAsState()
    val import by viewModel.import.collectAsState()
    val menuState = LocalMenuState.current
    val scope = rememberCoroutineScope()
    var checkError by remember { mutableStateOf<String?>(null) }
    var waitingForPermission by remember { mutableStateOf<Uri?>(null) }
    val replaceTitle = stringResource(R.string.loc_storage_transfer_replace_title)
    val replaceMessage = stringResource(R.string.loc_storage_transfer_replace_message)
    val pickZip = stringResource(R.string.loc_storage_transfer_pick_zip)

    val exportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) viewModel.exportTo(uri)
    }
    // local songs come back through the phone's music library, a no still imports everything else
    val audioPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        waitingForPermission?.let(viewModel::importFrom)
        waitingForPermission = null
    }
    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        checkError = null
        scope.launch {
            viewModel.check(uri)
                .onSuccess { manifest ->
                    if (viewModel.wantsAudioPermission(manifest)) {
                        waitingForPermission = uri
                        audioPermission.launch(TransferViewModel.AudioPermission)
                    } else {
                        viewModel.importFrom(uri)
                    }
                }
                .onFailure { checkError = it.message }
        }
    }

    Text(
        stringResource(R.string.loc_storage_transfer_title),
        style = storageText(17f, FontWeight.SemiBold),
        modifier = Modifier.padding(horizontal = 2.dp),
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 14.dp)) {
        StorageActionButton(
            label = stringResource(R.string.loc_storage_transfer_export),
            detail = when (val step = export) {
                is TransferStep.Working -> step.progress?.let {
                    stringResource(R.string.loc_storage_transfer_export_progress, (it * 100).toInt())
                } ?: stringResource(R.string.loc_storage_transfer_exporting)
                TransferStep.Done -> stringResource(R.string.loc_storage_transfer_exported)
                is TransferStep.Failed -> step.message
                TransferStep.Idle -> stringResource(R.string.loc_storage_transfer_export_idle)
            },
            enabled = export !is TransferStep.Working && import !is TransferStep.Working,
            onClick = { exportPicker.launch(viewModel.exportFileName()) },
        )
        StorageActionButton(
            label = stringResource(R.string.loc_storage_transfer_import),
            detail = when (val step = import) {
                is TransferStep.Working -> step.progress?.let {
                    stringResource(R.string.loc_storage_transfer_import_progress, (it * 100).toInt())
                } ?: stringResource(R.string.loc_storage_transfer_importing)
                TransferStep.Done -> stringResource(R.string.loc_storage_transfer_imported)
                is TransferStep.Failed -> step.message
                TransferStep.Idle -> checkError ?: stringResource(R.string.loc_storage_transfer_import_idle)
            },
            danger = true,
            enabled = export !is TransferStep.Working && import !is TransferStep.Working && import !is TransferStep.Done,
            onClick = {
                menuState.showConfirm(
                    title = replaceTitle,
                    message = replaceMessage,
                    action = pickZip,
                    onConfirm = {
                        viewModel.clearImport()
                        importPicker.launch(ZipTypes)
                    },
                )
            },
        )
    }
}
