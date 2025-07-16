package com.l3on1kl.movies.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "movies",
    primaryKeys = ["id", "categoryId"]
)
data class MovieEntity(
    val id: Long,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String,
    val voteAverage: Double,
    val categoryId: Int,
    val page: Int
)
