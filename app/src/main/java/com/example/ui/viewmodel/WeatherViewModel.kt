package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CityLocation
import com.example.data.model.FullWeatherData
import com.example.data.model.TemperatureUnit
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(
        val weatherData: FullWeatherData,
        val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS
    ) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

@OptIn(FlowPreview::class)
class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WeatherRepository

    val defaultCity = CityLocation(
        name = "Tokyo",
        country = "Japan",
        adminArea = "Tokyo",
        latitude = 35.6762,
        longitude = 139.6503
    )

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _temperatureUnit = MutableStateFlow(TemperatureUnit.CELSIUS)
    val temperatureUnit: StateFlow<TemperatureUnit> = _temperatureUnit.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<CityLocation>>(emptyList())
    val searchResults: StateFlow<List<CityLocation>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _selectedCity = MutableStateFlow<CityLocation?>(null)
    val selectedCity: StateFlow<CityLocation?> = _selectedCity.asStateFlow()

    val savedCities: StateFlow<List<CityLocation>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = WeatherRepository(database.cityDao())

        savedCities = repository.getSavedCities().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Default setup with Tokyo
        _selectedCity.value = defaultCity
        fetchWeatherForCity(defaultCity)

        // Auto debounce search
        viewModelScope.launch {
            _searchQuery
                .debounce(350)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.trim().length >= 2) {
                        _isSearching.value = true
                        val results = repository.searchCities(query.trim())
                        _searchResults.value = results
                        _isSearching.value = false
                    } else {
                        _searchResults.value = emptyList()
                        _isSearching.value = false
                    }
                }
        }
    }

    fun fetchWeatherForCity(city: CityLocation) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val data = repository.getFullWeatherData(city)
                _uiState.value = WeatherUiState.Success(
                    weatherData = data,
                    temperatureUnit = _temperatureUnit.value
                )
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(
                    message = e.localizedMessage ?: "Failed to connect to weather service. Please check your internet connection."
                )
            }
        }
    }

    fun selectCity(city: CityLocation) {
        _selectedCity.value = city
        fetchWeatherForCity(city)
    }

    fun updateGpsLocation(latitude: Double, longitude: Double) {
        val gpsCity = CityLocation(
            name = "Current Location",
            country = "GPS",
            latitude = latitude,
            longitude = longitude,
            isCurrentLocation = true
        )
        selectCity(gpsCity)
    }

    fun toggleTemperatureUnit() {
        val newUnit = if (_temperatureUnit.value == TemperatureUnit.CELSIUS) {
            TemperatureUnit.FAHRENHEIT
        } else {
            TemperatureUnit.CELSIUS
        }
        _temperatureUnit.value = newUnit

        val currentState = _uiState.value
        if (currentState is WeatherUiState.Success) {
            _uiState.value = currentState.copy(temperatureUnit = newUnit)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun saveCityToFavorites(city: CityLocation) {
        viewModelScope.launch {
            repository.saveCity(city)
        }
    }

    fun removeCityFromFavorites(city: CityLocation) {
        viewModelScope.launch {
            repository.removeCity(city)
        }
    }

    fun refreshWeather() {
        val city = _selectedCity.value ?: defaultCity
        fetchWeatherForCity(city)
    }
}
