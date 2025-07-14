package com.l3on1kl.movies.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val posterPath: String?,
    val overview: String,
    val voteAverage: Double,
    val category: String,
    val page: Int
)
