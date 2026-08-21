package com.school.gdsportal.ui.parent.children.submissions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.ui.theme.AccentParent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildSubmissionsScreen(viewModel: ChildSubmissionsViewModel, onBackClick: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submissions") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentParent)
            } else if (uiState.error != null) {
                Button(onClick = { viewModel.loadSubmissions() }, modifier = Modifier.align(Alignment.Center), colors = ButtonDefaults.buttonColors(containerColor = AccentParent)) { Text("Retry") }
            } else if (uiState.submissions.isEmpty()) {
                Text("No submissions found.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.submissions) { submission ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Safely handle missing titles
                                Text(submission.assignmentTitle ?: "Assignment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentParent)
                                Spacer(modifier = Modifier.height(8.dp))

                                // Safely handle missing status
                                val statusName = submission.status?.name ?: "PENDING"
                                val statusColor = when (statusName) {
                                    "GRADED" -> Color(0xFF4CAF50)
                                    "LATE" -> Color(0xFFFF9800)
                                    else -> MaterialTheme.colorScheme.primary
                                }

                                Text("Status: $statusName", color = statusColor, fontWeight = FontWeight.SemiBold)
                                Text("Submitted: ${submission.submittedAt ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)

                                // Safely handle missing marks
                                if (submission.marksAwarded != null && submission.marksAwarded.toString() != "null") {
                                    Text("Marks: ${submission.marksAwarded}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                if (!submission.feedback.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Feedback: ${submission.feedback}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}