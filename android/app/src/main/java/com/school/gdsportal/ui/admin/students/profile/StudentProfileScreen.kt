package com.school.gdsportal.ui.admin.students.profile

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.school.gdsportal.data.remote.Student

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    viewModel: StudentProfileViewModel,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onPersonalClick: () -> Unit,
    onEnrollmentClick: () -> Unit,
    onParentsClick: () -> Unit,
    onEnrollClick: () -> Unit,
    onTransferClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showEndEnrollmentDialog by remember { mutableStateOf(false) }
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var showLoginAccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Student") },
                                onClick = {
                                    showMenu = false
                                    onEditClick()
                                }
                            )
                            if (uiState.enrollment?.active == true) {
                                DropdownMenuItem(
                                    text = { Text("Transfer Student") },
                                    onClick = {
                                        showMenu = false
                                        onTransferClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("End Enrollment", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        showEndEnrollmentDialog = true
                                    }
                                )
                            }
                            
                            if (uiState.student?.active == true) {
                                DropdownMenuItem(
                                    text = { Text("Delete Student", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        showDeactivateDialog = true
                                    }
                                )
                            }
                            
                            if (uiState.user != null) {
                                val isUserActive = uiState.user!!.active
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            if (isUserActive) "Disable Login Access" else "Enable Login Access", 
                                            color = if (isUserActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        ) 
                                    },
                                    onClick = {
                                        showMenu = false
                                        showLoginAccessDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (showEndEnrollmentDialog) {
            AlertDialog(
                onDismissRequest = { showEndEnrollmentDialog = false },
                title = { Text("End Enrollment?") },
                text = { Text("This will remove the student from their current active class and section. Are you sure you want to end their enrollment for this academic year?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showEndEnrollmentDialog = false
                            viewModel.endEnrollment()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("End Enrollment")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndEnrollmentDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        if (showDeactivateDialog) {
            AlertDialog(
                onDismissRequest = { showDeactivateDialog = false },
                title = { Text("Delete Student?") },
                text = { Text("This will permanently delete the student from the system and end any active enrollments. They will no longer be able to log in. Are you sure?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeactivateDialog = false
                            viewModel.deleteStudent(onSuccess = { onBackClick() })
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeactivateDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        if (showLoginAccessDialog && uiState.user != null) {
            val isUserActive = uiState.user!!.active
            AlertDialog(
                onDismissRequest = { showLoginAccessDialog = false },
                title = { Text(if (isUserActive) "Disable Login Access?" else "Enable Login Access?") },
                text = { 
                    Text(
                        if (isUserActive) 
                            "This will prevent the student from logging into the portal, but they will remain active in the system (e.g., for suspensions). Are you sure?"
                        else
                            "This will restore the student's ability to log into the portal. Are you sure?"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLoginAccessDialog = false
                            viewModel.updateUserStatus(!isUserActive)
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (isUserActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (isUserActive) "Disable" else "Enable")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLoginAccessDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.retry() }) {
                        Text("Retry")
                    }
                }
            }
        } else {
            val student = uiState.student
            if (student != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Section
                    StudentHeader(student)

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    // Academic Status Section
                    Text(
                        text = "CURRENT ACADEMIC STATUS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    val enrollment = uiState.enrollment
                    if (enrollment != null && enrollment.active) {
                        AcademicStatusCard(
                            academicYear = uiState.academicYear?.yearName ?: "-",
                            className = uiState.schoolClass?.className ?: "-",
                            sectionName = uiState.section?.sectionName ?: "-",
                            rollNumber = enrollment.rollNumber ?: "-"
                        )
                    } else {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = "Not currently enrolled",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = onEnrollClick) {
                                Text("Enroll Student")
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    // Navigation Rows
                    NavigationRow(title = "Personal Information", onClick = onPersonalClick)
                    NavigationRow(title = "Academic Enrollment", onClick = onEnrollmentClick)
                    NavigationRow(title = "Parents & Guardians", onClick = onParentsClick)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun StudentHeader(student: Student) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val initials = "${student.firstName.firstOrNull() ?: ""}${student.lastName.firstOrNull() ?: ""}".uppercase()
        
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "${student.firstName} ${student.lastName}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Registration: ${student.registrationNumber}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val statusText = if (student.active) "Active" else "Inactive"
        val statusColor = if (student.active) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
        
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
    }
}

@Composable
private fun AcademicStatusCard(
    academicYear: String,
    className: String,
    sectionName: String,
    rollNumber: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            StatusRow(label = "Academic Year", value = academicYear)
            Spacer(modifier = Modifier.height(8.dp))
            StatusRow(label = "Class", value = className)
            Spacer(modifier = Modifier.height(8.dp))
            StatusRow(label = "Section", value = sectionName)
            Spacer(modifier = Modifier.height(8.dp))
            StatusRow(label = "Roll Number", value = rollNumber)
        }
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

@Composable
private fun NavigationRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
