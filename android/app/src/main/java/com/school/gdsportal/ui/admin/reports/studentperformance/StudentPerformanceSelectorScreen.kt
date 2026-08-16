package com.school.gdsportal.ui.admin.reports.studentperformance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.ui.admin.teaching.teacherclasses.FilterDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentPerformanceSelectorScreen(
    viewModel: StudentPerformanceSelectorViewModel,
    onBackClick: () -> Unit,
    onGenerateReport: (studentId: Long, academicYearId: Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Performance") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Student Performance",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select a student and academic year",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Academic Year Dropdown
                FilterDropdown(
                    label = "Academic Year *",
                    items = uiState.academicYears.map { it.academicYearId to it.yearName },
                    selectedId = uiState.selectedAcademicYearId,
                    onSelect = { viewModel.selectAcademicYear(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Class Dropdown
                FilterDropdown(
                    label = "Class (Optional)",
                    items = listOf(-1 to "All Classes") + uiState.classes.map { it.classId to it.className },
                    selectedId = uiState.selectedClassId ?: -1,
                    onSelect = { viewModel.selectClass(if (it == -1) null else it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Section Dropdown
                FilterDropdown(
                    label = "Section (Optional)",
                    items = listOf(-1 to "All Sections") + uiState.sections.map { it.sectionId to it.sectionName },
                    selectedId = uiState.selectedSectionId ?: -1,
                    onSelect = { viewModel.selectSection(if (it == -1) null else it) },
                    enabled = uiState.selectedClassId != null
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Student Search Field
                Box {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        label = { Text("Student *") },
                        placeholder = { Text("Search by name or reg. number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearStudent() }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )

                    // Search Results Dropdown
                    DropdownMenu(
                        expanded = uiState.students.isNotEmpty(),
                        onDismissRequest = {},
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        uiState.students.forEach { student ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "${student.firstName} ${student.lastName}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = student.registrationNumber,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = { viewModel.selectStudent(student) }
                            )
                        }
                    }
                }

                if (uiState.isSearching) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Generate Report Button
                Button(
                    onClick = {
                        val student = uiState.selectedStudent
                        val yearId = uiState.selectedAcademicYearId
                        if (student != null && yearId != null) {
                            onGenerateReport(student.studentId, yearId)
                        }
                    },
                    enabled = uiState.selectedStudent != null && uiState.selectedAcademicYearId != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generate Report")
                }
            }
        }
    }
}
