package com.chanmi.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rosary_entries",
    foreignKeys = [ForeignKey(
        entity = DailyRecord::class,
        parentColumns = ["id"],
        childColumns = ["dailyRecordId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("dailyRecordId")]
)
data class RosaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mysteryType: String,
    val completedAt: Long,
    val dailyRecordId: Long,
    @ColumnInfo(name = "decadeCount", defaultValue = "5")
    val decadeCount: Int = 5
)
