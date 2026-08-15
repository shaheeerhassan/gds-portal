package com.school.gdsportal.ui.admin.academics.subjects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectCreateScreen(
    viewModel: SubjectCreateViewModel,
    onBackClick: () -> Unit,
    onSubjectCreated: (Int?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.createdSubjectId) {
        if (uiState.createdSubjectId != null) {
            val id = if (uiState.createdSubjectId == -1) null else uiState.createdSubjectId
            onSubjectCreated(id)
        }
    }

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
                title = { Text("Create Subject") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    "SUBJECT DETAILS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = uiState.subjectName,
                    onValueChange = { viewModel.updateForm(subjectName = it) },
                    label = { Text("Subject Name *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.subjectCode,
                    onValueChange = { viewModel.updateForm(subjectCode = it) },
                    label = { Text("Subject Code *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateForm(description = it) },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    minLines = 3,
                    maxLines = 5
                )

                Button(
                    onClick = { viewModel.createSubject() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Subject")
                }
            }

            if (uiState.isSaving) {
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
