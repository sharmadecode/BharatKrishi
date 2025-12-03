package com.bharatkrishi.app.network

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

// Base URL for the MARKET API
private const val BASE_URL = "https://api.data.gov.in/resource/"

private val retrofit = Retrofit.Builder() //Retrofit instance
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()


interface AgriApiService {
    @GET("9ef84268-d588-465a-a308-a864a43d0070") //End point of url
    suspend fun getMarketData(
        @Query("api-key") apiKey: String,
        @Query("format") format: String,
        @Query("offset") offset: String,
        @Query("limit") limit: String,
        @QueryMap filters: Map<String, String>
    ): AgriApiResponse // The response should be wrapped in a main response object
}

data class AgriApiResponse(  // wrapper class for the entire API response
    @SerializedName("records")
    val records: List<AgriApiRecord>
)

object AgriApi {   //to exposes the retrofit service to the rest of the app.
    val retrofitService: AgriApiService by lazy {
        retrofit.create(AgriApiService::class.java)
    }
}