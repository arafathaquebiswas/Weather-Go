package com.example.util

import com.example.data.model.WeatherCondition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WeatherUtils {

    fun getWeatherCondition(code: Int, isDay: Boolean = true): WeatherCondition {
        return when (code) {
            0 -> WeatherCondition(code, "Clear Sky", if (isDay) "Sunny with clear blue skies" else "Clear starry night", isDay)
            1 -> WeatherCondition(code, "Mainly Clear", "Mostly sunny with a few passing clouds", isDay)
            2 -> WeatherCondition(code, "Partly Cloudy", "Scattered clouds with periods of sunshine", isDay)
            3 -> WeatherCondition(code, "Overcast", "Overcast sky with dense cloud cover", isDay)
            45, 48 -> WeatherCondition(code, "Foggy", "Foggy conditions with reduced visibility", isDay)
            51 -> WeatherCondition(code, "Light Drizzle", "Light patchy drizzle", isDay)
            53 -> WeatherCondition(code, "Moderate Drizzle", "Steady drizzle falling", isDay)
            55 -> WeatherCondition(code, "Dense Drizzle", "Heavy drizzle mist", isDay)
            61 -> WeatherCondition(code, "Slight Rain", "Light rain showers", isDay)
            63 -> WeatherCondition(code, "Moderate Rain", "Moderate rainfall", isDay)
            65 -> WeatherCondition(code, "Heavy Rain", "Heavy downpour rain", isDay)
            71 -> WeatherCondition(code, "Slight Snow", "Light flurries and snow", isDay)
            73 -> WeatherCondition(code, "Moderate Snow", "Moderate snowfall", isDay)
            75 -> WeatherCondition(code, "Heavy Snow", "Heavy blizzard snowfall", isDay)
            80, 81, 82 -> WeatherCondition(code, "Rain Showers", "Intermittent rain showers", isDay)
            85, 86 -> WeatherCondition(code, "Snow Showers", "Snow showers", isDay)
            95 -> WeatherCondition(code, "Thunderstorm", "Thunderstorm with rain", isDay)
            96, 99 -> WeatherCondition(code, "Severe Thunderstorm", "Severe thunderstorm with hail", isDay)
            else -> WeatherCondition(code, "Partly Cloudy", "Variable cloudiness", isDay)
        }
    }

    fun formatHourTime(isoTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            val date = inputFormat.parse(isoTime)
            val outputFormat = SimpleDateFormat("ha", Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: isoTime
        } catch (e: Exception) {
            if (isoTime.contains("T")) {
                val hour = isoTime.substringAfter("T").take(2)
                "${hour.toIntOrNull() ?: 12}:00"
            } else isoTime
        }
    }

    fun formatDayName(isoDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(isoDate)
            val outputFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            date?.let {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                if (isoDate == todayStr) "Today" else outputFormat.format(it)
            } ?: isoDate
        } catch (e: Exception) {
            isoDate
        }
    }

    fun formatDateLabel(isoDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(isoDate)
            val outputFormat = SimpleDateFormat("MMM d", Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: isoDate
        } catch (e: Exception) {
            isoDate
        }
    }

    fun formatSunTime(isoTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            val date = inputFormat.parse(isoTime)
            val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: isoTime
        } catch (e: Exception) {
            if (isoTime.contains("T")) isoTime.substringAfter("T") else isoTime
        }
    }

    fun generateSmartInsight(tempC: Double, code: Int, uvMax: Double, windSpeedKmH: Double): String {
        return when {
            code in 95..99 -> "⛈️ Severe thunderstorm warning in effect! Stay indoors and secure loose outdoor items."
            code in 61..65 || code in 80..82 -> "☔ Rainfall expected today. Don't forget your umbrella and waterproof outerwear."
            code in 71..86 -> "❄️ Snowfall alert! Bundle up with warm layers and take caution when driving."
            uvMax >= 8.0 -> "☀️ Very high UV index ($uvMax). Apply SPF 50+ sunscreen, wear sunglasses, and seek shade during peak midday hours."
            tempC >= 32.0 -> "🔥 High heat ($tempC°C). Stay hydrated, avoid heavy outdoors activity, and drink plenty of water."
            tempC <= 5.0 -> "🥶 Chilly weather ($tempC°C). Wear a heavy coat, scarf, and insulated gloves."
            windSpeedKmH >= 35.0 -> "💨 High winds ($windSpeedKmH km/h). Keep a hold on light accessories and watch for tree branches."
            else -> "✨ Pleasant weather condition overall! Perfect day for outdoor activities and fresh air walks."
        }
    }
}
