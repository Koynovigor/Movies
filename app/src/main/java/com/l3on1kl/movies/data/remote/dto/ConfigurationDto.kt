package com.l3on1kl.movies.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConfigurationDto(
    val images: ImagesCfg
)

@Serializable
data class ImagesCfg(
    @SerialName("secure_base_url") val base: String,
    @SerialName("poster_sizes") val posterSizes: List<String>,
    @SerialName("backdrop_sizes") val backdropSizes: List<String>
)