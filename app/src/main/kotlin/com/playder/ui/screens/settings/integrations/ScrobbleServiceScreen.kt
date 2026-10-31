// the last.fm or libre.fm page: every song that counts is scrobbled, and offline plays wait for a network

package com.playder.ui.screens.settings.integrations

import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.playder.R
import com.playder.constants.LastFMUseNowPlaying
import com.playder.constants.LastFMUseSendLikes
import com.playder.scrobble.ScrobbleSite
import com.playder.ui.component.AppSwitch
import com.playder.ui.component.LocalZoomOutOverlayState
import com.playder.ui.component.PopupSheetHandle
import com.playder.ui.component.SettingsGroup
import com.playder.ui.component.SettingsGroupStyle
import com.playder.ui.component.SettingsItem
import com.playder.ui.component.SubSettingsScaffold
import com.playder.utils.rememberPreference
import com.playder.viewmodels.IntegrationsViewModel

const val ScrobbleServiceRoute = "integrations_settings/scrobble/{site}"

fun scrobbleServiceRoute(site: ScrobbleSite) = "integrations_settings/scrobble/${site.name}"

@Composable
fun ScrobbleServiceScreen(
    navController: NavController,
    site: ScrobbleSite,
    viewModel: IntegrationsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val overlay = LocalZoomOutOverlayState.current
    val accounts by viewModel.accounts.collectAsState()
    val pending by viewModel.pending.collectAsState()
    val sendResult by viewModel.sendResult.collectAsState()
    val (nowPlaying, onNowPlayingChange) = rememberPreference(LastFMUseNowPlaying, defaultValue = true)
    val (sendLikes, onSendLikesChange) = rememberPreference(LastFMUseSendLikes, defaultValue = false)

    LaunchedEffect(sendResult) {
        sendResult?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSendResult()
        }
    }

    fun signIn() {
        var sheet: PopupSheetHandle? = null
        sheet = overlay.showWithHandle(fullBleed = true) {
            ScrobbleSignInContent(
                site = site,
                viewModel = viewModel,
                onSignedIn = { name ->
                    sheet?.dismiss()
                    Toast.makeText(context, "Signed in to ${site.label} as $name. Songs you play are scrobbled from now on.", Toast.LENGTH_LONG).show()
                },
                onClose = { sheet?.dismiss() },
            )
        }
    }

    SubSettingsScaffold(
        title = site.label,
        onBack = { navController.navigateUp() },
        backLabel = "Integrations",
    ) {
        IntegrationPageSubtitle("Scrobbling")
        ScrobbleAccountGroup(
            site = site,
            username = accounts[site],
            onSignIn = ::signIn,
            onSignOut = { viewModel.signOut(site) },
        )
        Spacer(Modifier.height(16.dp))

        // shared by both services, so it reads the same on either page
        SettingsGroup(
            title = "While you listen",
            style = SettingsGroupStyle.Grouped,
            items = listOfNotNull(
                SettingsItem(
                    title = { Text(androidx.compose.ui.res.stringResource(com.playder.R.string.now_playing)) },
                    highlightKey = "Now Playing",
                    descriptionText = "Show the song you're on right now on your profile",
                    icon = painterResource(R.drawable.music_note),
                    iconShape = CircleShape,
                    onClick = { onNowPlayingChange(!nowPlaying) },
                    trailingContent = { AppSwitch(checked = nowPlaying, onCheckedChange = onNowPlayingChange) },
                ),
                SettingsItem(
                    title = { Text("Send likes") },
                    highlightKey = "Send likes",
                    descriptionText = "Liking a song here loves it there too",
                    icon = painterResource(R.drawable.favorite),
                    iconShape = CircleShape,
                    onClick = { onSendLikesChange(!sendLikes) },
                    trailingContent = { AppSwitch(checked = sendLikes, onCheckedChange = onSendLikesChange) },
                ),
                if (pending > 0) {
                    SettingsItem(
                        title = { Text(if (pending == 1) "1 play waiting to send" else "$pending plays waiting to send") },
                        descriptionText = "They go out on their own once you're online. Tap to send now",
                        icon = painterResource(R.drawable.cloud),
                        iconShape = CircleShape,
                        onClick = viewModel::sendNow,
                    )
                } else null,
            ),
        )

        Spacer(Modifier.height(140.dp))
    }
}

/** sign in, or once signed in: scrobbling on or paused, the profile, and sign out */
@Composable
private fun ScrobbleAccountGroup(
    site: ScrobbleSite,
    username: String?,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
) {
    val (enabled, onEnabledChange) = rememberPreference(site.enabledKey, defaultValue = true)
    val uriHandler = LocalUriHandler.current
    val items = if (username == null) {
        listOf(
            SettingsItem(
                title = { Text("Sign in to ${site.label}") },
                highlightKey = site.label,
                descriptionText = if (site.api.isConfigured) {
                    "Keep your listening history, stats and recommendations there"
                } else {
                    "Not set up in this build of Playder"
                },
                icon = painterResource(R.drawable.login),
                iconShape = CircleShape,
                enabled = site.api.isConfigured,
                onClick = onSignIn,
            ),
        )
    } else {
        listOf(
            SettingsItem(
                title = { Text("Scrobble to ${site.label}") },
                highlightKey = site.label,
                descriptionText = if (enabled) "Signed in as $username" else "Paused. Signed in as $username",
                icon = painterResource(R.drawable.music_history),
                iconShape = CircleShape,
                onClick = { onEnabledChange(!enabled) },
                trailingContent = { AppSwitch(checked = enabled, onCheckedChange = onEnabledChange) },
            ),
            SettingsItem(
                title = { Text("Open your profile") },
                descriptionText = "Your scrobbles and stats on ${site.label}",
                icon = painterResource(R.drawable.web_link),
                iconShape = CircleShape,
                onClick = { uriHandler.openUri(site.profileUrl(username)) },
            ),
            SettingsItem(
                title = { Text("Sign out") },
                descriptionText = "Playder stops scrobbling to ${site.label}",
                icon = painterResource(R.drawable.logout),
                iconShape = CircleShape,
                onClick = onSignOut,
            ),
        )
    }
    SettingsGroup(title = "Account", style = SettingsGroupStyle.Grouped, items = items)
}
