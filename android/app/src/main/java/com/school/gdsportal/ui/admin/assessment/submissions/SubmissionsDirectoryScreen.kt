package com.school.gdsportal.ui.admin.assessment.submissions

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
import com.school.gdsportal.data.remote.SubmissionStatus
import com.school.gdsportal.data.remote.SubmissionDisplay
import com.school.gdsportal.ui.admin.teaching.teacherclasses.FilterDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionsDirectoryScreen(
    viewModel: SubmissionsDirectoryViewModel,
    onMenuClick: () -> Unit,
    onSubmissionClick: (SubmissionDisplay) -> Unit
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
                title = { Text("Submissions") },
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
                // ROW 1: Academic Year + Class
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

                // ROW 2: Section
                FilterDropdown(
                    label = "Section",
                    items = uiState.sections.map { it.sectionId to it.sectionName },
                    selectedId = uiState.selectedSectionId,
                    onSelect = { viewModel.selectSection(it) },
                    enabled = uiState.selectedAcademicYearId != null && uiState.selectedClassId != null
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ROW 3: Assignment selector (populated after section is selected)
                FilterDropdown(
                    label = "Assignment",
                    items = uiState.assignments.map { it.assignmentId.toInt() to it.title },
                    selectedId = uiState.selectedAssignmentId?.toInt(),
                    onSelect = { viewModel.selectAssignment(it.toLong()) },
                    enabled = uiState.selectedSectionId != null && uiState.assignments.isNotEmpty()
                )
            }

            HorizontalDivider()

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading && uiState.submissions.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.submissions.isEmpty() && uiState.selectedAssignmentId != null) {
                    Text(
                        text = "No submissions found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.submissions) { submission ->
                            SubmissionItem(submission = submission, onClick = { onSubmissionClick(submission) })
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubmissionItem(submission: SubmissionDisplay, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = submission.studentName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = submission.registrationNumber,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Submitted ${submission.submittedAt}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            SubmissionStatusChip(status = submission.status)
            if (submission.marksAwarded != "—") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${submission.marksAwarded} marks",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SubmissionStatusChip(status: SubmissionStatus) {
    val (bgColor, textColor) = when (status) {
        SubmissionStatus.SUBMITTED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        SubmissionStatus.LATE -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        SubmissionStatus.GRADED -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
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
