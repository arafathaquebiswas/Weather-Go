package com.example.data.model

import androidx.compose.ui.graphics.Color

data class CityLocation(
    val id: Long = 0,
    val name: String,
    val country: String,
    val adminArea: String? = null,
    val latitude: Double,
    val longitude: Double,
    val isCurrentLocation: Boolean = false
) {
    val displayName: String
        get() = if (!adminArea.isNullOrBlank() && adminArea != name) "$name, $adminArea" else "$name, $country"
}

enum class TemperatureUnit {
    CELSIUS, FAHRENHEIT
}

data class WeatherCondition(
    val code: Int,
    val title: String,
    val description: String,
    val isDay: Boolean = true
) {
    val themeGradient: List<Color>
        get() = when (code) {
            0 -> if (isDay) listOf(Color(0xFF2563EB), Color(0xFF60A5FA), Color(0xFF93C5FD))
                 else listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF312E81)) // Clear Sky
            1, 2 -> if (isDay) listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFBAE6FD))
                    else listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF475569)) // Partly Cloudy
            3 -> listOf(Color(0xFF475569), Color(0xFF64748B), Color(0xFF94A3B8)) // Overcast
            45, 48 -> listOf(Color(0xFF64748B), Color(0xFF94A3B8), Color(0xFFCBD5E1)) // Fog
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> listOf(Color(0xFF1E3A8A), Color(0xFF1D4ED8), Color(0xFF3B82F6)) // Rain
            71, 73, 75, 77, 85, 86 -> listOf(Color(0xFF0284C7), Color(0xFF7DD3FC), Color(0xFFE0F2FE)) // Snow
            95, 96, 99 -> listOf(Color(0xFF311042), Color(0xFF581C87), Color(0xFF1E1B4B)) // Thunderstorm
            else -> listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFBAE6FD))
        }
}

data class CurrentWeather(
    val tempC: Double,
    val feelsLikeC: Double,
    val humidity: Int,
    val windSpeedKmH: Double,
    val windDirectionDegrees: Int,
    val pressureHpa: Double,
    val uvIndex: Double,
    val condition: WeatherCondition,
    val isDay: Boolean,
    val timeFormatted: String
) {
    fun getTempFormatted(unit: TemperatureUnit): String {
        val temp = if (unit == TemperatureUnit.CELSIUS) tempC else (tempC * 9 / 5) + 32
        return "${temp.toInt()}°"
    }

    fun getFeelsLikeFormatted(unit: TemperatureUnit): String {
        val temp = if (unit == TemperatureUnit.CELSIUS) feelsLikeC else (feelsLikeC * 9 / 5) + 32
        return "Feels like ${temp.toInt()}°"
    }

    val windSpeedMph: Double
        get() = windSpeedKmH * 0.621371

    val windDirectionCardinal: String
        get() {
            val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
            val index = ((windDirectionDegrees + 22.5) % 360 / 45).toInt()
            return directions[index]
        }
}

data class HourlyForecast(
    val timeLabel: String,
    val tempC: Double,
    val condition: WeatherCondition,
    val precipitationChance: Int
) {
    fun getTempFormatted(unit: TemperatureUnit): String {
        val temp = if (unit == TemperatureUnit.CELSIUS) tempC else (tempC * 9 / 5) + 32
        return "${temp.toInt()}°"
    }
}

data class DailyForecast(
    val dayName: String,
    val dateLabel: String,
    val minTempC: Double,
    val maxTempC: Double,
    val condition: WeatherCondition,
    val uvIndexMax: Double,
    val precipitationSumMm: Double,
    val sunriseTime: String,
    val sunsetTime: String
) {
    fun getMinTempFormatted(unit: TemperatureUnit): String {
        val temp = if (unit == TemperatureUnit.CELSIUS) minTempC else (minTempC * 9 / 5) + 32
        return "${temp.toInt()}°"
    }

    fun getMaxTempFormatted(unit: TemperatureUnit): String {
        val temp = if (unit == TemperatureUnit.CELSIUS) maxTempC else (maxTempC * 9 / 5) + 32
        return "${temp.toInt()}°"
    }
}

data class FullWeatherData(
    val location: CityLocation,
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    val aiInsight: String? = null
)
