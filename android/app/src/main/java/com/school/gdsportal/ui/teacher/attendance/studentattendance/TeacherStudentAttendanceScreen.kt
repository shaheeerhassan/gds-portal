package com.school.gdsportal.ui.teacher.attendance.studentattendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.StudentAttendance
import com.school.gdsportal.ui.theme.AccentTeacher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherStudentAttendanceScreen(
    viewModel: TeacherStudentAttendanceViewModel,
    onMenuClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    // Optional: Add DatePicker implementation here for uiState.selectedDate

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Attendance") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.attendanceRecords.isNotEmpty() && !uiState.isLoading) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Button(
                        onClick = { viewModel.saveAttendance() },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeacher),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Save Attendance")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Success Message Snackbar (Simplified)
            if (uiState.saveSuccessMessage != null) {
                Surface(color = Color(0xFF4CAF50), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = uiState.saveSuccessMessage!!,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearSuccessMessage()
                }
            }

            // Filters Area
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Class", style = MaterialTheme.typography.labelMedium, color = AccentTeacher)

                    // Class Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = uiState.selectedSection?.let { "${it.className} - ${it.sectionName}" } ?: "Loading...",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                                }
                            }
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            uiState.assignedClasses.forEach { section ->
                                DropdownMenuItem(
                                    text = { Text("${section.className} - ${section.sectionName}") },
                                    onClick = {
                                        viewModel.onSectionSelected(section)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Date Display (Ideally wired to a DatePickerDialog)
                    Text("Date: ${uiState.selectedDate}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            // Data State Handling
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentTeacher)
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
            } else if (uiState.attendanceRecords.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No students found for this class.")
                }
            } else {
                // Students List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.attendanceRecords) { record ->
                        StudentAttendanceCard(
                            record = record,
                            onStatusSelected = { newStatus ->
                                viewModel.updateLocalAttendanceStatus(record.attendanceId, newStatus)
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
    record: StudentAttendance,
    onStatusSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Wait, your StudentAttendance DTO might not contain the student's name directly.
            // If it doesn't, you may need to use Student ID or map it from the Directory API.
            Text(
                text = "Student ID: ${record.studentClassId}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Quick Status Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val statuses = listOf("PRESENT", "ABSENT", "LATE", "LEAVE")
                statuses.forEach { status ->
                    val isSelected = record.status.name == status
                    val containerColor = if (isSelected) AccentTeacher else Color.Transparent
                    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(containerColor)
                            .clickable { onStatusSelected(status) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(text = status.take(3), color = contentColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}