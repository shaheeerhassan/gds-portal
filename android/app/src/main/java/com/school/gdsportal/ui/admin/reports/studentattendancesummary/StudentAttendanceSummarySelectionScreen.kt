package com.school.gdsportal.ui.admin.reports.studentattendancesummary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.school.gdsportal.ui.admin.teaching.teacherclasses.FilterDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAttendanceSummarySelectionScreen(
    viewModel: StudentAttendanceSummarySelectionViewModel,
    onBackClick: () -> Unit,
    onGenerateReportClick: (studentId: Long, academicYearId: Int, studentName: String, regNum: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

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
                title = { Text("Student Attendance Summary") },
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
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "ACADEMIC CONTEXT",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val yearItems = uiState.academicYears.map { it.academicYearId to it.yearName }
                    FilterDropdown(
                        label = "Academic Year",
                        items = yearItems,
                        selectedId = uiState.selectedAcademicYearId,
                        onSelect = { viewModel.selectAcademicYear(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val classItems = uiState.classes.map { it.classId to it.className }
                    FilterDropdown(
                        label = "Class",
                        items = classItems,
                        selectedId = uiState.selectedClassId,
                        onSelect = { viewModel.selectClass(it) },
                        enabled = uiState.selectedAcademicYearId != null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val sectionItems = uiState.sections.map { it.sectionId to it.sectionName }
                    FilterDropdown(
                        label = "Section",
                        items = sectionItems,
                        selectedId = uiState.selectedSectionId,
                        onSelect = { viewModel.selectSection(it) },
                        enabled = uiState.selectedClassId != null && uiState.sections.isNotEmpty()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "STUDENT",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val isStudentSearchEnabled = uiState.selectedSectionId != null

                    ExposedDropdownMenuBox(
                        expanded = uiState.isDropdownExpanded,
                        onExpandedChange = { }
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            label = { Text("Search Student") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .onFocusChanged { state ->
                                    if (!state.isFocused) {
                                        viewModel.dismissDropdown()
                                    }
                                },
                            enabled = isStudentSearchEnabled,
                            trailingIcon = {
                                if (uiState.isSearching) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                } else if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true
                        )

                        if (uiState.searchResults.isNotEmpty() && uiState.isDropdownExpanded) {
                            ExposedDropdownMenu(
                                expanded = uiState.isDropdownExpanded,
                                onDismissRequest = { viewModel.dismissDropdown() }
                            ) {
                                uiState.searchResults.forEach { student ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text("${student.firstName} ${student.lastName}")
                                                Text(
                                                    text = student.registrationNumber ?: "",
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
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            val st = uiState.selectedStudent
                            val year = uiState.selectedAcademicYearId
                            if (st != null && year != null) {
                                onGenerateReportClick(
                                    st.studentId,
                                    year,
                                    "${st.firstName} ${st.lastName}",
                                    st.registrationNumber ?: ""
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.selectedStudent != null && uiState.selectedAcademicYearId != null
                    ) {
                        Text("Generate Attendance Summary")
                    }
                }
            }
        }
    }
}
