package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseEntity
import com.example.data.model.ProjectEntity
import com.example.ui.theme.ConstructionAmber
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800

@Composable
fun AddEditProjectDialog(
    initialProject: ProjectEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, location: String, budget: Double, status: String, clientName: String, supervisorName: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialProject?.name ?: "") }
    var location by remember { mutableStateOf(initialProject?.location ?: "") }
    var budgetText by remember { mutableStateOf(if (initialProject != null) initialProject.budget.toLong().toString() else "") }
    var status by remember { mutableStateOf(initialProject?.status ?: ProjectEntity.STATUS_IN_PROGRESS) }
    var clientName by remember { mutableStateOf(initialProject?.clientName ?: "") }
    var supervisorName by remember { mutableStateOf(initialProject?.supervisorName ?: "") }
    var notes by remember { mutableStateOf(initialProject?.notes ?: "") }

    var nameError by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf(false) }
    var budgetError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialProject == null) "إضافة ورشة جديدة" else "تعديل بيانات الورشة",
                fontWeight = FontWeight.Bold,
                color = Slate800
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("اسم المشروع / الورشة *") },
                    isError = nameError,
                    supportingText = if (nameError) { { Text("يرجى إدخال اسم الورشة") } } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_project_name")
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = {
                        location = it
                        locationError = it.isBlank()
                    },
                    label = { Text("الموقع الجغرافي للورشة *") },
                    isError = locationError,
                    supportingText = if (locationError) { { Text("يرجى إدخال موقع الورشة") } } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_project_location")
                )

                OutlinedTextField(
                    value = budgetText,
                    onValueChange = {
                        budgetText = it.filter { char -> char.isDigit() || char == '.' }
                        budgetError = budgetText.toDoubleOrNull() == null || budgetText.toDouble() <= 0
                    },
                    label = { Text("الميزانية المرصودة (د.ج) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = budgetError,
                    supportingText = if (budgetError) { { Text("يرجى إدخال قيمة صحيحة للميزانية") } } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_project_budget")
                )

                Text(
                    text = "حالة الورشة:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate700
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProjectEntity.ALL_STATUSES.forEach { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = { Text(s) },
                            modifier = Modifier.testTag("chip_status_$s")
                        )
                    }
                }

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("صاحب المشروع / جهة التكليف") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = supervisorName,
                    onValueChange = { supervisorName = it },
                    label = { Text("رئيس الورشة / المهندس المشرف") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validName = name.isNotBlank()
                    val validLocation = location.isNotBlank()
                    val budgetVal = budgetText.toDoubleOrNull()
                    val validBudget = budgetVal != null && budgetVal > 0

                    nameError = !validName
                    locationError = !validLocation
                    budgetError = !validBudget

                    if (validName && validLocation && validBudget) {
                        onSave(name, location, budgetVal!!, status, clientName, supervisorName, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ConstructionAmber),
                modifier = Modifier.testTag("btn_save_project")
            ) {
                Text("حفظ الورشة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, category: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseEntity.CAT_MATERIALS) }
    var notes by remember { mutableStateOf("") }

    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تسجيل مصروف / دفعة مالية",
                fontWeight = FontWeight.Bold,
                color = Slate800
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = it.isBlank()
                    },
                    label = { Text("بيان المصروف (مثال: شراء حديد تسليح) *") },
                    isError = titleError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expense_title")
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { c -> c.isDigit() || c == '.' }
                        amountError = amountText.toDoubleOrNull() == null || amountText.toDouble() <= 0
                    },
                    label = { Text("المبلغ (د.ج) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = amountError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expense_amount")
                )

                Text(
                    text = "تصنيف المصروف:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate700
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExpenseEntity.ALL_CATEGORIES.chunked(2).forEach { rowCats ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowCats.forEach { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = { category = cat },
                                    label = { Text(cat, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("تفاصيل / رقم الفاتورة أو المورد") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validTitle = title.isNotBlank()
                    val amountVal = amountText.toDoubleOrNull()
                    val validAmount = amountVal != null && amountVal > 0

                    titleError = !validTitle
                    amountError = !validAmount

                    if (validTitle && validAmount) {
                        onSave(title, amountVal!!, category, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ConstructionAmber),
                modifier = Modifier.testTag("btn_save_expense")
            ) {
                Text("تسجيل المصروف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, phase: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var phase by remember { mutableStateOf("الهيكل الخرساني") }
    var titleError by remember { mutableStateOf(false) }

    val presetPhases = listOf("الأساسات والحفر", "الهيكل الخرساني", "أعمال البناء", "التشطيبات", "التسليم النهائي")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إضافة مرحلة أو مهمة عمل",
                fontWeight = FontWeight.Bold,
                color = Slate800
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = it.isBlank()
                    },
                    label = { Text("اسم المهمة / الإنجاز المطلوب *") },
                    isError = titleError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_task_title")
                )

                Text(
                    text = "مرحلة العمل:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate700
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    presetPhases.chunked(2).forEach { rowPhases ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowPhases.forEach { p ->
                                FilterChip(
                                    selected = phase == p,
                                    onClick = { phase = p },
                                    label = { Text(p, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, phase)
                    } else {
                        titleError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ConstructionAmber),
                modifier = Modifier.testTag("btn_save_task")
            ) {
                Text("إضافة المهمة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun AddDailyLogDialog(
    onDismiss: () -> Unit,
    onSave: (workersCount: Int, weather: String, workSummary: String, supervisorNote: String) -> Unit
) {
    var workersCountText by remember { mutableStateOf("12") }
    var weather by remember { mutableStateOf("مشمس ومناسب للعمل") }
    var workSummary by remember { mutableStateOf("") }
    var supervisorNote by remember { mutableStateOf("") }
    var summaryError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تسجيل يومية الورشة",
                fontWeight = FontWeight.Bold,
                color = Slate800
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = workersCountText,
                    onValueChange = { workersCountText = it.filter { c -> c.isDigit() } },
                    label = { Text("عدد العمال المتواجدين اليوم") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = weather,
                    onValueChange = { weather = it },
                    label = { Text("حالة الطقس") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = workSummary,
                    onValueChange = {
                        workSummary = it
                        summaryError = it.isBlank()
                    },
                    label = { Text("ملخص الأعمال المنجزة اليوم *") },
                    isError = summaryError,
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_log_summary")
                )

                OutlinedTextField(
                    value = supervisorNote,
                    onValueChange = { supervisorNote = it },
                    label = { Text("ملاحظات وتوجيهات السلامة والنوعية") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (workSummary.isNotBlank()) {
                        val count = workersCountText.toIntOrNull() ?: 0
                        onSave(count, weather, workSummary, supervisorNote)
                    } else {
                        summaryError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ConstructionAmber),
                modifier = Modifier.testTag("btn_save_daily_log")
            ) {
                Text("تسجيل اليومية")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun ServerSyncDialog(
    initialUrl: String,
    isSyncing: Boolean,
    syncMessage: String?,
    onDismiss: () -> Unit,
    onSync: (url: String) -> Unit,
    onClearMessage: () -> Unit
) {
    var urlText by remember { mutableStateOf(initialUrl) }

    AlertDialog(
        onDismissRequest = {
            if (!isSyncing) {
                onClearMessage()
                onDismiss()
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = null,
                tint = ConstructionAmber,
                modifier = Modifier.width(36.dp)
            )
        },
        title = {
            Text(
                text = "مزامنة الـ API مع خادم الورشات",
                fontWeight = FontWeight.Bold,
                color = Slate800
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "يرتبط هذا التطبيق مباشرة مع خادم Node.js / Express المذكور في الكود عبر المسار:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate700
                )
                Surface(
                    color = Slate200,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "GET /api/projects\nPOST /api/projects",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Slate800,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = { Text("عنوان الخادم (Base URL)") },
                    supportingText = { Text("المحاكي: http://10.0.2.2:5000 أو عنوان IP خادمك") },
                    singleLine = true,
                    enabled = !isSyncing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_server_url")
                )

                if (syncMessage != null) {
                    Surface(
                        color = if (syncMessage.contains("نجاح")) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = syncMessage,
                            color = if (syncMessage.contains("نجاح")) Color(0xFF047857) else Color(0xFFB91C1C),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSync(urlText) },
                enabled = !isSyncing,
                colors = ButtonDefaults.buttonColors(containerColor = ConstructionAmber),
                modifier = Modifier.testTag("btn_trigger_sync")
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("جارية المزامنة...")
                } else {
                    Text("مزامنة الآن")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onClearMessage()
                    onDismiss()
                },
                enabled = !isSyncing
            ) {
                Text("إغلاق")
            }
        }
    )
}
