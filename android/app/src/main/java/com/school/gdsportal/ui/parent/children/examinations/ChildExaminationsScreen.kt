package com.school.gdsportal.ui.parent.children.examinations

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.ui.theme.AccentParent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildExaminationsScreen(viewModel: ChildExaminationsViewModel, onBackClick: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Examinations") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentParent)
            } else if (uiState.error != null) {
                Button(onClick = { viewModel.loadExaminations() }, modifier = Modifier.align(Alignment.Center), colors = ButtonDefaults.buttonColors(containerColor = AccentParent)) { Text("Retry") }
            } else if (uiState.examinations.isEmpty()) {
                Text("No upcoming examinations.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.examinations) { exam ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(exam.examName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentParent)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Date: ${exam.examDate}", style = MaterialTheme.typography.bodyMedium)
                                Text("Time: ${exam.startTime} - ${exam.endTime}", style = MaterialTheme.typography.bodyMedium)
                                Text("Max Marks: ${exam.maxMarks}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}