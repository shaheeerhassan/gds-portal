package com.school.gdsportal.ui.teacher.classteacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.ClassTeacherAssignment
import com.school.gdsportal.ui.theme.AccentTeacher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherClassTeacherScreen(
    viewModel: TeacherClassTeacherViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Class Teacher")
                        Text(
                            text = "Your homeroom assignments",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentTeacher)
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadClassTeacherHistory() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeacher)
                    ) { Text("Retry") }
                }
            } else if (uiState.currentAssignment == null && uiState.pastAssignments.isEmpty()) {
                Text(
                    text = "You have no class teacher assignments.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Current Assignment Section
                    if (uiState.currentAssignment != null) {
                        item {
                            Text(
                                text = "CURRENT ASSIGNMENT",
                                style = MaterialTheme.typography.labelMedium,
                                color = AccentTeacher,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                            ClassTeacherCard(
                                assignment = uiState.currentAssignment!!,
                                isCurrent = true
                            )
                        }
                    }

                    // Past Assignments Section
                    if (uiState.pastAssignments.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "PREVIOUS ASSIGNMENTS",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                        }
                        items(uiState.pastAssignments) { pastAssignment ->
                            ClassTeacherCard(
                                assignment = pastAssignment,
                                isCurrent = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassTeacherCard(assignment: ClassTeacherAssignment, isCurrent: Boolean) {
    // Note: Assuming your ClassTeacherAssignment model has class and section display names.
    // If it only has IDs, you may need to map them to strings.
    val cardColor = if (isCurrent) AccentTeacher.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val iconColor = if (isCurrent) AccentTeacher else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
        color = cardColor
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (isCurrent) AccentTeacher.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = "Class Teacher Icon",
                    tint = iconColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // If your DTO has className/sectionName, use them. Otherwise, fallback to IDs.
                Text(
                    text = "Section ID: ${assignment.sectionId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Academic Year ID: ${assignment.academicYearId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
