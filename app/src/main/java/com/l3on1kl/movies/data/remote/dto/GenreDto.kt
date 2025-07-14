package com.l3on1kl.movies.data.remote.dto

data class GenreDto(
    val id: Int,
    val name: String
)

data class GenresDto(
    val genres: List<GenreDto>
)
