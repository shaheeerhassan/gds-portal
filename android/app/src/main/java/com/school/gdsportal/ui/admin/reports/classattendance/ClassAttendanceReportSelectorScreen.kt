package com.school.gdsportal.ui.admin.reports.classattendance

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
import java.text.DateFormatSymbols
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassAttendanceReportSelectorScreen(
    viewModel: ClassAttendanceReportSelectorViewModel,
    onBackClick: () -> Unit,
    onGenerateReportClick: (sectionId: Int, month: Int, year: Int, classId: Int, academicYearId: Int) -> Unit
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
                title = { Text("Class Attendance Report") },
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
                    Spacer(modifier = Modifier.height(8.dp))

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
                        text = "REPORT PERIOD",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val months = (1..12).map { it to DateFormatSymbols().months[it - 1] }
                    FilterDropdown(
                        label = "Month",
                        items = months,
                        selectedId = uiState.selectedMonth,
                        onSelect = { viewModel.selectMonth(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
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
                            val sectionId = uiState.selectedSectionId
                            val classId = uiState.selectedClassId
                            val academicYearId = uiState.selectedAcademicYearId
                            if (sectionId != null && classId != null && academicYearId != null) {
                                onGenerateReportClick(sectionId, uiState.selectedMonth, uiState.selectedYear, classId, academicYearId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.selectedAcademicYearId != null &&
                                uiState.selectedClassId != null &&
                                uiState.selectedSectionId != null
                    ) {
                        Text("Generate Report")
                    }
                }
            }
        }
    }
}
