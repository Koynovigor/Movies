package com.l3on1kl.movies.util

import com.l3on1kl.movies.data.remote.dto.ImagesCfg

const val IMAGE_BASE = "https://image.tmdb.org/t/p/"
const val POSTER_SIZE = "w500"

object TmdbConfigHolder {
    var imagesConfig: ImagesCfg? = null
}

fun String?.toPosterUrl(
    cfg: ImagesCfg?
): String? =
    this?.let {
        "${cfg?.base ?: IMAGE_BASE}${cfg?.posterSizes?.getOrNull(3) ?: POSTER_SIZE}$it"
    }
