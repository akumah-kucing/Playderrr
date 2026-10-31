// ArtistSimilarScreen.kt

package com.playder.ui.screens.artist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.playder.R
import com.playder.ui.utils.resize
import com.playder.utils.ArtistPageCache
import com.playder.viewmodels.ArtistSimilarViewModel

/** "similar to <artist>": the artists around them, the playlists they're on, those artists' songs */
@Composable
fun ArtistSimilarScreen(
    navController: NavController,
    viewModel: ArtistSimilarViewModel = hiltViewModel(),
) {
    val page by viewModel.page.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val deepLoading by viewModel.deepLoading.collectAsState()
    val header = ArtistPageCache.header(viewModel.artistId)

    SimilarPage(
        navController = navController,
        bandImageUrl = header?.bannerUrl
            ?: (page?.artist?.thumbnail ?: viewModel.picks?.artistThumbnailUrl)?.resize(544, 544),
        smallLine = stringResource(R.string.similar_to),
        bigLine = header?.name ?: page?.artist?.title ?: viewModel.picks?.artistName.orEmpty(),
        groups = groups,
        deepLoading = deepLoading,
        artistsTitle = stringResource(R.string.artist_similar_artists_ui),
        playlistsTitle = stringResource(R.string.artist_featured_playlists_ui),
        songsTitle = stringResource(R.string.artist_featured_songs_ui),
        albumsTitle = stringResource(R.string.artist_new_from_them_ui),
    )
}
