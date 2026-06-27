package com.bharatkrishi.app.network

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap
import java.util.concurrent.TimeUnit


private const val BASE_URL = "https://api.data.gov.in/resource/"

private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

private val retrofit = Retrofit.Builder() 
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()


interface AgriApiService {
    @GET("9ef84268-d588-465a-a308-a864a43d0070") 
    suspend fun getMarketData(
        @Query("api-key") apiKey: String,
        @Query("format") format: String,
        @Query("offset") offset: String,
        @Query("limit") limit: String,
        @QueryMap filters: Map<String, String>
    ): AgriApiResponse 
}

data class AgriApiResponse(  
    @SerializedName("records")
    val records: List<AgriApiRecord>
)

object AgriApi {   
    val retrofitService: AgriApiService by lazy {
        retrofit.create(AgriApiService::class.java)
    }
}