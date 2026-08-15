package com.school.gdsportal.ui.admin.teaching.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.school.gdsportal.ui.admin.teaching.teacherclasses.FilterDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableDirectoryScreen(
    viewModel: TimetableDirectoryViewModel,
    onMenuClick: () -> Unit,
    onAddClick: () -> Unit,
    onTimetableClick: (TimetableDisplay) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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
                title = { Text("Timetable") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.selectedSectionId != null) {
                        IconButton(onClick = onAddClick) {
                            Icon(Icons.Default.Add, contentDescription = "Add Timetable Entry")
                        }
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
                
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Manage class schedules and lesson assignments",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Filters
                    FilterDropdown(
                        label = "Academic Year",
                        items = uiState.academicYears.map { it.academicYearId to it.yearName },
                        selectedId = uiState.selectedAcademicYearId,
                        onSelect = { viewModel.selectAcademicYear(it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            FilterDropdown(
                                label = "Class",
                                items = uiState.classes.map { it.classId to it.className },
                                selectedId = uiState.selectedClassId,
                                onSelect = { viewModel.selectClass(it) },
                                enabled = uiState.selectedAcademicYearId != null
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            FilterDropdown(
                                label = "Section",
                                items = uiState.sections.map { it.sectionId to it.sectionName },
                                selectedId = uiState.selectedSectionId,
                                onSelect = { viewModel.selectSection(it) },
                                enabled = uiState.selectedClassId != null
                            )
                        }
                    }
                }

                HorizontalDivider()

                if (uiState.selectedSectionId == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Select an academic year, class, and section to view the timetable.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else if (uiState.timetableEntries.isEmpty() && !uiState.isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No timetable entries",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add the first lesson to begin building this schedule.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onAddClick) {
                            Text("Add Timetable Entry")
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val dayOrder = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                        val grouped = uiState.timetableEntries.groupBy { it.dayOfWeek }
                            .mapKeys { it.key.uppercase() }
                        
                        dayOrder.forEach { day ->
                            val entriesForDay = grouped[day]
                            if (!entriesForDay.isNullOrEmpty()) {
                                item {
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                                
                                val sortedEntries = entriesForDay.sortedBy { it.periodNumber }
                                items(sortedEntries) { entry ->
                                    TimetableRowItem(
                                        entry = entry,
                                        onClick = { onTimetableClick(entry) }
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                }
                            }
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
fun TimetableRowItem(
    entry: TimetableDisplay,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // LEFT: Period number
        Text(
            text = "${entry.periodNumber}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(32.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // CENTER: Subject Name, Teacher Name
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.subjectName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.teacherName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // RIGHT: Start/end time, chevron
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${entry.startTime}–${entry.endTime}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = ">",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
