package com.chanmi.app.ui.prayers

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chanmi.app.data.model.Prayer
import com.chanmi.app.data.model.PrayerCategory
import com.chanmi.app.data.repository.PrayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrayersViewModel @Inject constructor(
    private val repository: PrayerRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object {
        private val PRAYER_FONT_SIZE_KEY = floatPreferencesKey("prayerFontSize")
    }

    private val _categories = MutableStateFlow<List<PrayerCategory>>(emptyList())
    val categories: StateFlow<List<PrayerCategory>> = _categories.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Prayer>>(emptyList())
    val searchResults: StateFlow<List<Prayer>> = _searchResults.asStateFlow()

    val prayerFontSize: StateFlow<Float> = dataStore.data
        .map { it[PRAYER_FONT_SIZE_KEY] ?: 18f }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 18f)

    init {
        loadPrayers()
    }

    fun updatePrayerFontSize(size: Float) {
        viewModelScope.launch {
            dataStore.edit { it[PRAYER_FONT_SIZE_KEY] = size }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
        } else {
            _searchResults.value = _categories.value
                .flatMap { it.prayers }
                .filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.content.contains(query, ignoreCase = true)
                }
        }
    }

    private fun loadPrayers() {
        viewModelScope.launch {
            _categories.value = repository.fetchCategories()
        }
    }
}
