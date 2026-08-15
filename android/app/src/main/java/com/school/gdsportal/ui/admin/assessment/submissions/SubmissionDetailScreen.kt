package com.school.gdsportal.ui.admin.assessment.submissions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.ui.admin.assessment.assignments.DetailRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionDetailScreen(
    viewModel: SubmissionDetailViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val submission = uiState.submission

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(submission?.studentName ?: "Submission") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
                // No actions — read-only
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
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (submission != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // STUDENT
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "STUDENT",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider()

                        Text(
                            text = submission.studentName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (submission.registrationNumber.isNotEmpty()) {
                            Text(
                                text = submission.registrationNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // ASSIGNMENT
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "ASSIGNMENT",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider()

                        DetailRow(label = "Title", value = submission.assignmentTitle)
                        DetailRow(label = "Subject", value = uiState.subjectName)
                        DetailRow(label = "Class & Section", value = uiState.sectionName)
                        DetailRow(label = "Teacher", value = uiState.teacherName)
                    }

                    // SUBMISSION
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "SUBMISSION",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider()

                        DetailRow(label = "Submitted At", value = submission.submittedAt)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Status",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            SubmissionStatusChip(status = submission.status)
                        }

                        if (submission.fileUrl.isNotEmpty()) {
                            DetailRow(label = "File", value = submission.fileUrl)
                        }
                    }

                    // EVALUATION
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "EVALUATION",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider()

                        val marksText = if (uiState.maxMarks.isNotEmpty()) {
                            "${submission.marksAwarded} / ${uiState.maxMarks}"
                        } else {
                            submission.marksAwarded
                        }
                        DetailRow(label = "Marks", value = marksText)

                        DetailRow(
                            label = "Feedback",
                            value = submission.feedback.ifEmpty { "No feedback provided." }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
