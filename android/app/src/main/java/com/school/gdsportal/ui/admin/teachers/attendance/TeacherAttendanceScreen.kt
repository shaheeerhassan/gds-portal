package com.school.gdsportal.ui.admin.teachers.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAttendanceScreen(
    viewModel: TeacherAttendanceViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.selectedDate
            .toEpochDay() * 86_400_000L
    )
    val displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val selected = LocalDate.ofEpochDay(millis / 86_400_000L)
                        viewModel.setDate(selected)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teacher Attendance") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { viewModel.saveAttendance() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = !uiState.isSaving && !uiState.isLoading && uiState.rows.isNotEmpty()
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving…")
                    } else {
                        Text("Save Attendance")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Date selector row
                Surface(
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Date",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = uiState.selectedDate.format(displayFormatter),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        FilledTonalIconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date")
                        }
                    }
                }

                HorizontalDivider()

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.error != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { viewModel.loadData() }) { Text("Retry") }
                        }
                    }
                } else if (uiState.rows.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No teachers found.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Section label
                    Text(
                        text = "${uiState.rows.size} TEACHERS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                    HorizontalDivider()

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.rows, key = { it.teacher.teacherId }) { row ->
                            AttendanceTeacherRow(
                                row = row,
                                onStatusChange = { newStatus ->
                                    viewModel.setStatus(row.teacher.teacherId, newStatus)
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }

            // Success snackbar
            if (uiState.successMessage != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.dismissMessages() }) {
                            Text("OK", color = MaterialTheme.colorScheme.inversePrimary)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(uiState.successMessage!!)
                }
            }

            // Error snackbar
            if (uiState.saveError != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.dismissMessages() }) {
                            Text("Dismiss", color = MaterialTheme.colorScheme.inversePrimary)
                        }
                    }
                ) {
                    Text(uiState.saveError!!)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceTeacherRow(
    row: TeacherAttendanceRow,
    onStatusChange: (String) -> Unit
) {
    val teacher = row.teacher
    val initials = "${teacher.firstName.firstOrNull() ?: ""}${teacher.lastName.firstOrNull() ?: ""}".uppercase()
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when (row.selectedStatus) {
        "PRESENT" -> Color(0xFF4CAF50)
        "ABSENT"  -> MaterialTheme.colorScheme.error
        "LATE"    -> Color(0xFFFF9800)
        "LEAVE"   -> Color(0xFF9C27B0)
        else      -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name + employee id
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${teacher.firstName} ${teacher.lastName}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = teacher.employeeId,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Status dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.width(130.dp)
        ) {
            OutlinedTextField(
                value = row.selectedStatus.lowercase().replaceFirstChar { it.uppercase() },
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                ),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = statusColor,
                    unfocusedBorderColor = statusColor.copy(alpha = 0.5f)
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                ATTENDANCE_STATUSES.forEach { status ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = status.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (status == row.selectedStatus) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onStatusChange(status)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
