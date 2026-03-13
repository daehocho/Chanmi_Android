package com.chanmi.app.ui.prayers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chanmi.app.data.model.Prayer
import com.chanmi.app.data.model.PrayerCategory
import com.chanmi.app.data.repository.PrayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrayersViewModel @Inject constructor(
    private val repository: PrayerRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<List<PrayerCategory>>(emptyList())
    val categories: StateFlow<List<PrayerCategory>> = _categories.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Prayer>>(emptyList())
    val searchResults: StateFlow<List<Prayer>> = _searchResults.asStateFlow()

    init {
        loadPrayers()
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
