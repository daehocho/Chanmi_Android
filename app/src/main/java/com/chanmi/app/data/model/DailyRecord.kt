package com.chanmi.app.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "daily_records")
data class DailyRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long // epoch millis, normalized to start of day
)

data class DailyRecordWithDetails(
    @Embedded val record: DailyRecord,
    @Relation(parentColumn = "id", entityColumn = "dailyRecordId")
    val rosaryEntries: List<RosaryEntry>,
    @Relation(parentColumn = "id", entityColumn = "dailyRecordId")
    val goodDeeds: List<GoodDeed>
)
