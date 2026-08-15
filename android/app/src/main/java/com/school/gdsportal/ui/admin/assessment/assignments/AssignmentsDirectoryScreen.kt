package com.school.gdsportal.ui.admin.assessment.assignments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.AssignmentStatus
import com.school.gdsportal.data.remote.AssignmentDisplay
import com.school.gdsportal.ui.admin.teaching.teacherclasses.FilterDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentsDirectoryScreen(
    viewModel: AssignmentsDirectoryViewModel,
    onMenuClick: () -> Unit,
    onAssignmentClick: (AssignmentDisplay) -> Unit
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
                title = { Text("Assignments") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
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
                if (uiState.isLoading && uiState.assignments.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.assignments.isEmpty() && uiState.selectedSectionId != null) {
                    Text(
                        text = "No assignments found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.assignments) { assignment ->
                            AssignmentItem(assignment = assignment, onClick = { onAssignmentClick(assignment) })
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssignmentItem(assignment: AssignmentDisplay, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = assignment.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${assignment.subjectName} · ${assignment.sectionName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = assignment.teacherName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Due ${assignment.deadline}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            AssignmentStatusChip(status = assignment.status)
        }
    }
}

@Composable
fun AssignmentStatusChip(status: AssignmentStatus) {
    val (bgColor, textColor) = when (status) {
        AssignmentStatus.CREATED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        AssignmentStatus.PUBLISHED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
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
