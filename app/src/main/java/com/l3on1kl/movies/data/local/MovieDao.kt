package com.l3on1kl.movies.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies WHERE category = :category ORDER BY page, id")
    suspend fun getByCategory(
        category: String
    ): List<MovieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(
        movies: List<MovieEntity>
    )

    @Query("DELETE FROM movies WHERE category = :category AND page = :page")
    suspend fun clearCategoryPage(
        category: String,
        page: Int
    )
}
