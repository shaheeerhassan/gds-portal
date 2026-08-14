package com.school.gdsportal.ui.admin.teachers.teaching.subjects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.TeacherSubjectDTO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherSubjectsScreen(
    viewModel: TeacherSubjectsViewModel,
    onBackClick: () -> Unit,
    onAssignClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<TeacherSubjectDTO?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assigned Subjects") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAssignClick) {
                Icon(Icons.Default.Add, contentDescription = "Assign Subject")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null && uiState.assignments.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = uiState.error ?: "", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadAssignments() }) {
                        Text("Retry")
                    }
                }
            } else if (uiState.assignments.isEmpty()) {
                Text(
                    text = "No subjects assigned yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.assignments) { assignment ->
                        TeacherSubjectCard(
                            assignment = assignment,
                            onRemoveClick = { showDeleteDialog = assignment }
                        )
                    }
                }
            }

            if (uiState.isUnassigning) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Remove Assignment") },
                text = { Text("Are you sure you want to remove ${showDeleteDialog!!.subjectName} from Section ${showDeleteDialog!!.sectionName}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.unassignSubject(showDeleteDialog!!.teacherSubjectId)
                            showDeleteDialog = null
                        }
                    ) {
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun TeacherSubjectCard(
    assignment: TeacherSubjectDTO,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = assignment.subjectName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Section: ${assignment.sectionName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onRemoveClick) {
                Text("Remove", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
