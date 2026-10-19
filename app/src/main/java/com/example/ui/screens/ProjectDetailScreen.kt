package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DailyLogEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.TaskEntity
import com.example.ui.ConstructionViewModel
import com.example.ui.components.AddDailyLogDialog
import com.example.ui.components.AddEditProjectDialog
import com.example.ui.components.AddExpenseDialog
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.BudgetProgressBar
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.theme.ConstructionAmber
import com.example.ui.theme.ConstructionAmberDark
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusActiveGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    viewModel: ConstructionViewModel,
    modifier: Modifier = Modifier
) {
    // Mandated BackHandler for custom state navigation
    BackHandler {
        viewModel.navigateToHome()
    }

    val project by viewModel.selectedProject.collectAsStateWithLifecycle()
    val expenses by viewModel.currentProjectExpenses.collectAsStateWithLifecycle()
    val tasks by viewModel.currentProjectTasks.collectAsStateWithLifecycle()
    val logs by viewModel.currentProjectLogs.collectAsStateWithLifecycle()

    val showEditProjectDialog by viewModel.showAddEditProjectDialog.collectAsStateWithLifecycle()
    val showAddExpenseDialog by viewModel.showAddExpenseDialog.collectAsStateWithLifecycle()
    val showAddTaskDialog by viewModel.showAddTaskDialog.collectAsStateWithLifecycle()
    val showAddDailyLogDialog by viewModel.showAddDailyLogDialog.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (project == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("جاري تحميل بيانات الورشة...", color = Slate700)
        }
        return
    }

    val currentProj = project!!
    val totalSpent = expenses.sumOf { it.amount }
    val remainingBudget = currentProj.budget - totalSpent
    val completedTasksCount = tasks.count { it.isCompleted }
    val taskProgress = if (tasks.isNotEmpty()) completedTasksCount.toFloat() / tasks.size else 0f

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentProj.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        color = Slate900
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateToHome() },
                        modifier = Modifier.testTag("btn_back_to_home")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "العودة للرئيسية",
                            tint = Slate900
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openEditProjectDialog(currentProj) },
                        modifier = Modifier.testTag("btn_edit_project")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل الورشة",
                            tint = Slate800
                        )
                    }
                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.testTag("btn_delete_project")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف الورشة",
                            tint = Color(0xFFDC2626)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate100),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 700.dp)
            ) {
                // Project Overview Header Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = ConstructionAmberDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = currentProj.location,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate700
                                )
                            }
                            StatusBadge(status = currentProj.status)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status quick switch chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تغيير الحالة:",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                            ProjectEntity.ALL_STATUSES.forEach { st ->
                                FilterChip(
                                    selected = currentProj.status == st,
                                    onClick = { viewModel.updateProjectStatus(currentProj, st) },
                                    label = { Text(st, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("quick_status_$st")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Budget Progress
                        BudgetProgressBar(
                            budget = currentProj.budget,
                            spent = totalSpent
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "المتبقي: ${formatCurrency(remainingBudget)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (remainingBudget >= 0) StatusActiveGreen else Color(0xFFDC2626)
                            )
                            Text(
                                text = "تاريخ البدء: ${formatDate(currentProj.startDate)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                        }

                        if (currentProj.supervisorName.isNotBlank() || currentProj.clientName.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (currentProj.clientName.isNotBlank()) {
                                    Text(
                                        text = "الزبون: ${currentProj.clientName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate600
                                    )
                                }
                                if (currentProj.supervisorName.isNotBlank()) {
                                    Text(
                                        text = "المشرف: ${currentProj.supervisorName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate700,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Tabs: Expenses, Tasks, Site Logs
                SecondaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = ConstructionAmberDark
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("المصروفات (${expenses.size})", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Paid, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_expenses")
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("المراحل (${completedTasksCount}/${tasks.size})", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_tasks")
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = { Text("يوميات الورشة (${logs.size})", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_logs")
                    )
                }

                // Tab Content
                when (selectedTabIndex) {
                    0 -> ExpensesTabContent(
                        expenses = expenses,
                        onAddExpense = { viewModel.showAddExpenseDialog.value = true },
                        onDeleteExpense = { viewModel.deleteExpense(it) }
                    )
                    1 -> TasksTabContent(
                        tasks = tasks,
                        completedCount = completedTasksCount,
                        progress = taskProgress,
                        onAddTask = { viewModel.showAddTaskDialog.value = true },
                        onToggleTask = { viewModel.toggleTask(it) },
                        onDeleteTask = { viewModel.deleteTask(it) }
                    )
                    2 -> DailyLogsTabContent(
                        logs = logs,
                        onAddLog = { viewModel.showAddDailyLogDialog.value = true },
                        onDeleteLog = { viewModel.deleteDailyLog(it) }
                    )
                }
            }
        }
    }

    // Edit Project Dialog
    if (showEditProjectDialog) {
        AddEditProjectDialog(
            initialProject = currentProj,
            onDismiss = { viewModel.closeProjectDialog() },
            onSave = { name, location, budget, status, clientName, supervisorName, notes ->
                viewModel.saveProject(name, location, budget, status, clientName, supervisorName, notes)
            }
        )
    }

    // Add Expense Dialog
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { viewModel.showAddExpenseDialog.value = false },
            onSave = { title, amount, category, notes ->
                viewModel.addExpense(currentProj.id, title, amount, category, notes)
            }
        )
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { viewModel.showAddTaskDialog.value = false },
            onSave = { title, phase ->
                viewModel.addTask(currentProj.id, title, phase)
            }
        )
    }

    // Add Daily Log Dialog
    if (showAddDailyLogDialog) {
        AddDailyLogDialog(
            onDismiss = { viewModel.showAddDailyLogDialog.value = false },
            onSave = { workersCount, weather, workSummary, supervisorNote ->
                viewModel.addDailyLog(currentProj.id, workersCount, weather, workSummary, supervisorNote)
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("حذف الورشة", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من رغبتك في حذف ورشة '${currentProj.name}'؟ سيتم حذف جميع المصروفات والمهام المرتبطة بها.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteProject(currentProj)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("btn_confirm_delete_project")
                ) {
                    Text("نعم، حذف نهائي")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun ExpensesTabContent(
    expenses: List<ExpenseEntity>,
    onAddExpense: () -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Button(
                onClick = onAddExpense,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_open_add_expense"),
                colors = ButtonDefaults.buttonColors(containerColor = ConstructionAmber)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("تسجيل مصروف / دفعة جديدة", fontWeight = FontWeight.Bold)
            }
        }

        if (expenses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "لا توجد مصروفات مسجلة لهذه الورشة حتى الآن.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate600
                        )
                    }
                }
            }
        } else {
            items(expenses, key = { it.id }) { expense ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_item_${expense.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = expense.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate800
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Slate100,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = expense.category,
                                        fontSize = 11.sp,
                                        color = Slate700,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = formatDate(expense.date),
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                            }
                            if (expense.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = expense.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatCurrency(expense.amount),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ConstructionAmberDark
                            )
                            IconButton(onClick = { onDeleteExpense(expense) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف المصروف",
                                    tint = Slate600,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TasksTabContent(
    tasks: List<TaskEntity>,
    completedCount: Int,
    progress: Float,
    onAddTask: () -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "نسبة إنجاز مراحل الورشة",
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                        Text(
                            text = "${(progress * 100).toInt()}% ($completedCount من ${tasks.size})",
                            fontWeight = FontWeight.Bold,
                            color = StatusActiveGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = StatusActiveGreen,
                        trackColor = Slate200
                    )
                }
            }
        }

        item {
            Button(
                onClick = onAddTask,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_open_add_task"),
                colors = ButtonDefaults.buttonColors(containerColor = ConstructionAmber)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("إضافة مرحلة / مهمة عمل", fontWeight = FontWeight.Bold)
            }
        }

        if (tasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "لم يتم تحديد مراحل أو مهام للورشة بعد.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate600
                        )
                    }
                }
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_item_${task.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (task.isCompleted) Color(0xFFF0FDF4) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { onToggleTask(task) },
                            colors = CheckboxDefaults.colors(checkedColor = StatusActiveGreen),
                            modifier = Modifier.testTag("task_checkbox_${task.id}")
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                                color = if (task.isCompleted) Slate600 else Slate800,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
                            Surface(
                                color = Slate100,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = task.milestonePhase,
                                    fontSize = 10.sp,
                                    color = Slate700,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        IconButton(onClick = { onDeleteTask(task) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف المهمة",
                                tint = Slate600,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyLogsTabContent(
    logs: List<DailyLogEntity>,
    onAddLog: () -> Unit,
    onDeleteLog: (DailyLogEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Button(
                onClick = onAddLog,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_open_add_log"),
                colors = ButtonDefaults.buttonColors(containerColor = ConstructionAmber)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("تسجيل يومية الورشة وملاحظات الميدان", fontWeight = FontWeight.Bold)
            }
        }

        if (logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "لا توجد يوميات مسجلة للموقع حتى الآن.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate600
                        )
                    }
                }
            }
        } else {
            items(logs, key = { it.id }) { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("log_item_${log.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "يومية: ${formatDate(log.date)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate800
                            )
                            IconButton(onClick = { onDeleteLog(log) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف اليومية",
                                    tint = Slate600,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = Slate600,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${log.workersCount} عامل",
                                    fontSize = 12.sp,
                                    color = Slate700
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = ConstructionAmberDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = log.weather,
                                    fontSize = 12.sp,
                                    color = Slate700
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = log.workSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate800
                        )

                        if (log.supervisorNote.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = Slate100,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "توجيهات المشرف: ${log.supervisorNote}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate700,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
