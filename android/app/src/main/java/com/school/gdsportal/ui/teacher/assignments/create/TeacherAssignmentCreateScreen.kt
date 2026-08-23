package com.school.gdsportal.ui.teacher.assignments.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.AssignmentStatus
import com.school.gdsportal.ui.theme.AccentTeacher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAssignmentCreateScreen(
    viewModel: TeacherAssignmentCreateViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.util.Date(millis)
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        viewModel.onDeadlineChanged(sdf.format(date))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onBackClick()
        }
    }

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Error") },
            text = { Text(uiState.error!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) { Text("OK") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Assignment") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentTeacher)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Class/Section Dropdown
                    var sectionExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = sectionExpanded,
                        onExpandedChange = { sectionExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.assignedClasses.find { it.sectionId == uiState.selectedSectionId }?.let { "${it.className} - ${it.sectionName}" } ?: "Select Class/Section",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Class / Section") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
                        )
                        ExposedDropdownMenu(expanded = sectionExpanded, onDismissRequest = { sectionExpanded = false }) {
                            uiState.assignedClasses.forEach { cls ->
                                DropdownMenuItem(
                                    text = { Text("${cls.className} - ${cls.sectionName}") },
                                    onClick = {
                                        viewModel.onSectionSelected(cls.sectionId)
                                        sectionExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Subject Dropdown
                    var subjectExpanded by remember { mutableStateOf(false) }
                    val availableSubjects = uiState.assignedSubjects.filter { it.sectionId == uiState.selectedSectionId }
                    
                    ExposedDropdownMenuBox(
                        expanded = subjectExpanded,
                        onExpandedChange = { if (uiState.selectedSectionId != null) subjectExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = availableSubjects.find { it.subjectId == uiState.selectedSubjectId }?.subjectName ?: "Select Subject",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Subject") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            enabled = uiState.selectedSectionId != null,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
                        )
                        ExposedDropdownMenu(expanded = subjectExpanded, onDismissRequest = { subjectExpanded = false }) {
                            availableSubjects.forEach { sub ->
                                DropdownMenuItem(
                                    text = { Text(sub.subjectName) },
                                    onClick = {
                                        viewModel.onSubjectSelected(sub.subjectId)
                                        subjectExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = viewModel::onTitleChanged,
                        label = { Text("Assignment Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
                    )

                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = viewModel::onDescriptionChanged,
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = uiState.maxMarks,
                            onValueChange = viewModel::onMaxMarksChanged,
                            label = { Text("Max Marks") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
                        )

                        androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f).clickable { showDatePicker = true }) {
                            OutlinedTextField(
                                value = uiState.deadline,
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                label = { Text("Deadline") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(
                            onClick = onBackClick,
                            enabled = !uiState.isSaving
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.submitAssignment(AssignmentStatus.CREATED) },
                            enabled = !uiState.isSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text("Save Draft")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.submitAssignment(AssignmentStatus.PUBLISHED) },
                            enabled = !uiState.isSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeacher)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            } else {
                                Text("Publish")
                            }
                        }
                    }
                }
            }
        }
    }
}



