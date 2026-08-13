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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

                // Student Count
                if (!uiState.isLoading && uiState.error == null && uiState.students.isNotEmpty()) {
                    Text(
                        text = "${uiState.totalStudents} Students",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
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
                                text = "Students will appear here once they are added.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(uiState.students, key = { it.studentId }) { student ->
                                StudentRow(
                                    student = student,
                                    onClick = { onStudentClick(student.studentId) }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
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
