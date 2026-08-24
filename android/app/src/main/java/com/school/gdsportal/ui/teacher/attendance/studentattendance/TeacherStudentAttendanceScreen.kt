package com.school.gdsportal.ui.teacher.attendance.studentattendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.StudentAttendanceStatus
import com.school.gdsportal.ui.admin.teaching.teacherclasses.FilterDropdown
import com.school.gdsportal.ui.theme.AccentTeacher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherStudentAttendanceScreen(
    viewModel: TeacherStudentAttendanceViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.of("UTC")).toLocalDate()
                        viewModel.onDateSelected(date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE))
                    }
                    showDatePicker = false
                }) { Text("OK", color = AccentTeacher) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Class Attendance")
                        Text(
                            text = "Record daily attendance",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.displayRecords.isNotEmpty() && !uiState.isLoading) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Button(
                        onClick = { viewModel.saveAttendance() },
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeacher),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Save Attendance", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Success Snackbar Overlay
            if (uiState.saveSuccessMessage != null) {
                Surface(color = Color(0xFF4CAF50), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = uiState.saveSuccessMessage!!,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                LaunchedEffect(uiState.saveSuccessMessage) {
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearSuccessMessage()
                }
            }

            // Premium Filters Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Select Context", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentTeacher)

                    if (uiState.assignedClasses.isEmpty() && !uiState.isLoading) {
                        Text(
                            text = "You are not assigned as a Class Teacher for any section this year.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        FilterDropdown(
                            label = "My Assigned Class",
                            items = uiState.assignedClasses.map { it.sectionId to "${it.className} - ${it.sectionName}" },
                            selectedId = uiState.selectedSection?.sectionId,
                            onSelect = { selectedId ->
                                val section = uiState.assignedClasses.find { it.sectionId == selectedId }
                                if (section != null) viewModel.onSectionSelected(section)
                            }
                        )

                        OutlinedTextField(
                            value = uiState.selectedDate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Attendance Date") },
                            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = "Date", tint = AccentTeacher) },
                            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }

            // Data State Handling
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentTeacher)
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
            } else if (uiState.displayRecords.isEmpty() && uiState.assignedClasses.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No students found for this class.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (uiState.displayRecords.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STUDENTS (${uiState.displayRecords.size})",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { viewModel.markAllPresent() }) {
                                Text("Mark All Present", color = AccentTeacher, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    items(uiState.displayRecords) { record ->
                        StudentAttendanceCard(
                            record = record,
                            onStatusSelected = { newStatus ->
                                viewModel.updateLocalAttendanceStatus(record.studentId, newStatus)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentAttendanceCard(
    record: com.school.gdsportal.data.remote.StudentAttendanceDisplay,
    onStatusSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AccentTeacher.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Person, contentDescription = "Student", tint = AccentTeacher, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = record.studentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = record.registrationNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color-Coded Status Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val statuses = listOf(
                    StudentAttendanceStatus.PRESENT to Color(0xFF4CAF50),
                    StudentAttendanceStatus.ABSENT to Color(0xFFF44336),
                    StudentAttendanceStatus.LATE to Color(0xFFFF9800),
                    StudentAttendanceStatus.LEAVE to Color(0xFF9E9E9E)
                )

                statuses.forEach { (status, activeColor) ->
                    val isSelected = record.status == status
                    val containerColor = if (isSelected) activeColor else Color.Transparent
                    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(containerColor)
                            .clickable { onStatusSelected(status.name) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = status.name.take(3),
                            color = contentColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}