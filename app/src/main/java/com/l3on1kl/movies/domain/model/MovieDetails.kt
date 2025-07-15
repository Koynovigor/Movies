package com.l3on1kl.movies.domain.model

data class MovieDetails(
    val id: Long,
    val title: String,
    val overview: String,
    val voteAverage: Double,
    val posterPath: String?,
    val backdropPath: String?,
    val runtime: Int?,
    val releaseDate: String?,
    val tagline: String?,
    val genres: List<String>,
    val originalTitle: String?,
    val status: String?,
    val budget: Long?,
    val revenue: Long?,
    val originalLanguage: String?
)
