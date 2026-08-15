package com.school.gdsportal.ui.admin.academics.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionCreateScreen(
    viewModel: SectionCreateViewModel,
    onBackClick: () -> Unit,
    onSectionCreated: (Int?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var yearExpanded by remember { mutableStateOf(false) }
    var classExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.createdSectionId) {
        if (uiState.createdSectionId != null) {
            val id = if (uiState.createdSectionId == -1) null else uiState.createdSectionId
            onSectionCreated(id)
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
                title = { Text("Create Section") },
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
                    "SECTION DETAILS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = yearExpanded,
                    onExpandedChange = { yearExpanded = it }
                ) {
                    OutlinedTextField(
                        value = uiState.academicYears.find { it.academicYearId == uiState.selectedAcademicYearId }?.yearName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Academic Year *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = yearExpanded,
                        onDismissRequest = { yearExpanded = false }
                    ) {
                        uiState.academicYears.forEach { year ->
                            DropdownMenuItem(
                                text = { Text(year.yearName) },
                                onClick = {
                                    viewModel.updateForm(academicYearId = year.academicYearId)
                                    yearExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = classExpanded,
                    onExpandedChange = { classExpanded = it }
                ) {
                    OutlinedTextField(
                        value = uiState.classes.find { it.classId == uiState.selectedClassId }?.className ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Class *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = classExpanded,
                        onDismissRequest = { classExpanded = false }
                    ) {
                        uiState.classes.forEach { cls ->
                            DropdownMenuItem(
                                text = { Text(cls.className) },
                                onClick = {
                                    viewModel.updateForm(classId = cls.classId)
                                    classExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.sectionName,
                    onValueChange = { viewModel.updateForm(sectionName = it) },
                    label = { Text("Section Name *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.capacity,
                    onValueChange = { viewModel.updateForm(capacity = it) },
                    label = { Text("Capacity") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.roomNumber,
                    onValueChange = { viewModel.updateForm(roomNumber = it) },
                    label = { Text("Room Number") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    singleLine = true
                )

                Button(
                    onClick = { viewModel.createSection() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Section")
                }
            }

            if (uiState.isLoading || uiState.isSaving) {
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
