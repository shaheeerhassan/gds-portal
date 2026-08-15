package com.school.gdsportal.ui.admin.teaching.classteacher

import androidx.compose.foundation.clickable
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
fun ClassTeacherAssignmentScreen(
    viewModel: ClassTeacherAssignmentViewModel,
    onBackClick: () -> Unit,
    onAssignmentSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onAssignmentSaved()
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove Class Teacher?") },
            text = { Text("This will remove the Class Teacher assignment for this section. The section and teacher records will not be deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.removeAssignment()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (uiState.isDetailMode) "Assignment Details" 
                        else if (uiState.isChangeMode) "Change Teacher" 
                        else "Add Class Teacher"
                    )
                },
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
            if (!uiState.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    if (uiState.isDetailMode) {
                        // View mode
                        Text(
                            "ACADEMIC INFORMATION",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        val yearName = uiState.academicYears.find { it.academicYearId == uiState.selectedAcademicYearId }?.yearName ?: ""
                        val className = uiState.classes.find { it.classId == uiState.selectedClassId }?.className ?: ""
                        val sectionName = uiState.allSections.find { it.sectionId == uiState.selectedSectionId }?.sectionName ?: ""
                        
                        DetailRow("Academic Year", yearName)
                        DetailRow("Class", className)
                        DetailRow("Section", sectionName)

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "CLASS TEACHER",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        val teacher = uiState.teachers.find { it.teacherId == uiState.selectedTeacherId }
                        if (teacher != null) {
                            Text(
                                text = "${teacher.firstName} ${teacher.lastName}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            if (teacher.employeeId != null) {
                                Text(
                                    text = "Employee ID\n${teacher.employeeId}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 24.dp)
                                )
                            }
                        } else {
                            Text("No Class Teacher Assigned", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 24.dp))
                        }

                        Button(
                            onClick = { viewModel.enterChangeMode() },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Text("Change Teacher")
                        }

                        if (uiState.existingAssignment != null) {
                            OutlinedButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Remove Assignment")
                            }
                        }

                    } else {
                        // Form mode (Add or Change)
                        DropdownSelector(
                            label = "Academic Year *",
                            items = uiState.academicYears.map { it.academicYearId to it.yearName },
                            selectedId = uiState.selectedAcademicYearId,
                            onSelect = { viewModel.selectAcademicYear(it) },
                            enabled = !uiState.isChangeMode && !uiState.isDetailMode
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        DropdownSelector(
                            label = "Class *",
                            items = uiState.classes.map { it.classId to it.className },
                            selectedId = uiState.selectedClassId,
                            onSelect = { viewModel.selectClass(it) },
                            enabled = uiState.selectedAcademicYearId != null && !uiState.isChangeMode && !uiState.isDetailMode
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        DropdownSelector(
                            label = "Section *",
                            items = uiState.availableSections.map { it.sectionId to it.sectionName },
                            selectedId = uiState.selectedSectionId,
                            onSelect = { viewModel.selectSection(it) },
                            enabled = uiState.selectedClassId != null && !uiState.isChangeMode && !uiState.isDetailMode
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        DropdownSelector(
                            label = "Teacher *",
                            items = uiState.teachers.map { it.teacherId.toInt() to "${it.firstName} ${it.lastName} (${it.employeeId ?: ""})" },
                            selectedId = uiState.selectedTeacherId?.toInt(),
                            onSelect = { viewModel.selectTeacher(it.toLong()) },
                            enabled = true
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { viewModel.submitAssignment() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (uiState.isChangeMode) "Save Changes" else "Assign Class Teacher")
                        }
                    }
                }
            }

            if (uiState.isLoading || uiState.isSaving || uiState.isDeleting) {
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
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    items: List<Pair<Int, String>>,
    selectedId: Int?,
    onSelect: (Int) -> Unit,
    enabled: Boolean
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
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { expanded = true },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            enabled = false
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
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
        // Catch clicks for disabled textfield
        if (enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = true }
            )
        }
    }
}
