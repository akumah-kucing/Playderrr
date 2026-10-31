// playder → settings → integrations: a card per service, tapping one opens its own page

package com.playder.ui.screens.settings.integrations

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.playder.R
import com.playder.scrobble.ScrobbleSite
import com.playder.ui.component.SubSettingsScaffold
import com.playder.ui.screens.settings.integrations.discord.DiscordRoute

const val IntegrationsRoute = "integrations_settings"

private val LastFmRed = Color(0xFFE31B23)
private val LibreFmRed = Color(0xFF990000)

@Composable
fun IntegrationsScreen(navController: NavController) {
    SubSettingsScaffold(
        title = stringResource(R.string.integrations),
        onBack = { navController.navigateUp() },
    ) {
        IntegrationPageSubtitle(androidx.compose.ui.res.stringResource(R.string.loc_integrations_available))

        IntegrationBannerCard(
            name = "Discord",
            icon = R.drawable.integration_discord,
            onClick = { navController.navigate(DiscordRoute) },
        ) {
            PictureBanner(R.drawable.integration_discord_banner)
        }
        Spacer(Modifier.height(13.dp))
        IntegrationBannerCard(
            name = ScrobbleSite.LASTFM.label,
            icon = R.drawable.integration_lastfm,
            onClick = { navController.navigate(scrobbleServiceRoute(ScrobbleSite.LASTFM)) },
        ) {
            ColorBanner(LastFmRed, wordmark = R.drawable.integration_lastfm_wordmark)
        }
        Spacer(Modifier.height(13.dp))
        IntegrationBannerCard(
            name = ScrobbleSite.LIBREFM.label,
            icon = R.drawable.integration_librefm,
            onClick = { navController.navigate(scrobbleServiceRoute(ScrobbleSite.LIBREFM)) },
        ) {
            ColorBanner(LibreFmRed)
        }

        Spacer(Modifier.height(140.dp))
    }
}
