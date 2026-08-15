package com.school.gdsportal.ui.admin.assessment.examinations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.ExaminationStatus
import com.school.gdsportal.ui.admin.teaching.teacherclasses.FilterDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExaminationsDirectoryScreen(
    viewModel: ExaminationsDirectoryViewModel,
    onMenuClick: () -> Unit,
    onAddClick: () -> Unit,
    onExaminationClick: (ExaminationDisplay) -> Unit
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
                title = { Text("Examinations") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Default.Add, contentDescription = "Add Examination")
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
                // INDEPENDENT SELECTORS
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

                // DEPENDENT SELECTOR
                FilterDropdown(
                    label = "Section",
                    items = uiState.sections.map { it.sectionId to it.sectionName },
                    selectedId = uiState.selectedSectionId,
                    onSelect = { viewModel.selectSection(it) },
                    enabled = uiState.selectedAcademicYearId != null && uiState.selectedClassId != null
                )
            }
            
            HorizontalDivider()
            
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading && uiState.examinations.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.examinations.isEmpty() && uiState.selectedSectionId != null) {
                    Text(
                        text = "No examinations found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.examinations) { exam ->
                            ExaminationItem(exam = exam, onClick = { onExaminationClick(exam) })
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExaminationItem(exam: ExaminationDisplay, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exam.examName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = exam.subjectName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = exam.examDate,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            StatusChip(status = exam.status)
        }
    }
}

@Composable
fun StatusChip(status: ExaminationStatus) {
    val (bgColor, textColor) = when (status) {
        ExaminationStatus.SCHEDULED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        ExaminationStatus.ONGOING -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        ExaminationStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        ExaminationStatus.PUBLISHED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Box(
        modifier = Modifier
            .background(bgColor, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}
