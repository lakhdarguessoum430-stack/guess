package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.DailyLogEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.TaskEntity
import com.example.data.remote.ApiClient
import com.example.data.remote.ProjectDto
import com.example.data.repository.ConstructionRepository
import com.example.data.repository.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConstructionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ConstructionRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = ConstructionRepository(
            projectDao = db.projectDao(),
            expenseDao = db.expenseDao(),
            taskDao = db.taskDao(),
            dailyLogDao = db.dailyLogDao()
        )
    }

    // Navigation and screen state
    val currentScreen = MutableStateFlow<Screen>(Screen.Home)

    // Filter and search state
    val searchQuery = MutableStateFlow("")
    val selectedStatusFilter = MutableStateFlow("الكل") // "الكل", "جارية", "مكتملة", "متوقفة"

    // Raw projects list
    val allProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered projects
    val filteredProjects: StateFlow<List<ProjectEntity>> = combine(
        allProjects,
        searchQuery,
        selectedStatusFilter
    ) { projects, query, statusFilter ->
        projects.filter { project ->
            val matchesQuery = query.isBlank() ||
                    project.name.contains(query, ignoreCase = true) ||
                    project.location.contains(query, ignoreCase = true) ||
                    project.supervisorName.contains(query, ignoreCase = true)

            val matchesStatus = statusFilter == "الكل" || project.status == statusFilter

            matchesQuery && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All expenses
    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently selected project for detail view
    val selectedProjectId = MutableStateFlow<Long?>(null)

    val selectedProject: StateFlow<ProjectEntity?> = selectedProjectId.flatMapLatest { id ->
        if (id != null) repository.getProjectById(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentProjectExpenses: StateFlow<List<ExpenseEntity>> = selectedProjectId.flatMapLatest { id ->
        if (id != null) repository.getExpensesForProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentProjectTasks: StateFlow<List<TaskEntity>> = selectedProjectId.flatMapLatest { id ->
        if (id != null) repository.getTasksForProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentProjectLogs: StateFlow<List<DailyLogEntity>> = selectedProjectId.flatMapLatest { id ->
        if (id != null) repository.getLogsForProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dialog state
    val showAddEditProjectDialog = MutableStateFlow(false)
    val editingProject = MutableStateFlow<ProjectEntity?>(null)

    val showAddExpenseDialog = MutableStateFlow(false)
    val showAddTaskDialog = MutableStateFlow(false)
    val showAddDailyLogDialog = MutableStateFlow(false)
    val showServerSyncDialog = MutableStateFlow(false)

    // Server configuration
    val serverUrl = MutableStateFlow(ApiClient.DEFAULT_BASE_URL)
    val isSyncing = MutableStateFlow(false)
    val syncMessage = MutableStateFlow<String?>(null)

    fun navigateToDetail(projectId: Long) {
        selectedProjectId.value = projectId
        currentScreen.value = Screen.ProjectDetail
    }

    fun navigateToHome() {
        currentScreen.value = Screen.Home
    }

    fun navigateToAnalytics() {
        currentScreen.value = Screen.Analytics
    }

    fun openNewProjectDialog() {
        editingProject.value = null
        showAddEditProjectDialog.value = true
    }

    fun openEditProjectDialog(project: ProjectEntity) {
        editingProject.value = project
        showAddEditProjectDialog.value = true
    }

    fun closeProjectDialog() {
        showAddEditProjectDialog.value = false
        editingProject.value = null
    }

    fun saveProject(
        name: String,
        location: String,
        budget: Double,
        status: String,
        clientName: String,
        supervisorName: String,
        notes: String
    ) {
        viewModelScope.launch {
            val current = editingProject.value
            if (current == null) {
                // Insert new
                val newProject = ProjectEntity(
                    name = name.trim(),
                    location = location.trim(),
                    budget = budget,
                    status = status,
                    clientName = clientName.trim(),
                    supervisorName = supervisorName.trim(),
                    notes = notes.trim(),
                    startDate = System.currentTimeMillis()
                )
                repository.insertProject(newProject)
            } else {
                // Update
                val updated = current.copy(
                    name = name.trim(),
                    location = location.trim(),
                    budget = budget,
                    status = status,
                    clientName = clientName.trim(),
                    supervisorName = supervisorName.trim(),
                    notes = notes.trim()
                )
                repository.updateProject(updated)
            }
            closeProjectDialog()
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project)
            if (selectedProjectId.value == project.id) {
                navigateToHome()
            }
        }
    }

    fun updateProjectStatus(project: ProjectEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateProject(project.copy(status = newStatus))
        }
    }

    // Expense actions
    fun addExpense(projectId: Long, title: String, amount: Double, category: String, notes: String) {
        viewModelScope.launch {
            repository.insertExpense(
                ExpenseEntity(
                    projectId = projectId,
                    title = title.trim(),
                    amount = amount,
                    category = category,
                    notes = notes.trim(),
                    date = System.currentTimeMillis()
                )
            )
            showAddExpenseDialog.value = false
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // Task actions
    fun addTask(projectId: Long, title: String, phase: String) {
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    projectId = projectId,
                    title = title.trim(),
                    milestonePhase = phase.trim().ifEmpty { "المرحلة الأولى" }
                )
            )
            showAddTaskDialog.value = false
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Daily log actions
    fun addDailyLog(projectId: Long, workersCount: Int, weather: String, workSummary: String, supervisorNote: String) {
        viewModelScope.launch {
            repository.insertDailyLog(
                DailyLogEntity(
                    projectId = projectId,
                    workersCount = workersCount,
                    weather = weather.trim(),
                    workSummary = workSummary.trim(),
                    supervisorNote = supervisorNote.trim(),
                    date = System.currentTimeMillis()
                )
            )
            showAddDailyLogDialog.value = false
        }
    }

    fun deleteDailyLog(log: DailyLogEntity) {
        viewModelScope.launch {
            repository.deleteDailyLog(log)
        }
    }

    // Sync action with Express backend
    fun syncWithBackend(url: String) {
        serverUrl.value = url
        viewModelScope.launch {
            isSyncing.value = true
            syncMessage.value = null
            when (val result = repository.syncWithServer(url)) {
                is SyncResult.Success -> {
                    syncMessage.value = "${result.message} (تم جلب ${result.fetchedCount} مشاريع جديدة)"
                }
                is SyncResult.Error -> {
                    syncMessage.value = result.message
                }
            }
            isSyncing.value = false
        }
    }

    fun clearSyncMessage() {
        syncMessage.value = null
    }
}

sealed class Screen {
    object Home : Screen()
    object ProjectDetail : Screen()
    object Analytics : Screen()
}
