package com.school.gdsportal.ui.teacher.examinations.manage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.school.gdsportal.ui.theme.AccentTeacher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherExaminationManageScreen(
    viewModel: TeacherExaminationManageViewModel,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onSuccess()
        }
    }

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.updateField() },
            title = { Text("Error", color = MaterialTheme.colorScheme.error) },
            text = { Text(uiState.error!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.updateField() }) { Text("OK", color = AccentTeacher) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Examination" else "Create Examination") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.saveExamination() },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeacher),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(if (uiState.isEditMode) "Save Changes" else "Create Exam", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentTeacher)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    FormSectionCard(title = "Examination Details") {
                        OutlinedTextField(
                            value = uiState.examName,
                            onValueChange = { viewModel.updateField(name = it) },
                            label = { Text("Examination Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
                        )

                        ExposedDropdownMenuBox(
                            expanded = subjectDropdownExpanded,
                            onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded }
                        ) {
                            val selectedSubject = uiState.availableSubjects.find { it.subjectId == uiState.subjectId }
                            OutlinedTextField(
                                value = selectedSubject?.subjectName ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Subject") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
                            )
                            ExposedDropdownMenu(
                                expanded = subjectDropdownExpanded,
                                onDismissRequest = { subjectDropdownExpanded = false }
                            ) {
                                uiState.availableSubjects.forEach { subject ->
                                    DropdownMenuItem(
                                        text = { Text(subject.subjectName) },
                                        onClick = { viewModel.updateField(subId = subject.subjectId); subjectDropdownExpanded = false }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = statusDropdownExpanded,
                            onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
                        ) {
                            val statuses = listOf("SCHEDULED", "ONGOING", "COMPLETED", "PUBLISHED")
                            OutlinedTextField(
                                value = uiState.status,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Examination Status") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
                            )
                            ExposedDropdownMenu(
                                expanded = statusDropdownExpanded,
                                onDismissRequest = { statusDropdownExpanded = false }
                            ) {
                                statuses.forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status) },
                                        onClick = { viewModel.updateField(stat = status); statusDropdownExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    FormSectionCard(title = "Schedule") {
                        var showDatePicker by remember { mutableStateOf(false) }
                        if (showDatePicker) {
                            val datePickerState = rememberDatePickerState()
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            val date = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.of("UTC")).toLocalDate()
                                            viewModel.updateField(date = date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE))
                                        }
                                        showDatePicker = false
                                    }) { Text("OK", color = AccentTeacher) }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                },
                                colors = DatePickerDefaults.colors(
                                    titleContentColor = AccentTeacher,
                                    headlineContentColor = AccentTeacher,
                                    selectedDayContainerColor = AccentTeacher,
                                    todayDateBorderColor = AccentTeacher
                                )
                            ) {
                                DatePicker(state = datePickerState)
                            }
                        }
                        Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                            OutlinedTextField(
                                value = uiState.examDate,
                                onValueChange = {},
                                label = { Text("Exam Date (yyyy-MM-dd)") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            var showStartTimePicker by remember { mutableStateOf(false) }
                            var showEndTimePicker by remember { mutableStateOf(false) }

                            if (showStartTimePicker) {
                                com.school.gdsportal.ui.admin.assessment.examinations.ExaminationTimePickerDialog(
                                    onDismissRequest = { showStartTimePicker = false },
                                    onTimeSelected = { hour, minute ->
                                        viewModel.updateField(start = String.format("%02d:%02d:00", hour, minute))
                                        showStartTimePicker = false
                                    }
                                )
                            }

                            if (showEndTimePicker) {
                                com.school.gdsportal.ui.admin.assessment.examinations.ExaminationTimePickerDialog(
                                    onDismissRequest = { showEndTimePicker = false },
                                    onTimeSelected = { hour, minute ->
                                        viewModel.updateField(end = String.format("%02d:%02d:00", hour, minute))
                                        showEndTimePicker = false
                                    }
                                )
                            }

                            Box(modifier = Modifier.weight(1f).clickable { showStartTimePicker = true }) {
                                OutlinedTextField(
                                    value = uiState.startTime,
                                    onValueChange = {},
                                    label = { Text("Start Time") },
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            Box(modifier = Modifier.weight(1f).clickable { showEndTimePicker = true }) {
                                OutlinedTextField(
                                    value = uiState.endTime,
                                    onValueChange = {},
                                    label = { Text("End Time") },
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    FormSectionCard(title = "Grading Configuration") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = uiState.maxMarks,
                                onValueChange = { viewModel.updateField(max = it) },
                                label = { Text("Max Marks") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
                            )
                            OutlinedTextField(
                                value = uiState.passingMarks,
                                onValueChange = { viewModel.updateField(pass = it) },
                                label = { Text("Passing Marks") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp)) // Padding for bottom bar
                }
            }
        }
    }
}

@Composable
private fun FormSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentTeacher)
            content()
        }
    }
}