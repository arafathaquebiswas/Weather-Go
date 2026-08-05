package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.db.CityDao
import com.example.data.db.CityEntity
import com.example.data.model.CityLocation
import com.example.data.model.CurrentWeather
import com.example.data.model.DailyForecast
import com.example.data.model.FullWeatherData
import com.example.data.model.HourlyForecast
import com.example.util.WeatherUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class WeatherRepository(private val cityDao: CityDao) {

    fun getSavedCities(): Flow<List<CityLocation>> {
        return cityDao.getAllSavedCities().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun saveCity(city: CityLocation) = withContext(Dispatchers.IO) {
        cityDao.insertCity(CityEntity.fromDomainModel(city))
    }

    suspend fun removeCity(city: CityLocation) = withContext(Dispatchers.IO) {
        cityDao.deleteByCoordinates(city.latitude, city.longitude)
    }

    suspend fun searchCities(query: String): List<CityLocation> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val response = ApiClient.geocodingService.searchCity(query)
            response.results?.map { dto ->
                CityLocation(
                    id = dto.id,
                    name = dto.name,
                    country = dto.country ?: "",
                    adminArea = dto.admin1,
                    latitude = dto.latitude,
                    longitude = dto.longitude
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFullWeatherData(location: CityLocation): FullWeatherData = withContext(Dispatchers.IO) {
        val forecast = ApiClient.weatherService.getWeatherForecast(
            latitude = location.latitude,
            longitude = location.longitude
        )

        val isDay = (forecast.current?.isDay ?: 1) == 1
        val currentCondition = WeatherUtils.getWeatherCondition(
            code = forecast.current?.weatherCode ?: 0,
            isDay = isDay
        )

        val current = CurrentWeather(
            tempC = forecast.current?.temperature2m ?: 20.0,
            feelsLikeC = forecast.current?.apparentTemperature ?: 20.0,
            humidity = forecast.current?.relativeHumidity2m ?: 50,
            windSpeedKmH = forecast.current?.windSpeed10m ?: 10.0,
            windDirectionDegrees = forecast.current?.windDirection10m ?: 180,
            pressureHpa = forecast.current?.surfacePressure ?: 1013.25,
            uvIndex = forecast.current?.uvIndex ?: 3.0,
            condition = currentCondition,
            isDay = isDay,
            timeFormatted = WeatherUtils.formatHourTime(forecast.current?.time ?: "")
        )

        val hourlyList = mutableListOf<HourlyForecast>()
        val hourlyDto = forecast.hourly
        if (hourlyDto != null && hourlyDto.time.isNotEmpty()) {
            val count = minOf(24, hourlyDto.time.size)
            for (i in 0 until count) {
                val timeIso = hourlyDto.time.getOrNull(i) ?: ""
                val temp = hourlyDto.temperature2m.getOrNull(i) ?: 0.0
                val code = hourlyDto.weatherCode.getOrNull(i) ?: 0
                val pop = hourlyDto.precipitationProbability.getOrNull(i) ?: 0

                val hourIsDay = if (timeIso.contains("T")) {
                    val hour = timeIso.substringAfter("T").take(2).toIntOrNull() ?: 12
                    hour in 6..19
                } else true

                hourlyList.add(
                    HourlyForecast(
                        timeLabel = WeatherUtils.formatHourTime(timeIso),
                        tempC = temp,
                        condition = WeatherUtils.getWeatherCondition(code, hourIsDay),
                        precipitationChance = pop
                    )
                )
            }
        }

        val dailyList = mutableListOf<DailyForecast>()
        val dailyDto = forecast.daily
        if (dailyDto != null && dailyDto.time.isNotEmpty()) {
            val count = minOf(7, dailyDto.time.size)
            for (i in 0 until count) {
                val dateIso = dailyDto.time.getOrNull(i) ?: ""
                val code = dailyDto.weatherCode.getOrNull(i) ?: 0
                val maxTemp = dailyDto.temperature2mMax.getOrNull(i) ?: 0.0
                val minTemp = dailyDto.temperature2mMin.getOrNull(i) ?: 0.0
                val uv = dailyDto.uvIndexMax.getOrNull(i) ?: 0.0
                val precip = dailyDto.precipitationSum.getOrNull(i) ?: 0.0
                val sunrise = dailyDto.sunrise.getOrNull(i) ?: ""
                val sunset = dailyDto.sunset.getOrNull(i) ?: ""

                dailyList.add(
                    DailyForecast(
                        dayName = WeatherUtils.formatDayName(dateIso),
                        dateLabel = WeatherUtils.formatDateLabel(dateIso),
                        minTempC = minTemp,
                        maxTempC = maxTemp,
                        condition = WeatherUtils.getWeatherCondition(code, true),
                        uvIndexMax = uv,
                        precipitationSumMm = precip,
                        sunriseTime = WeatherUtils.formatSunTime(sunrise),
                        sunsetTime = WeatherUtils.formatSunTime(sunset)
                    )
                )
            }
        }

        val aiInsight = WeatherUtils.generateSmartInsight(
            tempC = current.tempC,
            code = current.condition.code,
            uvMax = dailyList.firstOrNull()?.uvIndexMax ?: current.uvIndex,
            windSpeedKmH = current.windSpeedKmH
        )

        FullWeatherData(
            location = location,
            current = current,
            hourly = hourlyList,
            daily = dailyList,
            aiInsight = aiInsight
        )
    }
}
