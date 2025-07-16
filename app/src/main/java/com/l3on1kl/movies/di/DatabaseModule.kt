package com.l3on1kl.movies.di

import android.content.Context
import androidx.room.Room
import com.l3on1kl.movies.data.local.MoviesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDb(
        @ApplicationContext context: Context
    ): MoviesDatabase =
        Room.databaseBuilder(
            context,
            MoviesDatabase::class.java,
            "movies.db"
        )
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides
    @Singleton
    fun provideDao(
        db: MoviesDatabase
    ) = db.movieDao()

    @Provides
    @Singleton
    fun provideCategoryDao(
        db: MoviesDatabase
    ) = db.categoryDao()

    @Provides
    @Singleton
    fun provideMovieDetailsDao(
        db: MoviesDatabase
    ) = db.movieDetailsDao()
}
