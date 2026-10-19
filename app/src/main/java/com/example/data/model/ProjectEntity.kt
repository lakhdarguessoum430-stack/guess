package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val remoteId: String? = null,
    val name: String,
    val location: String,
    val budget: Double,
    val status: String = STATUS_IN_PROGRESS, // "جارية", "مكتملة", "متوقفة"
    val startDate: Long = System.currentTimeMillis(),
    val notes: String = "",
    val clientName: String = "",
    val supervisorName: String = "",
    val isSynced: Boolean = false
) {
    companion object {
        const val STATUS_IN_PROGRESS = "جارية"
        const val STATUS_COMPLETED = "مكتملة"
        const val STATUS_PAUSED = "متوقفة"

        val ALL_STATUSES = listOf(STATUS_IN_PROGRESS, STATUS_COMPLETED, STATUS_PAUSED)
    }
}
