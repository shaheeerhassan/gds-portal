package com.school.gdsportal.ui.admin.teachers.teaching.classes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAssignClassScreen(
    viewModel: TeacherAssignClassViewModel,
    onBackClick: () -> Unit,
    onAssignSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.assignSuccess) {
        if (uiState.assignSuccess) {
            onAssignSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.errorShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assign Class") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading && uiState.classes.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    var classExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = classExpanded,
                        onExpandedChange = { classExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedClass?.className ?: "Select Class",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Class") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = classExpanded,
                            onDismissRequest = { classExpanded = false }
                        ) {
                            uiState.classes.forEach { schoolClass ->
                                DropdownMenuItem(
                                    text = { Text(schoolClass.className) },
                                    onClick = {
                                        viewModel.selectClass(schoolClass)
                                        classExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (uiState.selectedClass != null) {
                        var sectionExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = sectionExpanded,
                            onExpandedChange = { sectionExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = uiState.selectedSection?.sectionName ?: "Select Section",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Section") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                enabled = uiState.sections.isNotEmpty()
                            )
                            ExposedDropdownMenu(
                                expanded = sectionExpanded,
                                onDismissRequest = { sectionExpanded = false }
                            ) {
                                if (uiState.sections.isEmpty() && !uiState.isLoading) {
                                    DropdownMenuItem(
                                        text = { Text("No sections available") },
                                        onClick = { sectionExpanded = false }
                                    )
                                } else {
                                    uiState.sections.forEach { section ->
                                        DropdownMenuItem(
                                            text = { Text(section.sectionName) },
                                            onClick = {
                                                viewModel.selectSection(section)
                                                sectionExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { viewModel.submitAssignment() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.selectedClass != null && uiState.selectedSection != null && !uiState.isAssigning
                    ) {
                        if (uiState.isAssigning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Assign Class")
                        }
                    }
                }
            }
        }
    }
}
