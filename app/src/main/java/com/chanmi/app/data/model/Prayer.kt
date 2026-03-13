package com.chanmi.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PrayerData(
    val categories: List<PrayerCategory>
)

@Serializable
data class PrayerCategory(
    val id: String,
    val name: String,
    val icon: String,
    val prayers: List<Prayer>
)

@Serializable
data class Prayer(
    val id: String,
    val title: String,
    val content: String
)
