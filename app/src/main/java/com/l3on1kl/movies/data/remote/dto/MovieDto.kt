package com.l3on1kl.movies.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieDto(
    val id: Long,
    val title: String,
    @SerialName("poster_path") val posterPath: String? = null,
    val overview: String,
    @SerialName("vote_average") val voteAverage: Double
)

@Serializable
data class MoviesPageDto(
    val page: Int,
    val results: List<MovieDto>
)
