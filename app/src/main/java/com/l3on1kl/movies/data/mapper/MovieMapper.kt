package com.l3on1kl.movies.data.mapper

import com.l3on1kl.movies.data.local.entity.MovieDetailsEntity
import com.l3on1kl.movies.data.local.entity.MovieEntity
import com.l3on1kl.movies.data.remote.dto.MovieDetailsDto
import com.l3on1kl.movies.data.remote.dto.MovieDto
import com.l3on1kl.movies.domain.model.Movie
import com.l3on1kl.movies.domain.model.MovieDetails

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
        categoryId: Int,
        page: Int
    ) = MovieEntity(
        id = movie.id,
        title = movie.title,
        posterPath = movie.posterPath,
        backdropPath = movie.backdropPath,
        overview = movie.overview,
        voteAverage = movie.voteAverage,
        categoryId = categoryId,
        page = page
    )

    fun fromDetailsDto(
        dto: MovieDetailsDto
    ) = MovieDetails(
        id = dto.id,
        title = dto.title,
        overview = dto.overview,
        voteAverage = dto.voteAverage,
        posterPath = dto.posterPath,
        backdropPath = dto.backdropPath,
        runtime = dto.runtime,
        releaseDate = dto.releaseDate,
        tagline = dto.tagline,
        genres = dto.genres.map { it.name },
        originalTitle = dto.originalTitle,
        status = dto.status,
        budget = dto.budget,
        revenue = dto.revenue,
        originalLanguage = dto.originalLanguage
    )

    fun fromDetailsEntity(entity: MovieDetailsEntity) = MovieDetails(
        id = entity.id,
        title = entity.title,
        overview = entity.overview,
        voteAverage = entity.voteAverage,
        posterPath = entity.posterPath,
        backdropPath = entity.backdropPath,
        runtime = entity.runtime,
        releaseDate = entity.releaseDate,
        tagline = entity.tagline,
        genres = if (entity.genres.isNotEmpty()) entity.genres.split(',') else emptyList(),
        originalTitle = entity.originalTitle,
        status = entity.status,
        budget = entity.budget,
        revenue = entity.revenue,
        originalLanguage = entity.originalLanguage
    )

    fun toDetailsEntity(details: MovieDetails) = MovieDetailsEntity(
        id = details.id,
        title = details.title,
        overview = details.overview,
        voteAverage = details.voteAverage,
        posterPath = details.posterPath,
        backdropPath = details.backdropPath,
        runtime = details.runtime,
        releaseDate = details.releaseDate,
        tagline = details.tagline,
        genres = details.genres.joinToString(separator = ","),
        originalTitle = details.originalTitle,
        status = details.status,
        budget = details.budget,
        revenue = details.revenue,
        originalLanguage = details.originalLanguage
    )
}
