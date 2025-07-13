package com.l3on1kl.movies.data.mapper

import com.l3on1kl.movies.data.remote.dto.MovieDto
import com.l3on1kl.movies.domain.model.Movie

object MovieMapper {
    fun fromDto(dto: MovieDto) = Movie(
        id = dto.id,
        title = dto.title,
        posterPath = dto.posterPath,
        overview = dto.overview,
        voteAverage = dto.voteAverage
    )
}
