package com.chanmi.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.chanmi.app.data.model.DailyRecord
import com.chanmi.app.data.model.DailyRecordWithDetails
import com.chanmi.app.data.model.GoodDeed
import com.chanmi.app.data.model.RosaryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRecordDao {

    @Transaction
    @Query("SELECT * FROM daily_records WHERE date >= :start AND date <= :end")
    fun getRecordsInRange(start: Long, end: Long): Flow<List<DailyRecordWithDetails>>

    @Transaction
    @Query("SELECT * FROM daily_records WHERE date = :date LIMIT 1")
    suspend fun getRecord(date: Long): DailyRecordWithDetails?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecord(record: DailyRecord): Long

    @Transaction
    suspend fun getOrCreateRecord(dateMillis: Long): Long {
        val existing = getRecord(dateMillis)
        if (existing != null) return existing.record.id
        val id = insertRecord(DailyRecord(date = dateMillis))
        return if (id != -1L) id else getRecord(dateMillis)!!.record.id
    }

    @Insert
    suspend fun insertRosaryEntry(entry: RosaryEntry)

    @Insert
    suspend fun insertGoodDeed(deed: GoodDeed)

    @Delete
    suspend fun deleteRosaryEntry(entry: RosaryEntry)

    @Delete
    suspend fun deleteGoodDeed(deed: GoodDeed)

    @Update
    suspend fun updateGoodDeed(deed: GoodDeed)
}
