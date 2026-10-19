package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val title: String,
    val amount: Double,
    val category: String, // مواد بناء, عمالة وأجور, آليات ومعدات, خدمات ورخص, أخرى
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    companion object {
        const val CAT_MATERIALS = "مواد بناء"
        const val CAT_LABOR = "عمالة وأجور"
        const val CAT_EQUIPMENT = "آليات ومعدات"
        const val CAT_PERMITS = "رخص واستشارات"
        const val CAT_OTHER = "مصاريف أخرى"

        val ALL_CATEGORIES = listOf(
            CAT_MATERIALS,
            CAT_LABOR,
            CAT_EQUIPMENT,
            CAT_PERMITS,
            CAT_OTHER
        )
    }
}
