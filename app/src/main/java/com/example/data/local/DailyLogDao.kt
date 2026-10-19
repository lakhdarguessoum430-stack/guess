package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DailyLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {

    @Query("SELECT * FROM daily_logs WHERE projectId = :projectId ORDER BY date DESC")
    fun getLogsForProject(projectId: Long): Flow<List<DailyLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DailyLogEntity): Long

    @Delete
    suspend fun deleteLog(log: DailyLogEntity)

    @Query("DELETE FROM daily_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)
}
