package com.bharatkrishi.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bharatkrishi.app.network.NetworkResponse
import com.bharatkrishi.app.network.RetrofitInstanceWeather
import com.bharatkrishi.app.network.WeatherConstant
import com.bharatkrishi.app.network.WeatherModel
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val weatherApi = RetrofitInstanceWeather.weatherApi
    private val _weatherResult = MutableLiveData<NetworkResponse<WeatherModel>>()
    val weatherResult: LiveData<NetworkResponse<WeatherModel>> = _weatherResult

    fun getData(city: String) {
        _weatherResult.value = NetworkResponse.Loading
        viewModelScope.launch {
            try{
                val response = weatherApi.getWeather(WeatherConstant.apiKey, city)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        _weatherResult.value = NetworkResponse.Success(body)
                    } else {
                        _weatherResult.value = NetworkResponse.Error("Empty response from weather service")
                    }
                } else {
                    _weatherResult.value = NetworkResponse.Error("Failed to load weather (${response.code()})")
                }
            } catch (e : Exception){
                android.util.Log.e("WeatherViewModel", "Error fetching weather", e)
                _weatherResult.value = NetworkResponse.Error("Failed: ${e.message}")
            }

        }
    }
}


