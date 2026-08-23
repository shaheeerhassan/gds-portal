package com.school.gdsportal.ui.teacher.assignments.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.school.gdsportal.ui.theme.AccentTeacher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAssignmentDetailScreen(
    viewModel: TeacherAssignmentDetailViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current // Used to open links in the browser

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assignment Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentTeacher)
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadDetails() }, colors = ButtonDefaults.buttonColors(containerColor = AccentTeacher)) {
                        Text("Retry")
                    }
                }
            } else if (uiState.assignment != null) {
                val assignment = uiState.assignment!!

                val statusName = assignment.status.name
                val statusColor = when (statusName) {
                    "PUBLISHED" -> Color(0xFF4CAF50)
                    "CREATED" -> Color(0xFFFF9800)
                    "CLOSED" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Header Section
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = assignment.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(statusColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = statusName,
                                    color = statusColor,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // 2. Publish Action Button (Only show if CREATED)
                    if (statusName == "CREATED") {
                        item {
                            Button(
                                onClick = { viewModel.publishAssignment() },
                                enabled = !uiState.isPublishing,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentTeacher),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (uiState.isPublishing) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Publish Assignment")
                                }
                            }
                        }
                    }

                    // 3. Context & Details Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                val formattedDeadline = assignment.deadline?.replace("T23:59:59", "") ?: "No Deadline"

                                DetailRow("Subject", uiState.subjectName ?: "ID: ${assignment.subjectId}")
                                DetailRow("Section", uiState.sectionName ?: "ID: ${assignment.sectionId}")
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                DetailRow("Max Marks", assignment.maxMarks.toString())
                                DetailRow("Deadline", formattedDeadline)
                            }
                        }
                    }

                    // 4. Description Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentTeacher)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (!assignment.description.isNullOrBlank()) {
                                    Text(text = assignment.description, style = MaterialTheme.typography.bodyMedium)
                                } else {
                                    Text("No description provided.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // 5. Submissions Section
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Submissions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }

                    // Filter Chips
                    item {
                        var statusFilter by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.school.gdsportal.data.remote.SubmissionStatus?>(null) }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = statusFilter == null,
                                onClick = { statusFilter = null },
                                label = { Text("All") }
                            )
                            com.school.gdsportal.data.remote.SubmissionStatus.values().forEach { status ->
                                FilterChip(
                                    selected = statusFilter == status,
                                    onClick = { statusFilter = status },
                                    label = { Text(status.name) }
                                )
                            }
                        }

                        val filteredSubmissions = if (statusFilter == null) uiState.submissions else uiState.submissions.filter { it.status == statusFilter }

                        if (filteredSubmissions.isEmpty()) {
                            Text("No submissions found.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 16.dp))
                        } else {
                            var selectedSubmissionForGrading by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.school.gdsportal.data.remote.SubmissionDisplay?>(null) }

                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                filteredSubmissions.forEach { sub ->
                                    SubmissionItemCard(submission = sub, onClick = { selectedSubmissionForGrading = sub })
                                }
                            }

                            // -------------------------------------------------------------
                            // THE NEW GRADING POP-UP DIALOG
                            // -------------------------------------------------------------
                            if (selectedSubmissionForGrading != null) {
                                val subToGrade = selectedSubmissionForGrading!!
                                var marksInput by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(if (subToGrade.marksAwarded != "—") subToGrade.marksAwarded else "") }
                                var feedbackInput by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(subToGrade.feedback) }

                                Dialog(onDismissRequest = { selectedSubmissionForGrading = null }) {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(24.dp)) {
                                            Text(
                                                text = "Grade Submission",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentTeacher
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Student Info
                                            Text(subToGrade.studentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                            Text("Reg No: ${subToGrade.registrationNumber}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                            Spacer(modifier = Modifier.height(16.dp))

                                            // File Link Block
                                            if (subToGrade.fileUrl.isNotBlank()) {
                                                OutlinedButton(
                                                    onClick = {
                                                        try {
                                                            uriHandler.openUri(subToGrade.fileUrl)
                                                        } catch (e: Exception) {
                                                            // Catch invalid URLs silently or show toast
                                                        }
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Icon(Icons.Default.Link, contentDescription = "Link", modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "View Attachment",
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(16.dp))
                                            }

                                            // Input Fields
                                            OutlinedTextField(
                                                value = marksInput,
                                                onValueChange = { marksInput = it },
                                                label = { Text("Marks Awarded") },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            OutlinedTextField(
                                                value = feedbackInput,
                                                onValueChange = { feedbackInput = it },
                                                label = { Text("Feedback (Optional)") },
                                                modifier = Modifier.fillMaxWidth(),
                                                minLines = 3,
                                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
                                            )

                                            Spacer(modifier = Modifier.height(24.dp))

                                            // Action Buttons
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                                TextButton(onClick = { selectedSubmissionForGrading = null }) {
                                                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Button(
                                                    onClick = {
                                                        viewModel.gradeSubmission(
                                                            submissionId = subToGrade.submissionId,
                                                            marks = marksInput.toDoubleOrNull() ?: 0.0,
                                                            feedback = feedbackInput
                                                        )
                                                        selectedSubmissionForGrading = null
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeacher)
                                                ) {
                                                    Text("Save Grade")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubmissionItemCard(submission: com.school.gdsportal.data.remote.SubmissionDisplay, onClick: () -> Unit) {
    val statusColor = when (submission.status.name) {
        "GRADED" -> Color(0xFF4CAF50)
        "LATE" -> Color(0xFFF44336)
        "SUBMITTED" -> Color(0xFF2196F3)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(submission.studentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(submission.registrationNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (submission.fileUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("File Attached", style = MaterialTheme.typography.labelSmall, color = AccentTeacher)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(submission.status.name, color = statusColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (submission.marksAwarded != "—") {
                    Text("Marks: ${submission.marksAwarded}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}