package com.chanmi.app.ui.nearbychurch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chanmi.app.data.model.ChurchCluster
import com.chanmi.app.data.model.ChurchItem
import com.chanmi.app.data.repository.ChurchRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class NearbyChurchViewModel @Inject constructor(
    private val repository: ChurchRepository
) : ViewModel() {

    private val _churches = MutableStateFlow<List<ChurchItem>>(emptyList())
    val churches: StateFlow<List<ChurchItem>> = _churches.asStateFlow()

    private val _clusters = MutableStateFlow<List<ChurchCluster>>(emptyList())
    val clusters: StateFlow<List<ChurchCluster>> = _clusters.asStateFlow()

    private val _selectedChurch = MutableStateFlow<ChurchItem?>(null)
    val selectedChurch: StateFlow<ChurchItem?> = _selectedChurch.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showSearchThisAreaButton = MutableStateFlow(false)
    val showSearchThisAreaButton: StateFlow<Boolean> = _showSearchThisAreaButton.asStateFlow()

    private val _isSearchingDifferentArea = MutableStateFlow(false)
    val isSearchingDifferentArea: StateFlow<Boolean> = _isSearchingDifferentArea.asStateFlow()

    var currentMapCenter: LatLng? = null
        private set

    private var lastSearchedCenter: LatLng? = null
    private var clusterRadiusKm: Double = -1.0

    fun searchNearbyChurches(coordinate: LatLng) {
        viewModelScope.launch {
            _isLoading.value = true
            val allChurches = repository.loadChurches()
            val radiusMeters = 10_000.0

            _churches.value = allChurches.mapNotNull { church ->
                val dist = haversineDistance(
                    coordinate.latitude, coordinate.longitude,
                    church.latitude, church.longitude
                )
                if (dist <= radiusMeters) church.copy(distance = dist) else null
            }.sortedBy { it.distance }

            lastSearchedCenter = coordinate
            _showSearchThisAreaButton.value = false
            _isSearchingDifferentArea.value = false
            _isLoading.value = false

            updateClusters(5000.0)
        }
    }

    fun updateClusters(cameraDistance: Double) {
        val newRadius = when {
            cameraDistance <= 5_000 -> 0.0
            cameraDistance <= 15_000 -> 1.0
            cameraDistance <= 30_000 -> 2.0
            cameraDistance <= 60_000 -> 3.0
            cameraDistance <= 150_000 -> 4.0
            cameraDistance <= 300_000 -> 6.0
            else -> 10.0
        }

        if (newRadius == clusterRadiusKm) return
        clusterRadiusKm = newRadius
        clusterChurches(newRadius)
    }

    fun updateMapCenter(center: LatLng) {
        currentMapCenter = center
    }

    fun checkIfMapMovedSignificantly() {
        val lastCenter = lastSearchedCenter ?: return
        val mapCenter = currentMapCenter ?: return
        val distance = haversineDistance(
            lastCenter.latitude, lastCenter.longitude,
            mapCenter.latitude, mapCenter.longitude
        )
        _showSearchThisAreaButton.value = distance > 1000
    }

    fun checkIfReturnedToUserLocation(userLocation: LatLng) {
        val mapCenter = currentMapCenter ?: return
        val distance = haversineDistance(
            mapCenter.latitude, mapCenter.longitude,
            userLocation.latitude, userLocation.longitude
        )
        if (distance <= 1000) {
            _isSearchingDifferentArea.value = false
            _showSearchThisAreaButton.value = false
        }
    }

    fun searchChurchesInCurrentArea() {
        val center = currentMapCenter ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val allChurches = repository.loadChurches()
            val radiusMeters = 10_000.0

            _churches.value = allChurches.mapNotNull { church ->
                val dist = haversineDistance(
                    center.latitude, center.longitude,
                    church.latitude, church.longitude
                )
                if (dist <= radiusMeters) church.copy(distance = dist) else null
            }.sortedBy { it.distance }

            lastSearchedCenter = center
            _showSearchThisAreaButton.value = false
            _isSearchingDifferentArea.value = true
            _isLoading.value = false

            updateClusters(5000.0)
        }
    }

    fun selectChurch(church: ChurchItem) {
        _selectedChurch.value = church
    }

    fun clearSelection() {
        _selectedChurch.value = null
    }

    fun zoomToCluster(cluster: ChurchCluster): LatLng? {
        if (cluster.count < 2) return null
        return cluster.center
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private fun clusterChurches(radiusKm: Double) {
        val currentChurches = _churches.value
        if (radiusKm <= 0) {
            _clusters.value = currentChurches.map { church ->
                ChurchCluster(
                    churches = listOf(church),
                    center = church.latLng
                )
            }
            return
        }

        val radiusMeters = radiusKm * 1000
        val latDelta = radiusMeters / 111_000.0
        val remaining = currentChurches.toMutableList()
        val result = mutableListOf<ChurchCluster>()

        while (remaining.isNotEmpty()) {
            val seed = remaining.removeFirst()
            val lonDelta = radiusMeters / (111_000.0 * cos(Math.toRadians(seed.latitude)))
            val grouped = mutableListOf(seed)

            remaining.removeAll { candidate ->
                if (abs(candidate.latitude - seed.latitude) > latDelta ||
                    abs(candidate.longitude - seed.longitude) > lonDelta
                ) return@removeAll false

                if (haversineDistance(
                        seed.latitude, seed.longitude,
                        candidate.latitude, candidate.longitude
                    ) <= radiusMeters
                ) {
                    grouped.add(candidate)
                    true
                } else false
            }

            val centerLat = grouped.sumOf { it.latitude } / grouped.size
            val centerLon = grouped.sumOf { it.longitude } / grouped.size

            result.add(
                ChurchCluster(
                    churches = grouped,
                    center = LatLng(centerLat, centerLon)
                )
            )
        }

        _clusters.value = result
    }
}
