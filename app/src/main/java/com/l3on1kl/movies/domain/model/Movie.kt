package com.l3on1kl.movies.domain.model

data class Movie(
    val id: Long,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String,
    val voteAverage: Double
)
