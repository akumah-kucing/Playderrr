package com.playder.ui.screens.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.playder.R

private val GenreDescriptionResources = mapOf(
    "chill" to R.string.loc_search_genre_chill,
    "commute" to R.string.loc_search_genre_commute,
    "energize" to R.string.loc_search_genre_energize,
    "feel good" to R.string.loc_search_genre_feel_good,
    "focus" to R.string.loc_search_genre_focus,
    "gaming" to R.string.loc_search_genre_gaming,
    "party" to R.string.loc_search_genre_party,
    "romance" to R.string.loc_search_genre_romance,
    "sad" to R.string.loc_search_genre_sad,
    "sleep" to R.string.loc_search_genre_sleep,
    "workout" to R.string.loc_search_genre_workout,
    "african" to R.string.loc_search_genre_african,
    "arabic" to R.string.loc_search_genre_arabic,
    "blues" to R.string.loc_search_genre_blues,
    "bollywood & indian" to R.string.loc_search_genre_bollywood_indian,
    "country" to R.string.loc_search_genre_country,
    "dance & electronic" to R.string.loc_search_genre_dance_electronic,
    "decades" to R.string.loc_search_genre_decades,
    "family" to R.string.loc_search_genre_family,
    "folk & acoustic" to R.string.loc_search_genre_folk_acoustic,
    "hip hop" to R.string.loc_search_genre_hip_hop,
    "indie & alternative" to R.string.loc_search_genre_indie_alternative,
    "j pop" to R.string.loc_search_genre_jpop,
    "jazz" to R.string.loc_search_genre_jazz,
    "k pop" to R.string.loc_search_genre_kpop,
    "latin" to R.string.loc_search_genre_latin,
    "metal" to R.string.loc_search_genre_metal,
    "pop" to R.string.loc_search_genre_pop,
    "r&b & soul" to R.string.loc_search_genre_rnb_soul,
    "reggae" to R.string.loc_search_genre_reggae,
    "rock" to R.string.loc_search_genre_rock,
    "soundtracks & musicals" to R.string.loc_search_genre_soundtracks,
)

@Composable
fun genreDescription(title: String): String {
    if (title.isBlank()) return ""
    val key = title.lowercase()
        .replace("&amp;", "&")
        .replace("-", " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    val resource = GenreDescriptionResources[key]
        ?: GenreDescriptionResources.entries.firstOrNull { (genre, _) ->
            key.startsWith(genre) || genre.startsWith(key)
        }?.value

    return if (resource != null) stringResource(resource)
    else stringResource(R.string.loc_genre_description, title)
}
