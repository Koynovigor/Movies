package com.l3on1kl.movies.data.mapper

import com.l3on1kl.movies.data.local.MovieEntity
import com.l3on1kl.movies.data.remote.dto.MovieDto
import com.l3on1kl.movies.domain.model.Movie

object MovieMapper {
    fun fromDto(
        dto: MovieDto
    ) = Movie(
        id = dto.id,
        title = dto.title,
        posterPath = dto.posterPath,
        backdropPath = dto.backdropPath,
        overview = dto.overview,
        voteAverage = dto.voteAverage
    )

    fun fromEntity(
        entity: MovieEntity
    ) = Movie(
        id = entity.id,
        title = entity.title,
        posterPath = entity.posterPath,
        backdropPath = entity.backdropPath,
        overview = entity.overview,
        voteAverage = entity.voteAverage
    )

    fun toEntity(
        movie: Movie,
        category: String,
        page: Int
    ) = MovieEntity(
        id = movie.id,
        title = movie.title,
        posterPath = movie.posterPath,
        backdropPath = movie.backdropPath,
        overview = movie.overview,
        voteAverage = movie.voteAverage,
        category = category,
        page = page
    )
}
