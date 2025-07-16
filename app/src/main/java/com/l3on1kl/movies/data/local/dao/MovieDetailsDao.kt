package com.l3on1kl.movies.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.l3on1kl.movies.data.local.entity.MovieDetailsEntity

@Dao
interface MovieDetailsDao {
    @Query("SELECT * FROM movie_details WHERE id = :id")
    suspend fun getById(
        id: Long
    ): MovieDetailsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(details: MovieDetailsEntity)
}
