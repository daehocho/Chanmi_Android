package com.chanmi.app.data.repository

import com.chanmi.app.data.local.DailyRecordDao
import com.chanmi.app.data.model.DailyRecord
import com.chanmi.app.data.model.DailyRecordWithDetails
import com.chanmi.app.data.model.GoodDeed
import com.chanmi.app.data.model.RosaryEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class CalendarRepository @Inject constructor(
    private val dao: DailyRecordDao
) {
    fun getRecordsForMonth(year: Int, month: Int): Flow<List<DailyRecordWithDetails>> {
        val start = LocalDate.of(year, month, 1)
        val end = start.plusMonths(1).minusDays(1)
        return dao.getRecordsInRange(
            start.toEpochMillis(),
            end.toEpochMillis()
        )
    }

    suspend fun getRecord(date: LocalDate): DailyRecordWithDetails? {
        return dao.getRecord(date.toEpochMillis())
    }

    suspend fun addRosaryEntry(date: LocalDate, mysteryType: String) {
        val recordId = getOrCreateRecord(date)
        dao.insertRosaryEntry(
            RosaryEntry(
                mysteryType = mysteryType,
                completedAt = System.currentTimeMillis(),
                dailyRecordId = recordId
            )
        )
    }

    suspend fun addGoodDeed(date: LocalDate, content: String, category: String) {
        val recordId = getOrCreateRecord(date)
        dao.insertGoodDeed(
            GoodDeed(
                content = content,
                category = category,
                createdAt = System.currentTimeMillis(),
                dailyRecordId = recordId
            )
        )
    }

    suspend fun deleteRosaryEntry(entry: RosaryEntry) {
        dao.deleteRosaryEntry(entry)
    }

    suspend fun deleteGoodDeed(deed: GoodDeed) {
        dao.deleteGoodDeed(deed)
    }

    suspend fun updateGoodDeed(deed: GoodDeed, content: String, category: String) {
        dao.updateGoodDeed(deed.copy(content = content, category = category))
    }

    private suspend fun getOrCreateRecord(date: LocalDate): Long {
        val epochMillis = date.toEpochMillis()
        val existing = dao.getRecord(epochMillis)
        return existing?.record?.id ?: dao.insertRecord(DailyRecord(date = epochMillis))
    }

    private fun LocalDate.toEpochMillis(): Long {
        return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
