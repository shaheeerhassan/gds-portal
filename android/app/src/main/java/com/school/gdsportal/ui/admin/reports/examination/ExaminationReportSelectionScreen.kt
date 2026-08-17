package com.school.gdsportal.ui.admin.reports.examination

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
fun ExaminationReportSelectionScreen(
    viewModel: ExaminationReportSelectionViewModel,
    onBackClick: () -> Unit,
    onGenerateReportClick: (examinationId: Long) -> Unit
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
                title = { Text("Examination Report") },
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

                    Spacer(modifier = Modifier.height(16.dp))

                    val subjectItems = uiState.subjects.map { it.subjectId to it.subjectName }
                    FilterDropdown(
                        label = "Subject",
                        items = subjectItems,
                        selectedId = uiState.selectedSubjectId,
                        onSelect = { viewModel.selectSubject(it) },
                        enabled = uiState.selectedSectionId != null && uiState.subjects.isNotEmpty()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "EXAMINATION",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val examItems = uiState.filteredExaminations.map {
                        it.examinationId.toInt() to "${it.examName} (${it.examDate})"
                    }
                    FilterDropdown(
                        label = "Examination",
                        items = examItems,
                        selectedId = uiState.selectedExaminationId?.toInt(),
                        onSelect = { viewModel.selectExamination(it.toLong()) },
                        enabled = uiState.selectedSubjectId != null && uiState.filteredExaminations.isNotEmpty()
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            val examId = uiState.selectedExaminationId
                            if (examId != null) {
                                onGenerateReportClick(examId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.selectedExaminationId != null
                    ) {
                        Text("View Examination Report")
                    }
                }
            }
        }
    }
}
