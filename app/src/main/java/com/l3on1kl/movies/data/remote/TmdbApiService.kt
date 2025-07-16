package com.l3on1kl.movies.data.remote

import com.l3on1kl.movies.data.remote.dto.ConfigurationDto
import com.l3on1kl.movies.data.remote.dto.GenresDto
import com.l3on1kl.movies.data.remote.dto.MovieDetailsDto
import com.l3on1kl.movies.data.remote.dto.MoviesPageDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApiService {
    @GET("3/configuration")
    suspend fun getConfiguration(
        @Query("api_key") apiKey: String
    ): ConfigurationDto

    @GET("3/genre/movie/list")
    suspend fun getGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ru-RU"
    ): GenresDto

    @GET("3/discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("with_genres") genreId: Int,
        @Query("language") language: String = "ru-RU"
    ): MoviesPageDto

    @GET("3/movie/{movie_id}")
    suspend fun getMovieDetails(
        @retrofit2.http.Path("movie_id") movieId: Long,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ru-RU"
    ): MovieDetailsDto
}
