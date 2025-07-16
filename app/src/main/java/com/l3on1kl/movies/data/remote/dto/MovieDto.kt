package com.l3on1kl.movies.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MovieDto(
    val id: Long,
    val title: String,
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    val overview: String,
    @SerializedName("vote_average") val voteAverage: Double
)

data class MoviesPageDto(
    val page: Int,
    val results: List<MovieDto>
)
