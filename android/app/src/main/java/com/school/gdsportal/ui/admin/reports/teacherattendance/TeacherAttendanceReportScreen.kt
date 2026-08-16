package com.school.gdsportal.ui.admin.reports.teacherattendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAttendanceReportScreen(
    viewModel: TeacherAttendanceReportViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Error") },
            text = { Text(uiState.error!!) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissError()
                    viewModel.loadReport()
                }) {
                    Text("Retry")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissError() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teacher Attendance Report") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.reportData != null) {
                val data = uiState.reportData!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column {
                            Text(
                                text = data.teacher.teacher_name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Attendance Report",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "ATTENDANCE SUMMARY",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    SummaryItem("Total Days", data.total_days.toString())
                                    SummaryItem("Attendance Rate", "${data.attendance_rate}%")
                                    SummaryItem("Absence Rate", "${data.absence_rate}%")
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    SummaryItem("PRESENT", data.present_days.toString())
                                    SummaryItem("ABSENT", data.absent_days.toString())
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    SummaryItem("LATE", data.late_days.toString())
                                    SummaryItem("LEAVE", data.leave_days.toString())
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "ATTENDANCE RECORDS",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (data.results.isEmpty()) {
                        item {
                            Text(
                                text = "No attendance records found for the selected period.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 32.dp)
                            )
                        }
                    } else {
                        items(data.results) { record ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatDate(record.attendance_date),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                StatusChip(status = record.status)
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status) {
        "PRESENT" -> MaterialTheme.colorScheme.primary
        "ABSENT" -> MaterialTheme.colorScheme.error
        "LATE" -> MaterialTheme.colorScheme.tertiary
        "LEAVE" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = when (status) {
        "PRESENT" -> MaterialTheme.colorScheme.onPrimary
        "ABSENT" -> MaterialTheme.colorScheme.onError
        "LATE" -> MaterialTheme.colorScheme.onTertiary
        "LEAVE" -> MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .background(color, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        if (date != null) outputFormat.format(date) else dateString
    } catch (e: Exception) {
        dateString
    }
}
