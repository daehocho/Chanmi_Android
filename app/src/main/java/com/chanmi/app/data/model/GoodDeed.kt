package com.chanmi.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "good_deeds",
    foreignKeys = [ForeignKey(
        entity = DailyRecord::class,
        parentColumns = ["id"],
        childColumns = ["dailyRecordId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("dailyRecordId")]
)
data class GoodDeed(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val category: String,
    val createdAt: Long,
    val dailyRecordId: Long
)
