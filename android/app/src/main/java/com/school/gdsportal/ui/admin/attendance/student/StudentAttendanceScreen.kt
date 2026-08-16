package com.school.gdsportal.ui.admin.attendance.student

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.StudentAttendanceDisplay
import com.school.gdsportal.data.remote.StudentAttendanceStatus
import com.school.gdsportal.ui.admin.teaching.teacherclasses.FilterDropdown
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAttendanceScreen(
    viewModel: StudentAttendanceViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Error") },
            text = { Text(uiState.error!!) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissError()
                    viewModel.loadAttendance()
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
                title = { Text("Student Attendance") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // ROW 1: Academic Year + Class
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FilterDropdown(
                            label = "Academic Year",
                            items = uiState.academicYears.map { it.academicYearId to it.yearName },
                            selectedId = uiState.selectedAcademicYearId,
                            onSelect = { viewModel.selectAcademicYear(it) }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FilterDropdown(
                            label = "Class",
                            items = uiState.classes.map { it.classId to it.className },
                            selectedId = uiState.selectedClassId,
                            onSelect = { viewModel.selectClass(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ROW 2: Section + Date
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FilterDropdown(
                            label = "Section",
                            items = uiState.sections.map { it.sectionId to it.sectionName },
                            selectedId = uiState.selectedSectionId,
                            onSelect = { viewModel.selectSection(it) },
                            enabled = uiState.selectedAcademicYearId != null && uiState.selectedClassId != null
                        )
                    }
                    var showDatePicker by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = uiState.selectedDate,
                            onValueChange = {},
                            label = { Text("Date") },
                            readOnly = true,
                            enabled = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showDatePicker = true }
                        )
                    }

                    if (showDatePicker) {
                        val currentParsed = try {
                            LocalDate.parse(uiState.selectedDate)
                        } catch (e: Exception) {
                            LocalDate.now()
                        }
                        val initialMillis = currentParsed.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
                        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
                        
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                            .atZone(java.time.ZoneOffset.UTC)
                                            .toLocalDate()
                                        viewModel.selectDate(selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                                    }
                                    showDatePicker = false
                                }) {
                                    Text("OK")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                    Text("Cancel")
                                }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }
                }
            }

            HorizontalDivider()

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading && uiState.attendanceRecords.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.selectedSectionId == null) {
                    Text(
                        text = "Select an academic context to view attendance.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (uiState.attendanceRecords.isEmpty()) {
                    Text(
                        text = "No attendance records found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.attendanceRecords) { record ->
                            StudentAttendanceItem(record = record)
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentAttendanceItem(record: StudentAttendanceDisplay) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.studentName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = record.registrationNumber,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (record.remarks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = record.remarks,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        StudentAttendanceStatusChip(status = record.status)
    }
}

@Composable
fun StudentAttendanceStatusChip(status: StudentAttendanceStatus?) {
    if (status == null) {
        Text(
            text = "Not Marked",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val (bgColor, textColor) = when (status) {
        StudentAttendanceStatus.PRESENT -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        StudentAttendanceStatus.ABSENT -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        StudentAttendanceStatus.LATE -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        StudentAttendanceStatus.LEAVE -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }

    Box(
        modifier = Modifier
            .background(bgColor, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}
