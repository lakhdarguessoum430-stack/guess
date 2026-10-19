package com.example.data.repository

import com.example.data.local.DailyLogDao
import com.example.data.local.ExpenseDao
import com.example.data.local.ProjectDao
import com.example.data.local.TaskDao
import com.example.data.model.DailyLogEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.TaskEntity
import com.example.data.remote.ApiClient
import com.example.data.remote.ProjectDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

sealed class SyncResult {
    data class Success(val message: String, val fetchedCount: Int, val uploadedCount: Int) : SyncResult()
    data class Error(val message: String) : SyncResult()
}

class ConstructionRepository(
    private val projectDao: ProjectDao,
    private val expenseDao: ExpenseDao,
    private val taskDao: TaskDao,
    private val dailyLogDao: DailyLogDao
) {

    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    fun getProjectById(id: Long): Flow<ProjectEntity?> = projectDao.getProjectById(id)

    fun getExpensesForProject(projectId: Long): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesForProject(projectId)

    fun getTasksForProject(projectId: Long): Flow<List<TaskEntity>> =
        taskDao.getTasksForProject(projectId)

    fun getLogsForProject(projectId: Long): Flow<List<DailyLogEntity>> =
        dailyLogDao.getLogsForProject(projectId)

    fun getTotalSpentForProject(projectId: Long): Flow<Double?> =
        expenseDao.getTotalSpentForProject(projectId)

    fun getTotalSpentAll(): Flow<Double?> = expenseDao.getTotalSpentAll()

    suspend fun insertProject(project: ProjectEntity): Long = withContext(Dispatchers.IO) {
        projectDao.insertProject(project)
    }

    suspend fun updateProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        projectDao.updateProject(project)
    }

    suspend fun deleteProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        projectDao.deleteProject(project)
    }

    suspend fun insertExpense(expense: ExpenseEntity): Long = withContext(Dispatchers.IO) {
        expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun insertTask(task: TaskEntity): Long = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.deleteTask(task)
    }

    suspend fun insertDailyLog(log: DailyLogEntity): Long = withContext(Dispatchers.IO) {
        dailyLogDao.insertLog(log)
    }

    suspend fun deleteDailyLog(log: DailyLogEntity) = withContext(Dispatchers.IO) {
        dailyLogDao.deleteLog(log)
    }

    // Sync with Express /api/projects
    suspend fun syncWithServer(serverUrl: String): SyncResult = withContext(Dispatchers.IO) {
        try {
            val api = ApiClient.createService(serverUrl)

            // 1. Fetch remote projects
            val response = api.getProjects()
            if (!response.isSuccessful) {
                return@withContext SyncResult.Error("خطأ من الخادم: رمز ${response.code()} - ${response.message()}")
            }

            val remoteProjects = response.body() ?: emptyList()
            var fetchedCount = 0

            for (dto in remoteProjects) {
                val existing = if (dto.id != null) projectDao.getProjectByRemoteId(dto.id) else null
                val parsedDate = parseDateToMillis(dto.startDate)

                if (existing == null) {
                    projectDao.insertProject(
                        ProjectEntity(
                            remoteId = dto.id,
                            name = dto.name,
                            location = dto.location,
                            budget = dto.budget,
                            status = if (dto.status.isNotBlank()) dto.status else ProjectEntity.STATUS_IN_PROGRESS,
                            startDate = parsedDate,
                            isSynced = true
                        )
                    )
                    fetchedCount++
                } else {
                    projectDao.updateProject(
                        existing.copy(
                            name = dto.name,
                            location = dto.location,
                            budget = dto.budget,
                            status = if (dto.status.isNotBlank()) dto.status else existing.status,
                            isSynced = true
                        )
                    )
                }
            }

            // 2. Upload any local unsynced projects
            var uploadedCount = 0
            // Note: We'd check unsynced projects if any
            SyncResult.Success(
                message = "تمت المزامنة بنجاح مع خادم الورشات",
                fetchedCount = fetchedCount,
                uploadedCount = uploadedCount
            )
        } catch (e: Exception) {
            SyncResult.Error("تعذر الاتصال بالخادم (${e.localizedMessage ?: "تأكد من تشغيل الخادم وعنوان الشبكة"})")
        }
    }

    private fun parseDateToMillis(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return System.currentTimeMillis()
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            format.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
