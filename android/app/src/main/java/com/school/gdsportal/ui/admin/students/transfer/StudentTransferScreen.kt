package com.school.gdsportal.ui.admin.students.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentTransferScreen(
    viewModel: StudentTransferViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer Student") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                if (uiState.error != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                
                // Read-only Current Status Box
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CURRENT PLACEMENT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(text = "Academic Year: ${uiState.academicYearName}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Class: ${uiState.currentClassName}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Section: ${uiState.currentSectionName}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Roll No: ${uiState.currentEnrollment?.rollNumber ?: "None"}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                
                Text(
                    text = "Transfer within ${uiState.academicYearName}:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Class Dropdown
                var expandedClass by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedClass,
                    onExpandedChange = { expandedClass = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.selectedClass?.className ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("New Class *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClass) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedClass,
                        onDismissRequest = { expandedClass = false }
                    ) {
                        uiState.classes.forEach { schoolClass ->
                            DropdownMenuItem(
                                text = { Text(schoolClass.className) },
                                onClick = {
                                    viewModel.selectClass(schoolClass)
                                    expandedClass = false
                                }
                            )
                        }
                    }
                }

                // Section Dropdown
                var expandedSection by remember { mutableStateOf(false) }
                val isSectionEnabled = uiState.selectedClass != null
                
                ExposedDropdownMenuBox(
                    expanded = expandedSection && isSectionEnabled,
                    onExpandedChange = { if (isSectionEnabled) expandedSection = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.selectedSection?.sectionName ?: if (uiState.isLoadingSections) "Loading..." else "",
                        onValueChange = {},
                        readOnly = true,
                        enabled = isSectionEnabled,
                        label = { Text("New Section *") },
                        trailingIcon = { 
                            if (uiState.isLoadingSections) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSection) 
                            }
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSection && uiState.sections.isNotEmpty(),
                        onDismissRequest = { expandedSection = false }
                    ) {
                        uiState.sections.forEach { section ->
                            DropdownMenuItem(
                                text = { Text(section.sectionName) },
                                onClick = {
                                    viewModel.selectSection(section)
                                    expandedSection = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.rollNumber,
                    onValueChange = { viewModel.updateRollNumber(it) },
                    label = { Text("New Roll Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.transferStudent() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving && uiState.isFormValid
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Confirm Transfer")
                    }
                }
            }
        }
    }
}
