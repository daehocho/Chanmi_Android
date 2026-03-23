package com.chanmi.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 기도 알림 엔티티
 *
 * weekdays: 콤마 구분 문자열 (1=일, 2=월, ..., 7=토). 빈 문자열 = 매일 반복.
 */
@Entity(tableName = "prayer_reminders")
data class PrayerReminder(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val prayerId: String,
    val prayerTitle: String,
    val categoryName: String = "",
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val weekdays: String = "", // "1,3,5" 또는 "" (매일)
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** 요일 문자열 → Set<Int> 변환 */
    fun weekdaySet(): Set<Int> {
        if (weekdays.isBlank()) return emptySet()
        return weekdays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    }

    /** "오전/오후 H:MM" 포맷 */
    val formattedTime: String
        get() {
            val period = if (hour < 12) "오전" else "오후"
            val displayHour = if (hour % 12 == 0) 12 else hour % 12
            return "$period $displayHour:${minute.toString().padStart(2, '0')}"
        }

    /** 요일 요약 텍스트 */
    val weekdayText: String
        get() {
            val days = weekdaySet()
            if (days.isEmpty()) return "매일"
            if (days.size == 7) return "매일"
            val names = listOf("일", "월", "화", "수", "목", "금", "토")
            return days.sorted().map { names[it - 1] }.joinToString(" ")
        }

    /** AlarmManager requestCode 생성 (요일별 구분) */
    fun requestCode(weekday: Int = 0): Int = id.hashCode() + weekday

    companion object {
        /** Set<Int> → 콤마 구분 문자열 */
        fun weekdaysToString(weekdays: Set<Int>): String {
            if (weekdays.isEmpty() || weekdays.size == 7) return ""
            return weekdays.sorted().joinToString(",")
        }
    }
}
