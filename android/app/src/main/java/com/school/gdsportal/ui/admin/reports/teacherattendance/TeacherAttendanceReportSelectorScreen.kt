package com.school.gdsportal.ui.admin.reports.teacherattendance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.school.gdsportal.ui.admin.teaching.teacherclasses.FilterDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAttendanceReportSelectorScreen(
    viewModel: TeacherAttendanceReportSelectorViewModel,
    onBackClick: () -> Unit,
    onGenerateReportClick: (teacherId: Long, month: Int, year: Int) -> Unit
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
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Teacher",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = uiState.isDropdownExpanded,
                        onExpandedChange = { }
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = { Text("Search Teacher (Name or ID)") },
                            singleLine = true
                        )

                        if (uiState.filteredTeachers.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = uiState.isDropdownExpanded,
                                onDismissRequest = { viewModel.dismissDropdown() }
                            ) {
                                uiState.filteredTeachers.forEach { teacher ->
                                    DropdownMenuItem(
                                        text = { Text("${teacher.firstName} ${teacher.lastName}") },
                                        onClick = { viewModel.selectTeacher(teacher) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "REPORT PERIOD",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val months = (1..12).map { it to java.text.DateFormatSymbols().months[it - 1] }
                    FilterDropdown(
                        label = "Month",
                        items = months,
                        selectedId = uiState.selectedMonth,
                        onSelect = { viewModel.selectMonth(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val years = ((currentYear - 5)..(currentYear + 1)).map { it to it.toString() }
                    FilterDropdown(
                        label = "Year",
                        items = years,
                        selectedId = uiState.selectedYear,
                        onSelect = { viewModel.selectYear(it) }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            uiState.selectedTeacher?.let { teacher ->
                                onGenerateReportClick(teacher.teacherId, uiState.selectedMonth, uiState.selectedYear)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.selectedTeacher != null
                    ) {
                        Text("Generate Report")
                    }
                }
            }
        }
    }
}
