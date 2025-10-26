package com.bharatkrishi.app.network

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

// Base URL for the AGMARKET API
private const val BASE_URL = "https://api.data.gov.in/resource/"

// Create a Retrofit instance
private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

/**
 * Defines the API endpoints. Retrofit will create the implementation.
 */
interface AgriApiService {
    @GET("9ef84268-d588-465a-a308-a864a43d0070") // This is the specific resource ID for the market data
    suspend fun getMarketData(
        @Query("api-key") apiKey: String,
        @Query("format") format: String,
        @Query("offset") offset: String,
        @Query("limit") limit: String,
        @QueryMap filters: Map<String, String>
    ): AgriApiResponse // The response should be wrapped in a main response object
}

/**
 * A wrapper class for the entire API response, as the 'records' are nested in the JSON.
 */
data class AgriApiResponse(
    val records: List<AgriApiRecord>
)

/**
 * Public object that exposes the Retrofit service to the rest of the app.
 */
object AgriApi {
    val retrofitService: AgriApiService by lazy {
        retrofit.create(AgriApiService::class.java)
    }
}