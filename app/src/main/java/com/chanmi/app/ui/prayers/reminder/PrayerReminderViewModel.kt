package com.chanmi.app.ui.prayers.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chanmi.app.data.model.Prayer
import com.chanmi.app.data.model.PrayerReminder
import com.chanmi.app.data.repository.PrayerReminderRepository
import com.chanmi.app.data.repository.PrayerRepository
import com.chanmi.app.notification.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrayerReminderViewModel @Inject constructor(
    private val repository: PrayerReminderRepository,
    private val prayerRepository: PrayerRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    /**
     * 알림 목록 (Room Flow → StateFlow)
     * null = 로딩 중, emptyList = 빈 목록, non-empty = 데이터 있음
     */
    val reminders: StateFlow<List<PrayerReminder>?> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** 기도문 선택에 사용할 전체 기도문 목록 */
    private val _availablePrayers = MutableStateFlow<List<Prayer>>(emptyList())
    val availablePrayers: StateFlow<List<Prayer>> = _availablePrayers.asStateFlow()

    /** 정확한 알림 권한 여부 */
    private val _canScheduleExact = MutableStateFlow(true)
    val canScheduleExact: StateFlow<Boolean> = _canScheduleExact.asStateFlow()

    init {
        loadAvailablePrayers()
        checkExactAlarmPermission()
    }

    private fun loadAvailablePrayers() {
        viewModelScope.launch {
            _availablePrayers.value = prayerRepository.fetchCategories()
                .flatMap { it.prayers }
        }
    }

    fun checkExactAlarmPermission() {
        _canScheduleExact.value = alarmScheduler.canScheduleExactAlarms()
    }

    fun saveReminder(reminder: PrayerReminder) {
        viewModelScope.launch {
            repository.save(reminder)
        }
    }

    fun deleteReminder(reminder: PrayerReminder) {
        viewModelScope.launch {
            repository.delete(reminder)
        }
    }

    fun toggleEnabled(reminder: PrayerReminder) {
        viewModelScope.launch {
            repository.toggleEnabled(reminder)
        }
    }

    /** 삭제 취소 (Snackbar undo) — 다시 저장 */
    fun undoDelete(reminder: PrayerReminder) {
        viewModelScope.launch {
            repository.save(reminder)
        }
    }

    /** ID로 알림 조회 (편집 화면 초기화용) */
    suspend fun getById(id: String): PrayerReminder? = repository.getById(id)
}
