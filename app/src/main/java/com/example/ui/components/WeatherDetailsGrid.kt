package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CurrentWeather
import com.example.data.model.DailyForecast

@Composable
fun WeatherDetailsGrid(
    currentWeather: CurrentWeather,
    todayForecast: DailyForecast?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weather_details_grid"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailCard(
                title = "Humidity",
                value = "${currentWeather.humidity}%",
                subtitle = "Dew point is high",
                icon = Icons.Default.WaterDrop,
                modifier = Modifier.weight(1f)
            )

            DetailCard(
                title = "Wind",
                value = "${currentWeather.windSpeedKmH.toInt()} km/h",
                subtitle = "Dir: ${currentWeather.windDirectionCardinal} (${currentWeather.windDirectionDegrees}°)",
                icon = Icons.Default.Air,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailCard(
                title = "UV Index",
                value = "${currentWeather.uvIndex}",
                subtitle = when {
                    currentWeather.uvIndex >= 8 -> "Very High"
                    currentWeather.uvIndex >= 6 -> "High"
                    currentWeather.uvIndex >= 3 -> "Moderate"
                    else -> "Low"
                },
                icon = Icons.Default.LightMode,
                modifier = Modifier.weight(1f)
            )

            DetailCard(
                title = "Pressure",
                value = "${currentWeather.pressureHpa.toInt()} hPa",
                subtitle = "Normal atmosphere",
                icon = Icons.Default.Compress,
                modifier = Modifier.weight(1f)
            )
        }

        if (todayForecast != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailCard(
                    title = "Sunrise",
                    value = todayForecast.sunriseTime,
                    subtitle = "Morning dawn",
                    icon = Icons.Default.WbSunny,
                    modifier = Modifier.weight(1f)
                )

                DetailCard(
                    title = "Sunset",
                    value = todayForecast.sunsetTime,
                    subtitle = "Evening dusk",
                    icon = Icons.Default.WbTwilight,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun DetailCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
