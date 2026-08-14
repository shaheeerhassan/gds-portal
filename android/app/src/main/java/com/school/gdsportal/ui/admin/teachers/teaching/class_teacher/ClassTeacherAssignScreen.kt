package com.school.gdsportal.ui.admin.teachers.teaching.class_teacher

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
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassTeacherAssignScreen(
    viewModel: ClassTeacherViewModel,
    onBackClick: () -> Unit,
    onAssignSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedYear by remember { mutableStateOf<AcademicYear?>(null) }
    var selectedClass by remember { mutableStateOf<SchoolClass?>(null) }
    var selectedSection by remember { mutableStateOf<Section?>(null) }

    var expandedYear by remember { mutableStateOf(false) }
    var expandedClass by remember { mutableStateOf(false) }
    var expandedSection by remember { mutableStateOf(false) }

    // When both year and class are selected, fetch the sections.
    LaunchedEffect(selectedYear, selectedClass) {
        selectedSection = null // Reset section selection when dependencies change
        if (selectedYear != null && selectedClass != null) {
            viewModel.loadSectionsForAssignment(selectedClass!!.classId, selectedYear!!.academicYearId)
        } else {
            viewModel.clearSections()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assign Class Teacher") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                // Academic Year Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedYear,
                    onExpandedChange = { expandedYear = !expandedYear }
                ) {
                    OutlinedTextField(
                        value = selectedYear?.yearName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Academic Year *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedYear) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedYear,
                        onDismissRequest = { expandedYear = false }
                    ) {
                        uiState.academicYears.forEach { year ->
                            DropdownMenuItem(
                                text = { Text(year.yearName) },
                                onClick = {
                                    selectedYear = year
                                    expandedYear = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Class Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedClass,
                    onExpandedChange = { expandedClass = !expandedClass }
                ) {
                    OutlinedTextField(
                        value = selectedClass?.className ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Class *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClass) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedClass,
                        onDismissRequest = { expandedClass = false }
                    ) {
                        uiState.classes.forEach { schoolClass ->
                            DropdownMenuItem(
                                text = { Text(schoolClass.className) },
                                onClick = {
                                    selectedClass = schoolClass
                                    expandedClass = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section Dropdown
                val isSectionEnabled = selectedYear != null && selectedClass != null && uiState.availableSections.isNotEmpty()
                
                ExposedDropdownMenuBox(
                    expanded = expandedSection,
                    onExpandedChange = { if (isSectionEnabled) expandedSection = !expandedSection }
                ) {
                    OutlinedTextField(
                        value = selectedSection?.sectionName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Section *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSection) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = isSectionEnabled,
                        supportingText = {
                            if (selectedYear != null && selectedClass != null && uiState.availableSections.isEmpty()) {
                                Text("No sections found for this class and year")
                            }
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSection,
                        onDismissRequest = { expandedSection = false }
                    ) {
                        uiState.availableSections.forEach { section ->
                            DropdownMenuItem(
                                text = { Text(section.sectionName) },
                                onClick = {
                                    selectedSection = section
                                    expandedSection = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (selectedSection != null && selectedYear != null) {
                            viewModel.assignClassTeacher(
                                sectionId = selectedSection!!.sectionId,
                                academicYearId = selectedYear!!.academicYearId,
                                onSuccess = onAssignSuccess
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedYear != null && selectedClass != null && selectedSection != null && !uiState.isAssigning
                ) {
                    if (uiState.isAssigning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Assign Class Teacher")
                    }
                }
            }

            if (uiState.assignError != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.dismissError() }) {
                            Text("Dismiss", color = MaterialTheme.colorScheme.inversePrimary)
                        }
                    }
                ) {
                    Text(uiState.assignError!!)
                }
            }
        }
    }
}
