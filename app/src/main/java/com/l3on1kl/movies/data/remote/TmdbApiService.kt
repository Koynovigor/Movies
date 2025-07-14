package com.l3on1kl.movies.data.remote

import com.l3on1kl.movies.data.remote.dto.MoviesPageDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApiService {
    @GET("3/movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "ru-RU"
    ): MoviesPageDto

    @GET("3/movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "ru-RU"
    ): MoviesPageDto

    @GET("3/movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "ru-RU"
    ): MoviesPageDto

    @GET("3/movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "ru-RU"
    ): MoviesPageDto
}
