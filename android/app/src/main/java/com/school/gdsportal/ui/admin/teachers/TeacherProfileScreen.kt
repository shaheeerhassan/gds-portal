package com.school.gdsportal.ui.admin.teachers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.Teacher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherProfileScreen(
    viewModel: TeacherProfileViewModel,
    onBackClick: () -> Unit,
    onEditTeacherClick: () -> Unit,
    onPersonalInformationClick: () -> Unit,
    onTeachingAssignmentsClick: () -> Unit,
    onClassTeacherClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var expandedMenu by remember { mutableStateOf(false) }
    var showDeactivateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.deactivateSuccess) {
        if (uiState.deactivateSuccess) {
            onBackClick() // Navigate back to directory
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teacher Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { expandedMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Teacher") },
                            onClick = {
                                expandedMenu = false
                                onEditTeacherClick()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Deactivate Teacher", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                expandedMenu = false
                                showDeactivateDialog = true
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        
        if (showDeactivateDialog) {
            AlertDialog(
                onDismissRequest = { showDeactivateDialog = false },
                title = { Text("Deactivate Teacher?") },
                text = { Text("This will deactivate the teacher and disable their access to the portal. Their records and historical data will remain available.") },
                confirmButton = {
                    Button(
                        onClick = { 
                            showDeactivateDialog = false
                            viewModel.deactivateTeacher()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        if (uiState.isDeactivating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onError)
                        } else {
                            Text("Deactivate")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeactivateDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadTeacherProfile() }) {
                            Text("Retry")
                        }
                }
            } else if (uiState.teacher != null) {
                val teacher = uiState.teacher!!
                Column(
                    modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Avatar
                        val initials = "${teacher.firstName.firstOrNull() ?: ""}${teacher.lastName.firstOrNull() ?: ""}".uppercase()
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Name & ID
                        Text(
                            text = "${teacher.firstName} ${teacher.lastName}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = teacher.employeeId,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val statusText = if (teacher.active) "Active" else "Inactive"
                        val statusColor = if (teacher.active) androidx.compose.ui.graphics.Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                        
                        Surface(
                            color = statusColor.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = statusText,
                                color = statusColor,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Summary info Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (!teacher.qualification.isNullOrBlank()) {
                                    StatusRow(label = "Qualification", value = teacher.qualification)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                
                                val joinYear = teacher.hireDate.take(4) // assuming YYYY-MM-DD
                                StatusRow(label = "Joined", value = joinYear)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Navigation Rows
                        ProfileNavigationRow(
                            title = "Personal Information",
                            onClick = onPersonalInformationClick
                        )
                        HorizontalDivider()
                        ProfileNavigationRow(
                            title = "Teaching Assignments",
                            onClick = onTeachingAssignmentsClick
                        )
                        HorizontalDivider()
                        ProfileNavigationRow(
                            title = "Class Teacher",
                            onClick = onClassTeacherClick
                        )
                        HorizontalDivider()
                    }
                }

                if (uiState.deactivateError != null) {
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        action = {
                            TextButton(onClick = { viewModel.dismissDeactivateError() }) {
                                Text("Dismiss", color = MaterialTheme.colorScheme.inversePrimary)
                            }
                        }
                    ) {
                        Text(uiState.deactivateError!!)
                    }
                }
        }
    }
}
@Composable
private fun ProfileNavigationRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate to $title",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}
