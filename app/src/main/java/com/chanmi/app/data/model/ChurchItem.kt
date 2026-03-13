package com.chanmi.app.data.model

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ChurchData(
    val churches: List<ChurchItemDto>
)

@Serializable
data class ChurchItemDto(
    val name: String,
    val diocese: String,
    val address: String,
    val phone: String,
    val latitude: Double,
    val longitude: Double
)

data class ChurchItem(
    val name: String,
    val diocese: String,
    val address: String,
    val phone: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Double = 0.0
) {
    val id: String get() = "${name}_${latitude}_${longitude}"
    val latLng: LatLng get() = LatLng(latitude, longitude)

    val formattedDistance: String
        get() = if (distance < 1000) {
            "${distance.toInt()}m"
        } else {
            String.format("%.1fkm", distance / 1000)
        }

    fun openInMaps(context: Context) {
        val uri = Uri.parse("geo:${latitude},${longitude}?q=${Uri.encode(name)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }
}

fun ChurchItemDto.toChurchItem(): ChurchItem = ChurchItem(
    name = name,
    diocese = diocese,
    address = address,
    phone = phone,
    latitude = latitude,
    longitude = longitude
)
