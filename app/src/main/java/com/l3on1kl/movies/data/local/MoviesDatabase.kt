package com.l3on1kl.movies.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.l3on1kl.movies.data.local.dao.CategoryDao
import com.l3on1kl.movies.data.local.dao.MovieDao
import com.l3on1kl.movies.data.local.entity.CategoryEntity
import com.l3on1kl.movies.data.local.entity.MovieEntity

@Database(
    entities = [MovieEntity::class, CategoryEntity::class],
    version = 4,
    exportSchema = false
)
abstract class MoviesDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun categoryDao(): CategoryDao
}
