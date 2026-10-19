package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectEntity
import com.example.ui.theme.ConstructionAmber
import com.example.ui.theme.ConstructionAmberDark
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusActiveGreenBg
import com.example.ui.theme.StatusCompletedBlue
import com.example.ui.theme.StatusCompletedBlueBg
import com.example.ui.theme.StatusPausedAmber
import com.example.ui.theme.StatusPausedAmberBg
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.FRENCH)
    formatter.maximumFractionDigits = 0
    return "${formatter.format(amount)} د.ج"
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    return sdf.format(Date(timestamp))
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (status) {
        ProjectEntity.STATUS_IN_PROGRESS -> Triple(
            StatusActiveGreenBg,
            StatusActiveGreen,
            Icons.Default.PlayCircle
        )
        ProjectEntity.STATUS_COMPLETED -> Triple(
            StatusCompletedBlueBg,
            StatusCompletedBlue,
            Icons.Default.CheckCircle
        )
        ProjectEntity.STATUS_PAUSED -> Triple(
            StatusPausedAmberBg,
            StatusPausedAmber,
            Icons.Default.PauseCircle
        )
        else -> Triple(
            Slate100,
            Slate700,
            Icons.Default.Business
        )
    }

    Surface(
        modifier = modifier.testTag("status_badge_${status}"),
        shape = RoundedCornerShape(20.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = status,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BudgetProgressBar(
    budget: Double,
    spent: Double,
    modifier: Modifier = Modifier
) {
    val progress = if (budget > 0) (spent / budget).coerceIn(0.0, 1.0).toFloat() else 0f
    val percentInt = if (budget > 0) ((spent / budget) * 100).toInt() else 0

    val barColor = when {
        percentInt > 100 -> Color(0xFFDC2626) // Red over budget
        percentInt > 80 -> ConstructionAmber // Warning yellow
        else -> StatusActiveGreen // Safe green
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "استهلاك الميزانية: $percentInt%",
                style = MaterialTheme.typography.bodySmall,
                color = Slate700,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${formatCurrency(spent)} / ${formatCurrency(budget)}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate800,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = Slate200
        )
    }
}

@Composable
fun ProjectCard(
    project: ProjectEntity,
    totalSpent: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("project_card_${project.id}")
            .clickable(onClick = onClick)
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate800
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "الموقع",
                            tint = ConstructionAmberDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = project.location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate600
                        )
                    }
                }
                StatusBadge(status = project.status)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Budget Progress
            BudgetProgressBar(
                budget = project.budget,
                spent = totalSpent
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "تاريخ البدء",
                        tint = Slate600,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "البدء: ${formatDate(project.startDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                }

                if (project.supervisorName.isNotBlank()) {
                    Text(
                        text = "إشراف: ${project.supervisorName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
