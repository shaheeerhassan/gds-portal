package com.school.gdsportal.ui.admin.reports.teacherperformance

import androidx.compose.foundation.clickable
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
fun TeacherPerformanceReportSelectorScreen(
    viewModel: TeacherPerformanceReportSelectorViewModel,
    onBackClick: () -> Unit,
    onGenerateReportClick: (teacherId: Long, academicYearId: Int) -> Unit
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
                title = { Text("Teacher Performance Report") },
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
                        text = "REPORT CONTEXT",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Teacher Search Field
                    ExposedDropdownMenuBox(
                        expanded = uiState.isDropdownExpanded,
                        onExpandedChange = { }
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            label = { Text("Teacher *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .onFocusChanged { state ->
                                    if (!state.isFocused) {
                                        viewModel.dismissDropdown()
                                    }
                                },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true
                        )

                        if (uiState.filteredTeachers.isNotEmpty() && uiState.isDropdownExpanded) {
                            ExposedDropdownMenu(
                                expanded = uiState.isDropdownExpanded,
                                onDismissRequest = { viewModel.dismissDropdown() }
                            ) {
                                uiState.filteredTeachers.forEach { teacher ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text("${teacher.firstName} ${teacher.lastName}")
                                                Text(
                                                    text = teacher.employeeId,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = { viewModel.selectTeacher(teacher) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val yearItems = uiState.academicYears.map { it.academicYearId to it.yearName }
                    FilterDropdown(
                        label = "Academic Year *",
                        items = yearItems,
                        selectedId = uiState.selectedAcademicYearId,
                        onSelect = { viewModel.selectAcademicYear(it) }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            val t = uiState.selectedTeacher
                            val y = uiState.selectedAcademicYearId
                            if (t != null && y != null) {
                                onGenerateReportClick(t.teacherId, y)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.selectedTeacher != null && uiState.selectedAcademicYearId != null
                    ) {
                        Text("Generate Report")
                    }
                }
            }
        }
    }
}
