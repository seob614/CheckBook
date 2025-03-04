package com.courr.checkbook.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

@SuppressLint("MissingPermission")
suspend fun getCurrentLocationAsString(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient
): String {
    return try {
        val location = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).await()

        if (location != null) {
            getAddressFromLocation(context, location.latitude, location.longitude)
        } else {
            "위치를 가져올 수 없습니다."
        }
    } catch (e: Exception) {
        "위치 가져오기 실패: ${e.message}"
    }
}


suspend fun getAddressFromLocation(context: Context, lat: Double, lon: Double): String {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                "${address.locality} ${address.thoroughfare}"
            } else {
                "주소를 찾을 수 없습니다."
            }
        } catch (e: IOException) {
            "주소 변환 실패"
        }
    }
}
