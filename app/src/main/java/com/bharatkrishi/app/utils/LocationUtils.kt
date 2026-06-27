package com.bharatkrishi.app.utils

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object LocationUtils {

    suspend fun getAddressFromLatLng(context: Context, latLng: LatLng): AddressResult {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) 
                
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val village = address.subLocality ?: address.featureName ?: "Unknown Village"
                    val district = address.subAdminArea ?: address.locality ?: "Unknown District"
                    val state = address.adminArea ?: "Unknown State"
                    val fullAddress = address.getAddressLine(0) ?: "$village, $district, $state"
                    
                    AddressResult(
                        village = village,
                        district = district,
                        state = state,
                        fullAddress = fullAddress,
                        latitude = latLng.latitude,
                        longitude = latLng.longitude
                    )
                } else {
                    AddressResult(error = "Location not found")
                }
            } catch (e: Exception) {
                if (com.bharatkrishi.app.BuildConfig.DEBUG) {
                    android.util.Log.e("LocationUtils", "Geocoding failed", e)
                }
                AddressResult(error = "Geocoding failed")
            }
        }
    }
}

data class AddressResult(
    val village: String = "",
    val district: String = "",
    val state: String = "",
    val fullAddress: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val error: String? = null
)
