package com.school.gdsportal.ui.admin.teaching.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.school.gdsportal.ui.admin.teaching.teacherclasses.FilterDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableFormScreen(
    viewModel: TimetableFormViewModel,
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Timetable Entry" else "Create Timetable Entry") },
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
                        onSelect = { viewModel.selectClass(it) },
                        enabled = uiState.selectedAcademicYearId != null
                    )
                    FilterDropdown(
                        label = "Section *",
                        items = uiState.sections.map { it.sectionId to it.sectionName },
                        selectedId = uiState.selectedSectionId,
                        onSelect = { viewModel.selectSection(it) },
                        enabled = uiState.selectedClassId != null
                    )
                }

                // LESSON DETAILS
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "LESSON DETAILS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FilterDropdown(
                        label = "Day *",
                        items = uiState.daysOfWeek.mapIndexed { index, day -> index to day },
                        selectedId = uiState.daysOfWeek.indexOf(uiState.selectedDayOfWeek).takeIf { it >= 0 },
                        onSelect = { viewModel.selectDay(uiState.daysOfWeek[it]) }
                    )
                    FilterDropdown(
                        label = "Period *",
                        items = uiState.periods.map { it.periodId to "Period ${it.periodNumber} (${it.startTime}-${it.endTime})" },
                        selectedId = uiState.selectedPeriodId,
                        onSelect = { viewModel.selectPeriod(it) }
                    )
                    FilterDropdown(
                        label = "Subject *",
                        items = uiState.subjects.map { it.subjectId to it.subjectName },
                        selectedId = uiState.selectedSubjectId,
                        onSelect = { viewModel.selectSubject(it) }
                    )
                    TeacherDropdown(
                        label = "Teacher *",
                        items = uiState.availableTeachers.map { it.teacherId to "${it.firstName} ${it.lastName}" },
                        selectedId = uiState.selectedTeacherId,
                        onSelect = { viewModel.selectTeacher(it) },
                        enabled = uiState.selectedSectionId != null && uiState.selectedSubjectId != null
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
fun TeacherDropdown(
    label: String,
    items: List<Pair<Long, String>>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = items.find { it.first == selectedId }?.second ?: ""

    Box {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        expanded = false
                        onSelect(id)
                    }
                )
            }
        }
        if (enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = true }
            )
        }
    }
}
