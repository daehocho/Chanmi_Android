package com.chanmi.app.data.repository

import android.content.Context
import com.chanmi.app.data.model.Prayer
import com.chanmi.app.data.model.PrayerCategory
import com.chanmi.app.data.model.PrayerData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class PrayerRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var cachedCategories: List<PrayerCategory>? = null
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()

    suspend fun fetchCategories(): List<PrayerCategory> = mutex.withLock {
        cachedCategories?.let { return@withLock it }
        withContext(Dispatchers.IO) {
            val jsonString = context.assets.open("prayers.json")
                .bufferedReader().use { it.readText() }
            val data = json.decodeFromString<PrayerData>(jsonString)
            cachedCategories = data.categories
            data.categories
        }
    }

    suspend fun searchPrayers(query: String): List<Prayer> {
        val categories = fetchCategories()
        val lowerQuery = query.lowercase()
        return categories.flatMap { it.prayers }
            .filter {
                it.title.lowercase().contains(lowerQuery) ||
                        it.content.lowercase().contains(lowerQuery)
            }
    }
}
