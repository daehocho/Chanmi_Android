package com.chanmi.app.data.model

import com.google.android.gms.maps.model.LatLng

data class ChurchCluster(
    val churches: List<ChurchItem>,
    val center: LatLng
) {
    val id: String get() = churches.map { it.id }.sorted().joinToString("_")
    val count: Int get() = churches.size
    val name: String
        get() = when {
            churches.isEmpty() -> ""
            churches.size == 1 -> churches[0].name
            else -> "${churches[0].name} 외 ${churches.size - 1}개"
        }
    val coordinate: LatLng get() = center
}
