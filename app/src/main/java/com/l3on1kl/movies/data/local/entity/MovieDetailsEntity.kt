package com.l3on1kl.movies.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_details")
data class MovieDetailsEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val overview: String,
    val voteAverage: Double,
    val posterPath: String?,
    val backdropPath: String?,
    val runtime: Int?,
    val releaseDate: String?,
    val tagline: String?,
    val genres: String,
    val originalTitle: String?,
    val status: String?,
    val budget: Long?,
    val revenue: Long?,
    val originalLanguage: String?
)
