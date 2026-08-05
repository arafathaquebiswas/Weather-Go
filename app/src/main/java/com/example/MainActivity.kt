package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.CitySearchBottomSheet
import com.example.ui.components.DailyForecastSection
import com.example.ui.components.HourlyForecastSection
import com.example.ui.components.SmartInsightCard
import com.example.ui.components.WeatherAtmosphereBackground
import com.example.ui.components.WeatherDetailsGrid
import com.example.ui.components.WeatherHeroCard
import com.example.ui.theme.WeatherTheme
import com.example.ui.viewmodel.WeatherUiState
import com.example.ui.viewmodel.WeatherViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {

    private val viewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WeatherTheme {
                WeatherAppContent(
                    viewModel = viewModel,
                    onRequestGps = { requestGpsLocation() }
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestGpsLocation() {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        viewModel.updateGpsLocation(location.latitude, location.longitude)
                        Toast.makeText(this, "Updated to GPS Location", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Could not fetch GPS location. Searching city instead.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to get location: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Location error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun WeatherAppContent(
    viewModel: WeatherViewModel,
    onRequestGps: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val savedCities by viewModel.savedCities.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    var showSearchSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val currentCondition = (uiState as? WeatherUiState.Success)?.weatherData?.current?.condition

        WeatherAtmosphereBackground(
            condition = currentCondition,
            modifier = Modifier.padding(innerPadding)
        ) {
            when (val state = uiState) {
                is WeatherUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("loading_view"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading live forecast...",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                is WeatherUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .testTag("error_view"),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.Black.copy(alpha = 0.4f),
                            contentColor = Color.White
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Connection Error",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = { viewModel.refreshWeather() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFF0F172A)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Retry", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                is WeatherUiState.Success -> {
                    val weatherData = state.weatherData
                    val isSaved = savedCities.any {
                        it.latitude == weatherData.location.latitude && it.longitude == weatherData.location.longitude
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("weather_main_list"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Hero Main Card
                        item {
                            WeatherHeroCard(
                                location = weatherData.location,
                                currentWeather = weatherData.current,
                                todayForecast = weatherData.daily.firstOrNull(),
                                temperatureUnit = state.temperatureUnit,
                                isSaved = isSaved,
                                onToggleUnit = { viewModel.toggleTemperatureUnit() },
                                onOpenSearch = { showSearchSheet = true },
                                onToggleFavorite = {
                                    if (isSaved) {
                                        viewModel.removeCityFromFavorites(weatherData.location)
                                    } else {
                                        viewModel.saveCityToFavorites(weatherData.location)
                                    }
                                }
                            )
                        }

                        // Smart Insight AI Card
                        weatherData.aiInsight?.let { insight ->
                            item {
                                SmartInsightCard(insightText = insight)
                            }
                        }

                        // Hourly Forecast Row
                        if (weatherData.hourly.isNotEmpty()) {
                            item {
                                HourlyForecastSection(
                                    hourlyList = weatherData.hourly,
                                    temperatureUnit = state.temperatureUnit
                                )
                            }
                        }

                        // 7-Day Forecast Card
                        if (weatherData.daily.isNotEmpty()) {
                            item {
                                DailyForecastSection(
                                    dailyList = weatherData.daily,
                                    temperatureUnit = state.temperatureUnit
                                )
                            }
                        }

                        // Weather Grid Details (Humidity, Wind, UV, Pressure, Sun)
                        item {
                            WeatherDetailsGrid(
                                currentWeather = weatherData.current,
                                todayForecast = weatherData.daily.firstOrNull()
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }

        // Location Search Bottom Sheet Modal
        if (showSearchSheet) {
            CitySearchBottomSheet(
                sheetState = sheetState,
                searchQuery = searchQuery,
                searchResults = searchResults,
                savedCities = savedCities,
                selectedCity = selectedCity,
                isSearching = isSearching,
                onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                onSelectCity = { city ->
                    viewModel.selectCity(city)
                },
                onSaveCity = { city ->
                    viewModel.saveCityToFavorites(city)
                },
                onDeleteCity = { city ->
                    viewModel.removeCityFromFavorites(city)
                },
                onRequestGpsLocation = {
                    if (locationPermissionState.status.isGranted) {
                        onRequestGps()
                    } else {
                        locationPermissionState.launchPermissionRequest()
                    }
                },
                onDismissRequest = { showSearchSheet = false }
            )
        }
    }
}
