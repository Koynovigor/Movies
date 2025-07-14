package com.l3on1kl.movies.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ConfigurationDto(
    val images: ImagesCfg
)

data class ImagesCfg(
    @SerializedName("secure_base_url") val base: String,
    @SerializedName("poster_sizes") val posterSizes: List<String>,
    @SerializedName("backdrop_sizes") val backdropSizes: List<String>
)
