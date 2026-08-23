package com.school.gdsportal.ui.admin.assessment.examinations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.ExaminationStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExaminationDetailScreen(
    isReadOnly: Boolean = false,
    viewModel: ExaminationDetailViewModel,
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val exam = uiState.examination

    var expandedMenu by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Error") },
            text = { Text(uiState.error!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) {
                    Text("OK")
                }
            }
        )
    }

    if (showStatusDialog && exam != null) {
        var selectedStatus by remember { mutableStateOf(exam.status) }
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Update Status") },
            text = {
                Column {
                    ExaminationStatus.values().forEach { status ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedStatus == status,
                                onClick = { selectedStatus = status }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = status.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showStatusDialog = false
                        viewModel.updateStatus(selectedStatus)
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Examination") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isReadOnly && exam != null) {
                        IconButton(onClick = { expandedMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                        }
                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    expandedMenu = false
                                    onEditClick(exam.examinationId)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Update Status") },
                                onClick = {
                                    expandedMenu = false
                                    showStatusDialog = true
                                }
                            )
                        }
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
            if (exam != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // EXAMINATION
                    Column {
                        Text(
                            text = "EXAMINATION",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
                        DetailRow("Exam Name", exam.examName)
                    }

                    // SCHEDULE
                    Column {
                        Text(
                            text = "SCHEDULE",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
                        DetailRow("Exam Date", exam.examDate.toString())
                        DetailRow("Time", "${exam.startTime} – ${exam.endTime}")
                    }

                    // MARKS
                    Column {
                        Text(
                            text = "MARKS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
                        DetailRow("Maximum Marks", exam.maxMarks.toString())
                        DetailRow("Passing Marks", exam.passingMarks?.toString() ?: "N/A")
                    }

                    // ACADEMIC CONTEXT
                    Column {
                        Text(
                            text = "ACADEMIC CONTEXT",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
                        DetailRow("Academic Year", uiState.academicYearName)
                        DetailRow("Class", uiState.className)
                        DetailRow("Section", uiState.sectionName)
                        DetailRow("Subject", uiState.subjectName)
                    }

                    // STATUS
                    Column {
                        Text(
                            text = "STATUS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusChip(status = exam.status)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            if (uiState.isLoading || uiState.isUpdatingStatus) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
