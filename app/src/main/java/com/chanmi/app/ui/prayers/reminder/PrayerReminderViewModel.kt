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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrayerReminderViewModel @Inject constructor(
    private val repository: PrayerReminderRepository,
    private val prayerRepository: PrayerRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    /**
     * 알림 목록
     * null = 로딩 중, emptyList = 빈 목록, non-empty = 데이터 있음
     *
     * MutableStateFlow로 유지하여 드래그 재정렬 시 낙관적 업데이트 지원
     */
    private val _reminders = MutableStateFlow<List<PrayerReminder>?>(null)
    val reminders: StateFlow<List<PrayerReminder>?> = _reminders.asStateFlow()

    /** 기도문 선택에 사용할 전체 기도문 목록 */
    private val _availablePrayers = MutableStateFlow<List<Prayer>>(emptyList())
    val availablePrayers: StateFlow<List<Prayer>> = _availablePrayers.asStateFlow()

    /** 정확한 알림 권한 여부 */
    private val _canScheduleExact = MutableStateFlow(true)
    val canScheduleExact: StateFlow<Boolean> = _canScheduleExact.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAll().collect { _reminders.value = it }
        }
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

    /**
     * 드래그 재정렬 - 낙관적 업데이트 후 DB 저장
     *
     * @param fromIndex 드래그 시작 인덱스
     * @param toIndex 드래그 종료 인덱스
     */
    fun reorder(fromIndex: Int, toIndex: Int) {
        val current = _reminders.value ?: return
        if (fromIndex == toIndex) return
        val reordered = current.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        _reminders.value = reordered
        viewModelScope.launch {
            repository.reorder(reordered)
        }
    }

    /** ID로 알림 조회 (편집 화면 초기화용) */
    suspend fun getById(id: String): PrayerReminder? = repository.getById(id)
}
