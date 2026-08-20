package com.school.gdsportal.ui.admin.teachers.information

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherPersonalScreen(
    isReadOnly: Boolean = false,
    viewModel: TeacherPersonalViewModel,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit // Kept for signature compatibility but not used in UI
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Information") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = uiState.error ?: "", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.retry() }) {
                        Text("Retry")
                    }
                }
            } else {
                val teacher = uiState.teacher
                if (teacher != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        InfoCard("Identity") {
                            InfoRow("Employee ID", teacher.employeeId)
                            InfoRow("First Name", teacher.firstName)
                            InfoRow("Last Name", teacher.lastName)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        InfoCard("Contact") {
                            InfoRow("Phone", teacher.phone ?: "Not provided")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        InfoCard("Personal") {
                            InfoRow("Gender", teacher.gender.lowercase().replaceFirstChar { it.uppercase() })
                            InfoRow("Date of Birth", teacher.dateOfBirth ?: "Not provided")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        InfoCard("Employment") {
                            InfoRow("Hire Date", teacher.hireDate)
                            InfoRow("Qualification", teacher.qualification ?: "Not provided")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        InfoCard("Account Status") {
                            InfoRow("Status", if (teacher.active) "Active" else "Inactive")
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}
