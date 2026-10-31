// settings → storage: how much of the phone playder takes, the cache limit, clearing downloads or the cache, and manage downloads

package com.playder.ui.screens.settings.storage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.playder.R
import com.playder.constants.MaxSongCacheSizeKey
import com.playder.ui.component.LocalMenuState
import com.playder.ui.component.SubSettingsScaffold
import com.playder.ui.screens.library.showConfirm
import com.playder.utils.rememberPreference
import com.playder.viewmodels.StorageUsage
import com.playder.viewmodels.StorageViewModel

const val StorageRoute = "storage_settings"

/** song cache limits on offer, in mb, -1 is no limit, the player's default is 1 gb */
private val CacheLimitSteps = listOf(0, 256, 512, 1024, 2048, 4096, 8192, -1)

@Composable
fun StorageScreen(navController: NavController, viewModel: StorageViewModel = hiltViewModel()) {
    val usage by viewModel.usage.collectAsState()
    val busy by viewModel.busy.collectAsState()
    val downloads by viewModel.downloads.collectAsState()
    val (cacheLimit, setCacheLimit) = rememberPreference(MaxSongCacheSizeKey, 1024)
    val context = LocalContext.current
    // the player picked its limit when the app started; a new one only applies from the next start
    val startingLimit = remember { cacheLimit }
    val menuState = LocalMenuState.current

    // songs removed from the Manage downloads sheet change the numbers here too
    LaunchedEffect(downloads?.size) { viewModel.measure() }

    SubSettingsScaffold(title = stringResource(R.string.storage), onBack = { navController.navigateUp() }) {
        OverallCard(usage)

        Spacer(Modifier.height(22.dp))
        Text(
            usage?.let {
                stringResource(
                    R.string.loc_storage_usage,
                    percentOf(it.playder, it.deviceTotal),
                    formatSize(it.playder),
                )
            } ?: stringResource(R.string.loc_storage_working),
            style = storageText(17f, FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 2.dp),
        )

        Spacer(Modifier.height(14.dp))
        BreakdownCard(usage)

        Spacer(Modifier.height(26.dp))
        Text(
            stringResource(R.string.loc_storage_cleanup_intro),
            style = storageText(17f, FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 2.dp),
        )
        usage?.let { SmartHint(it, downloadCount = downloads?.size ?: 0) }

        Spacer(Modifier.height(14.dp))
        CacheLimitCard(
            limit = cacheLimit,
            onLimitChange = setCacheLimit,
            appliesLater = cacheLimit != startingLimit,
        )

        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val downloadCount = downloads?.size ?: 0
            StorageActionButton(
                label = stringResource(R.string.loc_storage_remove_downloads),
                detail = if (downloadCount > 0) {
                    "$downloadCount ${stringResource(if (downloadCount == 1) R.string.loc_storage_songs_singular else R.string.loc_storage_songs_plural)} · ${formatSize(usage?.downloads ?: 0)}"
                } else stringResource(R.string.loc_storage_nothing_downloaded),
                danger = true,
                enabled = !busy && downloadCount > 0,
                onClick = {
                    menuState.showConfirm(
                        title = context.getString(R.string.loc_storage_remove_downloads_confirm),
                        message = context.getString(R.string.loc_storage_downloads_deleted),
                        action = context.getString(R.string.loc_storage_remove_downloads),
                        onConfirm = viewModel::removeAllDownloads,
                    )
                },
            )
            StorageActionButton(
                label = stringResource(R.string.loc_storage_remove_cache),
                detail = usage?.let { formatSize(it.cache) },
                danger = true,
                enabled = !busy,
                onClick = {
                    menuState.showConfirm(
                        title = context.getString(R.string.loc_storage_clear_cache_confirm),
                        message = context.getString(R.string.loc_storage_cache_deleted),
                        action = context.getString(R.string.loc_storage_clear_cache),
                        onConfirm = viewModel::clearCaches,
                    )
                },
            )
            StorageActionButton(
                label = stringResource(R.string.loc_storage_manage_downloads),
                detail = if (downloadCount > 0) stringResource(R.string.loc_storage_manage_downloads_detail) else null,
                enabled = downloadCount > 0,
                onClick = { menuState.showManageDownloadsSheet(viewModel) },
            )
        }

        Spacer(Modifier.height(26.dp))
        TransferSection()
    }
}

/** like 1.4%, or <1% for a sliver, so a small app never reads as using nothing */
private fun percentOf(part: Long, total: Long): String {
    if (total <= 0L) return "0%"
    val p = part * 100.0 / total
    return when {
        p < 0.1 -> "<0.1%"
        p < 10 -> String.format("%.1f%%", p)
        else -> "${p.toInt()}%"
    }
}

@Composable
private fun OverallCard(usage: StorageUsage?) {
    StorageCardColumn {
        Text(stringResource(R.string.loc_storage_overall), style = storageText(17f))
        Spacer(Modifier.height(14.dp))
        UsageBar(
            parts = listOf(
                BarPart(PlayderGreen, usage?.playder ?: 0),
                BarPart(OthersGrey, usage?.othersUsed ?: 0),
            ),
            total = usage?.deviceTotal ?: 0,
        )
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            LegendSwatch(PlayderGreen)
            Spacer(Modifier.width(8.dp))
            Text("Playder", style = storageText(16f, FontWeight.SemiBold))
            Spacer(Modifier.width(16.dp))
            LegendSwatch(OthersGrey)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.loc_storage_your_storage), style = storageText(16f, FontWeight.SemiBold), modifier = Modifier.weight(1f), maxLines = 1)
            if (usage != null) {
                Text(stringResource(R.string.loc_storage_free, formatSize(usage.deviceFree)), style = storageText(12f, FontWeight.Medium, StorageMuted, 0f))
            }
        }
    }
}

@Composable
private fun BreakdownCard(usage: StorageUsage?) {
    StorageCardColumn {
        Text(stringResource(R.string.loc_storage_app_usage), style = storageText(17f))
        Spacer(Modifier.height(14.dp))
        UsageBar(
            parts = listOf(
                BarPart(CacheOrange, usage?.cache ?: 0),
                BarPart(PlayderGreen, usage?.downloads ?: 0),
                BarPart(DatabaseRed, usage?.database ?: 0),
                BarPart(AppDataBlue, usage?.appData ?: 0),
            ),
            total = usage?.playder ?: 0,
        )
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LegendRow(CacheOrange, stringResource(R.string.cache), usage?.let { formatSize(it.cache) })
            LegendRow(PlayderGreen, stringResource(R.string.loc_storage_downloaded_local), usage?.let { formatSize(it.downloads) })
            LegendRow(DatabaseRed, stringResource(R.string.loc_storage_database), usage?.let { formatSize(it.database) })
            LegendRow(AppDataBlue, stringResource(R.string.loc_storage_app_data), usage?.let { formatSize(it.appData) })
        }
    }
}

/** one line pointing at the biggest easy win: the cache, the downloads sheet, or a nudge when the phone is nearly full */
@Composable
private fun SmartHint(usage: StorageUsage, downloadCount: Int) {
    val nearlyFull = usage.deviceTotal > 0 && usage.deviceFree * 10 < usage.deviceTotal
    val hint = when {
        usage.cache >= usage.downloads && usage.cache > 50_000_000L ->
            stringResource(R.string.loc_storage_hint_cache, formatSize(usage.cache))
        downloadCount > 0 && usage.downloads > 50_000_000L ->
            stringResource(R.string.loc_storage_hint_downloads)
        else -> null
    }
    val text = listOfNotNull(if (nearlyFull) stringResource(R.string.loc_storage_phone_nearly_full) else null, hint).joinToString(" ")
    if (text.isEmpty()) return
    Text(
        text,
        style = storageText(13f, FontWeight.Medium, StorageMuted, -0.2f),
        modifier = Modifier.padding(start = 2.dp, end = 2.dp, top = 6.dp),
    )
}

@Composable
private fun CacheLimitCard(limit: Int, onLimitChange: (Int) -> Unit, appliesLater: Boolean) {
    StorageCardColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDADADA)),
            ) {
                Icon(painterResource(R.drawable.cached), contentDescription = null, tint = StorageCard, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.loc_storage_set_cache_limit), style = storageText(15f, FontWeight.SemiBold), modifier = Modifier.weight(1f))
            Text(
                cacheLimitLabel(
                    limit,
                    stringResource(R.string.unlimited),
                    stringResource(R.string.loc_storage_off),
                ),
                style = storageText(15f, FontWeight.Bold),
            )
        }
        Spacer(Modifier.height(10.dp))
        StepSlider(steps = CacheLimitSteps, value = limit, onValueChange = onLimitChange)
        Row {
            Text(stringResource(R.string.loc_storage_no_cache), style = storageText(10f, FontWeight.Medium, StorageMuted, 0f), modifier = Modifier.weight(1f))
            Text(androidx.compose.ui.res.stringResource(com.playder.R.string.unlimited), style = storageText(10f, FontWeight.Medium, StorageMuted, 0f))
        }
        AnimatedVisibility(
            visible = appliesLater,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Text(
                stringResource(R.string.loc_storage_limit_next_start),
                style = storageText(12f, FontWeight.Medium, StorageMuted, -0.1f),
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

private fun cacheLimitLabel(megabytes: Int, unlimited: String, off: String): String = when {
    megabytes < 0 -> unlimited
    megabytes == 0 -> off
    megabytes < 1024 -> "$megabytes MB"
    else -> "${megabytes / 1024} GB"
}
