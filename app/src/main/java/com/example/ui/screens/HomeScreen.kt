package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ProjectEntity
import com.example.ui.ConstructionViewModel
import com.example.ui.components.AddEditProjectDialog
import com.example.ui.components.ProjectCard
import com.example.ui.components.ServerSyncDialog
import com.example.ui.components.formatCurrency
import com.example.ui.theme.ConstructionAmber
import com.example.ui.theme.ConstructionAmberDark
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusCompletedBlue
import com.example.ui.theme.StatusPausedAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ConstructionViewModel,
    modifier: Modifier = Modifier
) {
    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    val filteredProjects by viewModel.filteredProjects.collectAsStateWithLifecycle()
    val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()

    val showProjectDialog by viewModel.showAddEditProjectDialog.collectAsStateWithLifecycle()
    val editingProject by viewModel.editingProject.collectAsStateWithLifecycle()
    val showSyncDialog by viewModel.showServerSyncDialog.collectAsStateWithLifecycle()
    val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncMessage by viewModel.syncMessage.collectAsStateWithLifecycle()

    // Aggregate statistics
    val totalBudget = allProjects.sumOf { it.budget }
    val totalSpent = allExpenses.sumOf { it.amount }
    val activeCount = allProjects.count { it.status == ProjectEntity.STATUS_IN_PROGRESS }
    val completedCount = allProjects.count { it.status == ProjectEntity.STATUS_COMPLETED }
    val pausedCount = allProjects.count { it.status == ProjectEntity.STATUS_PAUSED }

    // Map project ID to spent amount
    val spentByProjectId = allExpenses.groupBy { it.projectId }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "GUESSOUM AREZKI",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Slate900,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "إدارة ورشات ومشاريع البناء",
                            style = MaterialTheme.typography.labelSmall,
                            color = ConstructionAmberDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.showServerSyncDialog.value = true },
                        modifier = Modifier.testTag("btn_open_sync")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "مزامنة الخادم",
                            tint = ConstructionAmberDark
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateToAnalytics() },
                        modifier = Modifier.testTag("btn_open_analytics")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "التحليلات والميزانية",
                            tint = Slate800
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openNewProjectDialog() },
                containerColor = ConstructionAmber,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("ورشة جديدة", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("btn_add_project_fab")
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 700.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Dashboard Overview Metrics Card
                item {
                    DashboardStatsCard(
                        totalBudget = totalBudget,
                        totalSpent = totalSpent,
                        activeCount = activeCount,
                        completedCount = completedCount,
                        pausedCount = pausedCount
                    )
                }

                // 2. Search & Filter Bar
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.searchQuery.value = it },
                            placeholder = { Text("بحث عن ورشة، موقع، أو مشرف...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "بحث",
                                    tint = Slate600
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "مسح",
                                            tint = Slate600
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_projects_input")
                        )

                        // Status Filter Chips
                        val filterOptions = listOf(
                            Triple("الكل", allProjects.size, ConstructionAmber),
                            Triple(ProjectEntity.STATUS_IN_PROGRESS, activeCount, StatusActiveGreen),
                            Triple(ProjectEntity.STATUS_COMPLETED, completedCount, StatusCompletedBlue),
                            Triple(ProjectEntity.STATUS_PAUSED, pausedCount, StatusPausedAmber)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(filterOptions) { (statusText, count, color) ->
                                val isSelected = selectedStatusFilter == statusText
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectedStatusFilter.value = statusText },
                                    label = {
                                        Text(
                                            text = "$statusText ($count)",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = color.copy(alpha = 0.15f),
                                        selectedLabelColor = color
                                    ),
                                    modifier = Modifier.testTag("filter_chip_$statusText")
                                )
                            }
                        }
                    }
                }

                // 3. Projects List Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "قائمة الورشات (${filteredProjects.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                    }
                }

                // 4. Projects Cards or Empty State
                if (filteredProjects.isEmpty()) {
                    item {
                        EmptyProjectsState(
                            hasSearch = searchQuery.isNotBlank() || selectedStatusFilter != "الكل",
                            onAddNew = { viewModel.openNewProjectDialog() },
                            onResetFilter = {
                                viewModel.searchQuery.value = ""
                                viewModel.selectedStatusFilter.value = "الكل"
                            }
                        )
                    }
                } else {
                    items(filteredProjects, key = { it.id }) { project ->
                        val spent = spentByProjectId[project.id] ?: 0.0
                        ProjectCard(
                            project = project,
                            totalSpent = spent,
                            onClick = { viewModel.navigateToDetail(project.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(64.dp))
                }
            }
        }
    }

    // Add/Edit Project Dialog
    if (showProjectDialog) {
        AddEditProjectDialog(
            initialProject = editingProject,
            onDismiss = { viewModel.closeProjectDialog() },
            onSave = { name, location, budget, status, clientName, supervisorName, notes ->
                viewModel.saveProject(name, location, budget, status, clientName, supervisorName, notes)
            }
        )
    }

    // Express Server Sync Dialog
    if (showSyncDialog) {
        ServerSyncDialog(
            initialUrl = serverUrl,
            isSyncing = isSyncing,
            syncMessage = syncMessage,
            onDismiss = { viewModel.showServerSyncDialog.value = false },
            onSync = { url -> viewModel.syncWithBackend(url) },
            onClearMessage = { viewModel.clearSyncMessage() }
        )
    }
}

@Composable
fun DashboardStatsCard(
    totalBudget: Double,
    totalSpent: Double,
    activeCount: Int,
    completedCount: Int,
    pausedCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Construction,
                        contentDescription = null,
                        tint = ConstructionAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "ملخص الورشات والميزانيات",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate800
                ) {
                    Text(
                        text = "${activeCount + completedCount + pausedCount} ورشات",
                        color = ConstructionAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Two main financial numbers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = Slate800
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "إجمالي الميزانيات",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate200
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(totalBudget),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = Slate800
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "إجمالي المصروفات",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate200
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(totalSpent),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ConstructionAmber
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status counts breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCounterItem(
                    title = "جارية",
                    count = activeCount,
                    color = StatusActiveGreen
                )
                StatCounterItem(
                    title = "مكتملة",
                    count = completedCount,
                    color = StatusCompletedBlue
                )
                StatCounterItem(
                    title = "متوقفة",
                    count = pausedCount,
                    color = StatusPausedAmber
                )
            }
        }
    }
}

@Composable
fun StatCounterItem(
    title: String,
    count: Int,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, shape = RoundedCornerShape(3.dp))
        )
        Text(
            text = "$title: $count",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
fun EmptyProjectsState(
    hasSearch: Boolean,
    onAddNew: () -> Unit,
    onResetFilter: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Engineering,
                contentDescription = null,
                tint = ConstructionAmber,
                modifier = Modifier.size(56.dp)
            )

            Text(
                text = if (hasSearch) "لا توجد ورشات مطابقة للبحث أو الفلتر" else "لا توجد ورشات بناء مسجلة حالياً",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate800
            )

            Text(
                text = if (hasSearch) "جرب تغيير كلمات البحث أو إعادة تعيين الفلتر." else "ابدأ بتسجيل أول ورشة عمل لتتبع الميزانية والمراحل والمصروفات.",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate600
            )

            if (hasSearch) {
                androidx.compose.material3.OutlinedButton(
                    onClick = onResetFilter,
                    modifier = Modifier.testTag("btn_reset_filter")
                ) {
                    Text("إعادة ضبط الفلتر")
                }
            }
        }
    }
}
