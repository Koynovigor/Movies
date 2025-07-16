package com.l3on1kl.movies.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MovieDetailsDto(
    val id: Long,
    val title: String,
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    val overview: String,
    @SerializedName("vote_average") val voteAverage: Double,
    val runtime: Int?,
    @SerializedName("release_date") val releaseDate: String?,
    val tagline: String?,
    val genres: List<GenreDto>,
    @SerializedName("original_title") val originalTitle: String?,
    val status: String?,
    val budget: Long?,
    val revenue: Long?,
    @SerializedName("original_language") val originalLanguage: String?
)
