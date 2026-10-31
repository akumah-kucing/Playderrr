// about app, upstream credits and license sheets pushed from the version sheet or the playder page

package com.playder.ui.screens.update

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.playder.BuildConfig
import com.playder.R
import com.playder.core.updater.GithubRepoUrl
import com.playder.ui.component.PopupSheetState
import com.playder.ui.component.pressBounce
import com.playder.ui.player.menu.MenuRowSurface

@Immutable
private data class LeadDevEntry(
    val name: String,
    val devSpaceIndex: Int,
    @DrawableRes val avatar: Int,
)

@Immutable
private data class CreditEntry(
    val name: String,
    val subtitle: String,
    val githubUser: String,
    val url: String,
)

@Immutable
private data class LicenseEntry(
    val tag: String,
    val title: String,
    val subtitle: String,
    val url: String,
)

@Immutable
private data class ContributorEntry(
    val login: String,
    @DrawableRes val avatar: Int,
)

private val LeadDevs = listOf(
    LeadDevEntry(
        name = "akumah_kucing",
        devSpaceIndex = 0,
        avatar = R.drawable.akumahkucing,
    ),
)

private val BaseCredits = listOf(
    CreditEntry(
        name = "Roo",
        subtitle = "Playder developer",
        githubUser = "realidkroo",
        url = "https://github.com/realidkroo",
    ),
    CreditEntry(
        name = "key",
        subtitle = "Playder developer",
        githubUser = "realkeichi",
        url = "https://github.com/realkeichi",
    ),
)

private val Licenses = listOf(
    LicenseEntry(
        tag = "GPL-3.0",
        title = "Playder Music · GNU General Public License v3.0",
        subtitle = "Free and open source software, source code on GitHub",
        url = "$GithubRepoUrl/blob/main/LICENSE",
    ),
    LicenseEntry(
        tag = "GPL-3.0",
        title = "ViviMusic · GNU General Public License v3.0",
        subtitle = "by vivizzz007 · base project this app was forked from",
        url = "https://github.com/vivizzz007/vivi-music/blob/main/LICENSE",
    ),
    LicenseEntry(
        tag = "GPL-3.0",
        title = "Metrolist · GNU General Public License v3.0",
        subtitle = "by Mostafa Alagamy · base framework",
        url = "https://github.com/mostafaalagamy/Metrolist/blob/main/LICENSE",
    ),
    LicenseEntry(
        tag = "GPL-3.0",
        title = "SimpMusic · GNU General Public License v3.0",
        subtitle = "by maxrave-dev · lyrics provider integration",
        url = "https://github.com/maxrave-dev/SimpMusic/blob/dev/LICENSE",
    ),
    LicenseEntry(
        tag = "GPL-3.0",
        title = "ArchiveTune · GNU General Public License v3.0",
        subtitle = "by Rukamori · design and architectural inspiration",
        url = "https://github.com/rukamori/ArchiveTune/blob/main/LICENSE",
    ),
    LicenseEntry(
        tag = "GPL-3.0",
        title = "RiMusic · GNU General Public License v3.0",
        subtitle = "by fast4x · InnerTube API wrapper",
        url = "https://github.com/fast4x/RiMusic/blob/master/LICENSE",
    ),
    LicenseEntry(
        tag = "MIT",
        title = "LRCLIB · MIT License",
        subtitle = "by tranxuanthang · synchronized lyrics database",
        url = "https://github.com/tranxuanthang/lrclib/blob/main/LICENSE",
    ),
    LicenseEntry(
        tag = "MIT",
        title = "AutoEq · MIT License",
        subtitle = "by Jaakko Pasanen · headphone correction profiles",
        url = "https://github.com/jaakkopasanen/AutoEq/blob/master/LICENSE",
    ),
)

private val CoolContributors = listOf(
    ContributorEntry(
        login = "akumah_kucing",
        avatar = R.drawable.akumahkucing,
    ),
    ContributorEntry(
        login = "Alvinzz",
        avatar = R.drawable.alvinzz,
    ),
)

// github redirects username.png straight to the user's current avatar without counting against the api rate limit
private fun githubAvatarUrl(githubUser: String): String =
    "https://github.com/$githubUser.png?size=160"

fun PopupSheetState.showAboutSheet(onOpenDevSpace: (Int) -> Unit = {}) {
    val popup = this
    show(buttonBar = { SheetPrimaryButton(text = stringResource(R.string.loc_update_done), onClick = popup::dismiss) }) {
        AboutSheetContent(popup = popup, onOpenDevSpace = onOpenDevSpace)
    }
}

private fun PopupSheetState.showLicenseSheet() {
    val popup = this
    show(buttonBar = { SheetPrimaryButton(text = stringResource(R.string.loc_update_done), onClick = popup::dismiss) }) {
        LicenseSheetContent()
    }
}

@Composable
private fun ColumnScope.AboutSheetContent(
    popup: PopupSheetState,
    onOpenDevSpace: (Int) -> Unit,
) {
    val context = LocalContext.current
    val installed = shortVersion(BuildConfig.VERSION_NAME)
    val scrollState = rememberScrollState()
    val ringScrollRangePx = with(LocalDensity.current) { 180.dp.toPx() }

    Box(Modifier.fillMaxWidth()) {
        // zero-size layout places the spinning ring behind the top icon without pushing the column down
        VersionRing(
            text = "PLAYDER $installed",
            scrollProgress = { scrollState.value / ringScrollRangePx },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(
                        constraints.copy(
                            minWidth = 0,
                            minHeight = 0,
                            maxWidth = Constraints.Infinity,
                            maxHeight = Constraints.Infinity,
                        ),
                    )
                    layout(0, 0) {
                        placeable.place(x = -placeable.width / 2, y = (-166).dp.roundToPx())
                    }
                },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
        ) {
            Spacer(Modifier.height(18.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(MenuRowSurface),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_playder_mark),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(38.dp),
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.loc_about_title),
                    fontSize = 36.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1.4).sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.loc_about_brand_line, installed),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp,
                    color = Color.White.copy(alpha = 0.9f),
                )
            }

            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MenuRowSurface)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            ) {
                Text(
                    text = stringResource(R.string.loc_about_blurb),
                    fontSize = 15.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp,
                )
            }

            Spacer(Modifier.height(18.dp))
            SectionHeader(stringResource(R.string.loc_about_actions))
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AboutPillRow(
                    text = stringResource(R.string.loc_about_github),
                    onClick = { context.openUrl(GithubRepoUrl) },
                )
            }

            Spacer(Modifier.height(18.dp))
            SectionHeader(stringResource(R.string.loc_about_lead_devs))
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MenuRowSurface)
                    .padding(vertical = 6.dp),
            ) {
                LeadDevs.forEach { dev ->
                    PersonRow(
                        name = dev.name,
                        subtitle = null,
                        avatarUrl = "",
                        localAvatarRes = dev.avatar,
                        showArrow = true,
                        onClick = { onOpenDevSpace(dev.devSpaceIndex) },
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            SectionHeader(stringResource(R.string.loc_about_credits))
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MenuRowSurface)
                    .padding(vertical = 6.dp),
            ) {
                BaseCredits.forEach { credit ->
                    PersonRow(
                        name = credit.name,
                        subtitle = stringResource(R.string.loc_about_developer),
                        avatarUrl = githubAvatarUrl(credit.githubUser),
                        showArrow = false,
                        onClick = { context.openUrl(credit.url) },
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            SectionHeader(stringResource(R.string.loc_about_license))
            Spacer(Modifier.height(8.dp))
            AboutCardButton(
                text = stringResource(R.string.loc_about_more_license),
                onClick = { popup.showLicenseSheet() },
            )

            Spacer(Modifier.height(18.dp))
            SectionHeader(stringResource(R.string.loc_about_contributors))
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MenuRowSurface)
                    .padding(vertical = 6.dp),
            ) {
                CoolContributors.forEach { contributor ->
                    PersonRow(
                        name = contributor.login,
                        subtitle = null,
                        avatarUrl = "",
                        showArrow = false,
                        onClick = null,
                        localAvatarRes = contributor.avatar,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ColumnScope.LicenseSheetContent() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.loc_about_license),
            fontSize = 36.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1.4).sp,
        )
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Licenses.forEach { entry ->
                LicenseCard(
                    entry = entry,
                    onClick = { context.openUrl(entry.url) },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.3).sp,
        color = Color.White.copy(alpha = 0.55f),
    )
}

@Composable
private fun AboutPillRow(text: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .pressBounce(interaction, pressedScale = 0.97f)
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MenuRowSurface)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 18.dp),
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.4).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AboutCardButton(text: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .pressBounce(interaction, pressedScale = 0.97f)
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MenuRowSurface)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 18.dp),
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.4).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PersonRow(
    name: String,
    subtitle: String?,
    avatarUrl: String,
    showArrow: Boolean,
    onClick: (() -> Unit)?,
    @DrawableRes localAvatarRes: Int? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val rowModifier = if (onClick != null) {
        Modifier
            .pressBounce(interaction, pressedScale = 0.98f)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    } else {
        Modifier
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = rowModifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        if (localAvatarRes != null) {
            Image(
                painter = painterResource(localAvatarRes),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(44.dp).clip(CircleShape),
            )
        } else {
            GithubAvatar(url = avatarUrl, size = 44.dp)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.2).sp,
                    color = Color.White.copy(alpha = 0.65f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (showArrow) {
            Spacer(Modifier.width(10.dp))
            ArrowBadge()
        }
    }
}

@Composable
private fun LicenseCard(
    entry: LicenseEntry,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .pressBounce(interaction, pressedScale = 0.98f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MenuRowSurface)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.tag,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp,
                color = Color.White.copy(alpha = 0.55f),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = entry.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(
                    when (entry.title) {
                        "Playder Music · GNU General Public License v3.0" -> R.string.loc_about_free_open_source
                        "ViviMusic · GNU General Public License v3.0" -> R.string.loc_about_license_vivi
                        "Metrolist · GNU General Public License v3.0" -> R.string.loc_about_license_metrolist
                        "SimpMusic · GNU General Public License v3.0" -> R.string.loc_about_license_simpmusic
                        "ArchiveTune · GNU General Public License v3.0" -> R.string.loc_about_license_archivetune
                        "RiMusic · GNU General Public License v3.0" -> R.string.loc_about_license_rimusic
                        "LRCLIB · MIT License" -> R.string.loc_about_license_lrclib
                        else -> R.string.loc_about_license_autoeq
                    },
                ),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.2).sp,
                color = Color.White.copy(alpha = 0.55f),
            )
        }
        Spacer(Modifier.width(12.dp))
        ArrowBadge()
    }
}

@Composable
private fun ArrowBadge() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.16f)),
    ) {
        Icon(
            painter = painterResource(R.drawable.arrow_forward),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(12.dp),
        )
    }
}

@Composable
private fun GithubAvatar(
    url: String,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val request = remember(url) {
        ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .build()
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFD9D9D9)),
    ) {
        AsyncImage(
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
