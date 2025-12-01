package com.bharatkrishi.app.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstanceWeather {
    private  const val baseUrl = "https://api.weatherapi.com/"

    private fun getInstance(): Retrofit{
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build()
    }

    val weatherApi: WeatherApi = getInstance().create(WeatherApi::class.java)

}