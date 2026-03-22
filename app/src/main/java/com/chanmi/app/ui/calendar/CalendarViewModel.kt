package com.chanmi.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chanmi.app.data.model.DailyRecordWithDetails
import com.chanmi.app.data.model.GoodDeed
import com.chanmi.app.data.model.RosaryEntry
import com.chanmi.app.data.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: CalendarRepository
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _dailyRecords = MutableStateFlow<Map<LocalDate, DailyRecordWithDetails>>(emptyMap())
    val dailyRecords: StateFlow<Map<LocalDate, DailyRecordWithDetails>> = _dailyRecords.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN)
    private var loadRecordsJob: Job? = null

    val monthTitle: String
        get() = _currentMonth.value.atDay(1).format(monthFormatter)

    init {
        loadRecords()
    }

    fun daysInMonth(): List<LocalDate?> {
        val ym = _currentMonth.value
        val firstDay = ym.atDay(1)
        val firstDayOfWeek = firstDay.dayOfWeek.value % 7 // Sunday=0

        val days = mutableListOf<LocalDate?>()
        repeat(firstDayOfWeek) { days.add(null) }
        for (day in 1..ym.lengthOfMonth()) {
            days.add(ym.atDay(day))
        }
        return days
    }

    fun isToday(date: LocalDate): Boolean = date == LocalDate.now()

    fun recordFor(date: LocalDate): DailyRecordWithDetails? = _dailyRecords.value[date]

    fun moveToNextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
        loadRecords()
    }

    fun moveToPreviousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
        loadRecords()
    }

    fun selectDate(date: LocalDate) {
        if (_selectedDate.value == date) {
            _selectedDate.value = null
        } else {
            _selectedDate.value = date
        }
    }

    fun addRosaryEntry(date: LocalDate, mysteryType: String, decadeCount: Int = 5) {
        viewModelScope.launch {
            repository.addRosaryEntry(date, mysteryType, decadeCount)
        }
    }

    fun addGoodDeed(date: LocalDate, content: String, category: String) {
        viewModelScope.launch {
            repository.addGoodDeed(date, content, category)
        }
    }

    fun deleteRosaryEntry(entry: RosaryEntry) {
        viewModelScope.launch {
            repository.deleteRosaryEntry(entry)
        }
    }

    fun deleteGoodDeed(deed: GoodDeed) {
        viewModelScope.launch {
            repository.deleteGoodDeed(deed)
        }
    }

    fun updateGoodDeed(deed: GoodDeed, content: String, category: String) {
        viewModelScope.launch {
            repository.updateGoodDeed(deed, content, category)
        }
    }

    fun undoDeleteRosaryEntry(entry: RosaryEntry) {
        viewModelScope.launch {
            repository.reInsertRosaryEntry(entry)
        }
    }

    fun undoDeleteGoodDeed(deed: GoodDeed) {
        viewModelScope.launch {
            repository.reInsertGoodDeed(deed)
        }
    }

    private fun loadRecords() {
        loadRecordsJob?.cancel()
        val ym = _currentMonth.value
        loadRecordsJob = viewModelScope.launch {
            repository.getRecordsForMonth(ym.year, ym.monthValue)
                .collect { records ->
                    val map = mutableMapOf<LocalDate, DailyRecordWithDetails>()
                    for (record in records) {
                        val date = java.time.Instant.ofEpochMilli(record.record.date)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                        map[date] = record
                    }
                    _dailyRecords.value = map
                }
        }
    }
}
