package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.CityLocation

@Entity(tableName = "saved_cities")
data class CityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val country: String,
    val adminArea: String? = null,
    val latitude: Double,
    val longitude: Double,
    val isCurrentLocation: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): CityLocation {
        return CityLocation(
            id = id,
            name = name,
            country = country,
            adminArea = adminArea,
            latitude = latitude,
            longitude = longitude,
            isCurrentLocation = isCurrentLocation
        )
    }

    companion object {
        fun fromDomainModel(city: CityLocation): CityEntity {
            return CityEntity(
                id = city.id,
                name = city.name,
                country = city.country,
                adminArea = city.adminArea,
                latitude = city.latitude,
                longitude = city.longitude,
                isCurrentLocation = city.isCurrentLocation
            )
        }
    }
}
