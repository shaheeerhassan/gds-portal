package com.school.gdsportal.ui.parent.children

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// Import the parent theme color
import com.school.gdsportal.ui.theme.AccentParent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildOverviewScreen(
    viewModel: ChildOverviewViewModel,
    onBackClick: () -> Unit,
    onNavigateToExamsAndMarks: () -> Unit,
    onNavigateToAssignments: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToInfo: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Overview") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentParent // Themed Spinner
                )
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadStudentDetails() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentParent) // Themed Button
                    ) { Text("Retry") }
                }
            } else if (uiState.student != null) {
                val student = uiState.student!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Header Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AccentParent.copy(alpha = 0.15f), // Themed Surface
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val initials = "${student.firstName.firstOrNull() ?: ""}${student.lastName.firstOrNull() ?: ""}".uppercase()
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(AccentParent.copy(alpha = 0.2f)), // Themed Initials Box
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    color = AccentParent, // Themed Initials Text
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "${student.firstName} ${student.lastName}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Reg: ${student.registrationNumber}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Navigation Actions
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "ACADEMICS",
                            style = MaterialTheme.typography.labelMedium,
                            color = AccentParent, // Themed Section Header
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        OverviewNavCard(
                            title = "Examinations & Marks",
                            icon = Icons.Default.Assessment,
                            onClick = onNavigateToExamsAndMarks
                        )
                        OverviewNavCard(
                            title = "Assignments & Submissions",
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            onClick = onNavigateToAssignments
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "ATTENDANCE",
                            style = MaterialTheme.typography.labelMedium,
                            color = AccentParent, // Themed Section Header
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        OverviewNavCard(
                            title = "View Attendance Record",
                            icon = Icons.Default.FactCheck,
                            onClick = onNavigateToAttendance
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "INFORMATION",
                            style = MaterialTheme.typography.labelMedium,
                            color = AccentParent, // Themed Section Header
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        OverviewNavCard(
                            title = "Student Profile",
                            icon = Icons.Default.Person,
                            onClick = onNavigateToInfo
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewNavCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentParent // Themed Card Icon
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}