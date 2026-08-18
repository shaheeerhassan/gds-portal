package com.school.gdsportal.ui.admin.academics.subjects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.school.gdsportal.data.remote.Subject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsDirectoryScreen(
    isReadOnly: Boolean = false,
    viewModel: SubjectsDirectoryViewModel,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onSubjectClick: (Int) -> Unit
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
                title = {
                    Column {
                        Text("Subjects", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Manage school subjects",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isReadOnly) {
                        IconButton(onClick = onAddClick) {
                            Icon(Icons.Default.Add, contentDescription = "Create Subject")
                        }
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
            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search subjects") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true
                )

                if (uiState.filteredSubjects.isEmpty() && !uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No subjects found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.filteredSubjects, key = { it.subjectId }) { subject ->
                            SubjectRow(
                                subject = subject,
                                onClick = { onSubjectClick(subject.subjectId) }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun SubjectRow(
    subject: Subject,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subject.subjectName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subject.subjectCode,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (!subject.description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subject.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = ">",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
