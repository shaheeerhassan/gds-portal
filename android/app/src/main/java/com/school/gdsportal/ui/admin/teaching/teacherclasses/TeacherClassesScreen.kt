package com.school.gdsportal.ui.admin.teaching.teacherclasses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.school.gdsportal.data.remote.AcademicYear

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherClassesScreen(
    viewModel: TeacherClassesViewModel,
    onBackClick: () -> Unit,
    onTeacherClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var showDeleteDialogForId by remember { mutableStateOf<Long?>(null) }

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

    if (showDeleteDialogForId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialogForId = null },
            title = { Text("Remove Teacher Assignment?") },
            text = { Text("This will remove the teacher's class/section assignment.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = showDeleteDialogForId!!
                        showDeleteDialogForId = null
                        viewModel.removeAssignment(id)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialogForId = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teacher Classes") },
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Filters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        FilterDropdown(
                            label = "Academic Year",
                            items = uiState.academicYears.map { it.academicYearId to it.yearName },
                            selectedId = uiState.selectedAcademicYearId,
                            onSelect = { viewModel.selectAcademicYear(it) }
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        FilterDropdown(
                            label = "Class",
                            items = listOf(-1 to "All Classes") + uiState.classes.map { it.classId to it.className },
                            selectedId = uiState.selectedClassId ?: -1,
                            onSelect = { if (it == -1) viewModel.selectClass(null) else viewModel.selectClass(it) },
                            enabled = uiState.selectedAcademicYearId != null
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FilterDropdown(
                            label = "Section",
                            items = listOf(-1 to "All Sections") + uiState.sections.map { it.sectionId to it.sectionName },
                            selectedId = uiState.selectedSectionId ?: -1,
                            onSelect = { if (it == -1) viewModel.selectSection(null) else viewModel.selectSection(it) },
                            enabled = uiState.selectedClassId != null
                        )
                    }
                }

                if (uiState.filteredAssignments.isEmpty() && !uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No assignments found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.filteredAssignments) { row ->
                            TeacherClassRowItem(
                                row = row,
                                onClick = { onTeacherClick(row.teacher.teacherId) },
                                onRemoveClick = { showDeleteDialogForId = row.dto.teacherClassId }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }

            if (uiState.isLoading) {
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
fun TeacherClassRowItem(
    row: TeacherClassRow,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${row.dto.className} · ${row.dto.sectionName}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${row.teacher.firstName} ${row.teacher.lastName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (row.teacher.employeeId != null) {
                Text(
                    text = row.teacher.employeeId,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More Options")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Remove Assignment", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        expanded = false
                        onRemoveClick()
                    }
                )
            }
        }
        
        Text(
            text = ">",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    label: String,
    items: List<Pair<Int, String>>,
    selectedId: Int?,
    onSelect: (Int) -> Unit,
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
            enabled = false,
            singleLine = true
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
