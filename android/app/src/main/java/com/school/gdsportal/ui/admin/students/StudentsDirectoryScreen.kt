package com.school.gdsportal.ui.admin.students

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.data.remote.dto.StudentDirectoryDTO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsDirectoryScreen(
    viewModel: StudentsDirectoryViewModel,
    onBackClick: () -> Unit,
    onAddStudentClick: () -> Unit,
    onStudentClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Students") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAddStudentClick) {
                        Icon(Icons.Default.Add, contentDescription = "Add Student")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Screen Header
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Students",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Browse and manage student records",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by name or registration number") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Student Count & Filter Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!uiState.isLoading && uiState.error == null && uiState.students.isNotEmpty()) {
                        Text(
                            text = "${uiState.totalStudents} Students",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    
                    TextButton(
                        onClick = {
                            viewModel.syncDraftFilters()
                            showFilterSheet = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Filter")
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.error != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = uiState.error ?: "An error occurred",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.retry() }) {
                                Text("Retry")
                            }
                        }
                    }
                    uiState.students.isEmpty() -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No students found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Adjust filters or try a different search.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(uiState.students.size, key = { uiState.students[it].studentId }) { index ->
                                val student = uiState.students[index]
                                StudentRow(
                                    student = student,
                                    onClick = { onStudentClick(student.studentId) }
                                )
                                HorizontalDivider()
                                
                                if (index >= uiState.students.size - 2 && uiState.hasNextPage && !uiState.isFetchingNextPage) {
                                    LaunchedEffect(index) {
                                        viewModel.loadNextPage()
                                    }
                                }
                            }
                            
                            if (uiState.isFetchingNextPage) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false }
            ) {
                FilterSheetContent(
                    uiState = uiState,
                    onAcademicYearChanged = viewModel::onDraftAcademicYearChanged,
                    onClassChanged = viewModel::onDraftClassChanged,
                    onSectionChanged = viewModel::onDraftSectionChanged,
                    onEnrollmentStatusChanged = viewModel::onDraftEnrollmentStatusChanged,
                    onApply = {
                        viewModel.applyFilters()
                        showFilterSheet = false
                    },
                    onReset = {
                        viewModel.resetFilters()
                        showFilterSheet = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheetContent(
    uiState: StudentsDirectoryUiState,
    onAcademicYearChanged: (AcademicYear?) -> Unit,
    onClassChanged: (SchoolClass?) -> Unit,
    onSectionChanged: (Section?) -> Unit,
    onEnrollmentStatusChanged: (Boolean?) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .navigationBarsPadding() // Prevent overlap with system nav bar
    ) {
        Text("Filter Students", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Academic Year Dropdown
        var yearExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = yearExpanded,
            onExpandedChange = { yearExpanded = !yearExpanded }
        ) {
            OutlinedTextField(
                value = uiState.draftAcademicYear?.yearName ?: "All",
                onValueChange = {},
                readOnly = true,
                label = { Text("Academic Year") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = yearExpanded,
                onDismissRequest = { yearExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All") },
                    onClick = {
                        onAcademicYearChanged(null)
                        yearExpanded = false
                    }
                )
                uiState.academicYears.forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year.yearName) },
                        onClick = {
                            onAcademicYearChanged(year)
                            yearExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Class Dropdown
        var classExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = classExpanded,
            onExpandedChange = { classExpanded = !classExpanded }
        ) {
            OutlinedTextField(
                value = uiState.draftClass?.className ?: "All",
                onValueChange = {},
                readOnly = true,
                label = { Text("Class") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = classExpanded,
                onDismissRequest = { classExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All") },
                    onClick = {
                        onClassChanged(null)
                        classExpanded = false
                    }
                )
                uiState.classes.forEach { schoolClass ->
                    DropdownMenuItem(
                        text = { Text(schoolClass.className) },
                        onClick = {
                            onClassChanged(schoolClass)
                            classExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Section Dropdown
        var sectionExpanded by remember { mutableStateOf(false) }
        val sectionEnabled = uiState.sections.isNotEmpty()
        ExposedDropdownMenuBox(
            expanded = sectionExpanded && sectionEnabled,
            onExpandedChange = { if (sectionEnabled) sectionExpanded = !sectionExpanded }
        ) {
            OutlinedTextField(
                value = uiState.draftSection?.sectionName ?: "All",
                onValueChange = {},
                readOnly = true,
                enabled = sectionEnabled,
                label = { Text("Section") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            if (sectionEnabled) {
                ExposedDropdownMenu(
                    expanded = sectionExpanded,
                    onDismissRequest = { sectionExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All") },
                        onClick = {
                            onSectionChanged(null)
                            sectionExpanded = false
                        }
                    )
                    uiState.sections.forEach { section ->
                        DropdownMenuItem(
                            text = { Text(section.sectionName) },
                            onClick = {
                                onSectionChanged(section)
                                sectionExpanded = false
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Enrollment Status
        Text("Enrollment Status", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.draftEnrollmentStatus == null,
                onClick = { onEnrollmentStatusChanged(null) },
                label = { Text("All") }
            )
            FilterChip(
                selected = uiState.draftEnrollmentStatus == true,
                onClick = { onEnrollmentStatusChanged(true) },
                label = { Text("Enrolled") }
            )
            FilterChip(
                selected = uiState.draftEnrollmentStatus == false,
                onClick = { onEnrollmentStatusChanged(false) },
                label = { Text("Not Enrolled") }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset")
            }
            Button(
                onClick = onApply,
                modifier = Modifier.weight(1f)
            ) {
                Text("Apply Filters")
            }
        }
    }
}

@Composable
fun StudentRow(student: StudentDirectoryDTO, onClick: () -> Unit) {
    val initials = getInitials(student.firstName, student.lastName)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${student.firstName} ${student.lastName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = student.registrationNumber,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            
            val enrollmentText = if (student.className != null) {
                "${student.className} · ${student.sectionName ?: "N/A"}"
            } else {
                "Not enrolled"
            }
            
            Text(
                text = enrollmentText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Chevron
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "View Profile",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getInitials(firstName: String, lastName: String): String {
    val firstInitial = firstName.firstOrNull()?.uppercase() ?: ""
    val lastInitial = lastName.firstOrNull()?.uppercase() ?: ""
    return "$firstInitial$lastInitial"
}
