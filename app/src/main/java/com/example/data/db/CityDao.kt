package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {

    @Query("SELECT * FROM saved_cities ORDER BY addedAt ASC")
    fun getAllSavedCities(): Flow<List<CityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: CityEntity): Long

    @Delete
    suspend fun deleteCity(city: CityEntity)

    @Query("DELETE FROM saved_cities WHERE latitude = :latitude AND longitude = :longitude")
    suspend fun deleteByCoordinates(latitude: Double, longitude: Double)

    @Query("SELECT * FROM saved_cities WHERE latitude = :latitude AND longitude = :longitude LIMIT 1")
    suspend fun getCityByCoordinates(latitude: Double, longitude: Double): CityEntity?
}
