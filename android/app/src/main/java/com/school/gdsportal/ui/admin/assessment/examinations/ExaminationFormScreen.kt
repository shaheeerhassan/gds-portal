package com.school.gdsportal.ui.admin.assessment.examinations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.school.gdsportal.ui.admin.teaching.teacherclasses.FilterDropdown
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.clickable
import com.school.gdsportal.data.remote.ExaminationStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExaminationFormScreen(
    viewModel: ExaminationFormViewModel,
    onBackClick: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            onSubmitSuccess()
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

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        viewModel.onExamDateChange(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showStartTimePicker) {
        ExaminationTimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            onTimeSelected = { hour, minute ->
                val timeString = String.format("%02d:%02d:00", hour, minute)
                viewModel.onStartTimeChange(timeString)
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        ExaminationTimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            onTimeSelected = { hour, minute ->
                val timeString = String.format("%02d:%02d:00", hour, minute)
                viewModel.onEndTimeChange(timeString)
                showEndTimePicker = false
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
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.submit() },
                        enabled = uiState.isFormValid && !uiState.isSubmitting
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // EXAMINATION DETAILS
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "EXAMINATION DETAILS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = uiState.examName,
                        onValueChange = { viewModel.onExamNameChange(it) },
                        label = { Text("Exam Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // ACADEMIC CONTEXT
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "ACADEMIC CONTEXT",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FilterDropdown(
                        label = "Academic Year *",
                        items = uiState.academicYears.map { it.academicYearId to it.yearName },
                        selectedId = uiState.selectedAcademicYearId,
                        onSelect = { viewModel.selectAcademicYear(it) }
                    )
                    FilterDropdown(
                        label = "Class *",
                        items = uiState.classes.map { it.classId to it.className },
                        selectedId = uiState.selectedClassId,
                        onSelect = { viewModel.selectClass(it) }
                    )
                    FilterDropdown(
                        label = "Section *",
                        items = uiState.sections.map { it.sectionId to it.sectionName },
                        selectedId = uiState.selectedSectionId,
                        onSelect = { viewModel.selectSection(it) },
                        enabled = uiState.selectedAcademicYearId != null && uiState.selectedClassId != null
                    )
                    FilterDropdown(
                        label = "Subject *",
                        items = uiState.subjects.map { it.subjectId to it.subjectName },
                        selectedId = uiState.selectedSubjectId,
                        onSelect = { viewModel.selectSubject(it) }
                    )
                }

                // SCHEDULE
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "SCHEDULE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = uiState.examDate,
                        onValueChange = { },
                        label = { Text("Date (YYYY-MM-DD) *") },
                        modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = uiState.startTime,
                            onValueChange = { },
                            label = { Text("Start (HH:MM) *") },
                            modifier = Modifier.weight(1f).clickable { showStartTimePicker = true },
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        OutlinedTextField(
                            value = uiState.endTime,
                            onValueChange = { },
                            label = { Text("End (HH:MM) *") },
                            modifier = Modifier.weight(1f).clickable { showEndTimePicker = true },
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }

                // MARKS
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "MARKS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = uiState.maxMarks,
                            onValueChange = { viewModel.onMaxMarksChange(it) },
                            label = { Text("Max Marks *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = uiState.passingMarks,
                            onValueChange = { viewModel.onPassingMarksChange(it) },
                            label = { Text("Passing Marks") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                }

                // STATUS
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "STATUS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FilterDropdown(
                        label = "Exam Status *",
                        items = ExaminationStatus.values().map { it.name.hashCode() to it.name },
                        selectedId = uiState.status.name.hashCode(),
                        onSelect = { selectedHash ->
                            val selectedStatus = ExaminationStatus.values().find { it.name.hashCode() == selectedHash }
                            if (selectedStatus != null) {
                                viewModel.onStatusChange(selectedStatus)
                            }
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            if (uiState.isLoading || uiState.isSubmitting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExaminationTimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    val timePickerState = rememberTimePickerState()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}
