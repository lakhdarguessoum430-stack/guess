package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExpenseEntity
import com.example.data.model.ProjectEntity
import com.example.ui.ConstructionViewModel
import com.example.ui.components.formatCurrency
import com.example.ui.theme.BlueprintCyan
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
fun AnalyticsScreen(
    viewModel: ConstructionViewModel,
    modifier: Modifier = Modifier
) {
    // Mandated BackHandler for navigation
    BackHandler {
        viewModel.navigateToHome()
    }

    val context = LocalContext.current
    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()

    val totalBudget = allProjects.sumOf { it.budget }
    val totalSpent = allExpenses.sumOf { it.amount }
    val netRemaining = totalBudget - totalSpent
    val utilization = if (totalBudget > 0) ((totalSpent / totalBudget) * 100).toInt() else 0

    // Expenses grouped by category
    val expensesByCategory = allExpenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    val activeProjects = allProjects.filter { it.status == ProjectEntity.STATUS_IN_PROGRESS }
    val completedProjects = allProjects.filter { it.status == ProjectEntity.STATUS_COMPLETED }
    val pausedProjects = allProjects.filter { it.status == ProjectEntity.STATUS_PAUSED }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "التحليلات والميزانية الإجمالية",
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateToHome() },
                        modifier = Modifier.testTag("btn_analytics_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "العودة",
                            tint = Slate900
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Overall Financial Health Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
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
                                        imageVector = Icons.Default.BarChart,
                                        contentDescription = null,
                                        tint = ConstructionAmber,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "الميزانية الكلية لجميع المشاريع",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Surface(
                                    color = Slate800,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "$utilization% مستهلك",
                                        color = ConstructionAmber,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("الميزانيات المرصودة", color = Slate200, fontSize = 12.sp)
                                    Text(formatCurrency(totalBudget), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Column {
                                    Text("إجمالي المصاريف", color = Slate200, fontSize = 12.sp)
                                    Text(formatCurrency(totalSpent), color = ConstructionAmber, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Column {
                                    Text("المتبقي الصافي", color = Slate200, fontSize = 12.sp)
                                    Text(
                                        formatCurrency(netRemaining),
                                        color = if (netRemaining >= 0) StatusActiveGreen else Color(0xFFDC2626),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            LinearProgressIndicator(
                                progress = { if (totalBudget > 0) (totalSpent / totalBudget).coerceIn(0.0, 1.0).toFloat() else 0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = ConstructionAmber,
                                trackColor = Slate800
                            )
                        }
                    }
                }

                // 2. Breakdown by Expense Category
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = BlueprintCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "توزيع التكاليف حسب البنود",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate800
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (totalSpent == 0.0) {
                                Text(
                                    text = "لا توجد مصروفات مسجلة حتى الآن لحساب التوزيع.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600
                                )
                            } else {
                                ExpenseEntity.ALL_CATEGORIES.forEach { category ->
                                    val catAmount = expensesByCategory[category] ?: 0.0
                                    val catPercent = if (totalSpent > 0) (catAmount / totalSpent).toFloat() else 0f
                                    val catPercentInt = (catPercent * 100).toInt()

                                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "$category ($catPercentInt%)",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = Slate800
                                            )
                                            Text(
                                                text = formatCurrency(catAmount),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = ConstructionAmberDark
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { catPercent },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = when (category) {
                                                ExpenseEntity.CAT_MATERIALS -> ConstructionAmberDark
                                                ExpenseEntity.CAT_LABOR -> BlueprintCyan
                                                ExpenseEntity.CAT_EQUIPMENT -> StatusActiveGreen
                                                else -> Slate600
                                            },
                                            trackColor = Slate200
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Status Breakdown Cards
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PieChart,
                                    contentDescription = null,
                                    tint = StatusActiveGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "حالة الورشات والمشاريع",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate800
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatusSummaryBox(
                                    title = "جارية",
                                    count = activeProjects.size,
                                    budget = activeProjects.sumOf { it.budget },
                                    color = StatusActiveGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                StatusSummaryBox(
                                    title = "مكتملة",
                                    count = completedProjects.size,
                                    budget = completedProjects.sumOf { it.budget },
                                    color = StatusCompletedBlue,
                                    modifier = Modifier.weight(1f)
                                )
                                StatusSummaryBox(
                                    title = "متوقفة",
                                    count = pausedProjects.size,
                                    budget = pausedProjects.sumOf { it.budget },
                                    color = StatusPausedAmber,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // 4. Export & Copy Report Summary
                item {
                    Button(
                        onClick = {
                            val summaryText = buildString {
                                appendLine("📋 تقرير ورشات ومشاريع البناء - GUESSOUM AREZKI")
                                appendLine("----------------------------------------")
                                appendLine("إجمالي عدد الورشات: ${allProjects.size}")
                                appendLine("الورشات الجارية: ${activeProjects.size} | المكتملة: ${completedProjects.size} | المتوقفة: ${pausedProjects.size}")
                                appendLine("إجمالي الميزانية المرصودة: ${formatCurrency(totalBudget)}")
                                appendLine("إجمالي المصروفات: ${formatCurrency(totalSpent)}")
                                appendLine("المتبقي الصافي: ${formatCurrency(netRemaining)}")
                                appendLine("نسبة استهلاك الميزانية: $utilization%")
                                appendLine("----------------------------------------")
                                allProjects.forEach { p ->
                                    appendLine("• ${p.name} (${p.location}) - الحالة: ${p.status} - ميزانية: ${formatCurrency(p.budget)}")
                                }
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("تقرير الورشات", summaryText))
                            Toast.makeText(context, "تم نسخ ملخص التقرير بنجاح", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_copy_analytics_report"),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("نسخ التقرير المالي والإحصائي", fontWeight = FontWeight.Bold)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun StatusSummaryBox(
    title: String,
    count: Int,
    budget: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Slate100
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, shape = RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 12.sp, color = Slate700, fontWeight = FontWeight.Medium)
            Text("$count ورشات", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate800)
            Spacer(modifier = Modifier.height(2.dp))
            Text(formatCurrency(budget), fontSize = 10.sp, color = Slate600)
        }
    }
}
