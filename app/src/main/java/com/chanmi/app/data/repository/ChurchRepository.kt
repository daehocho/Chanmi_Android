package com.chanmi.app.data.repository

import android.content.Context
import com.chanmi.app.data.model.ChurchData
import com.chanmi.app.data.model.ChurchItem
import com.chanmi.app.data.model.toChurchItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ChurchRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var cachedData: List<ChurchItem>? = null
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadChurches(): List<ChurchItem> = withContext(Dispatchers.IO) {
        cachedData?.let { return@withContext it }
        val jsonString = context.assets.open("korean_catholic_churches.json")
            .bufferedReader().use { it.readText() }
        val data = json.decodeFromString<ChurchData>(jsonString)
        val churches = data.churches.map { it.toChurchItem() }
        cachedData = churches
        churches
    }
}
