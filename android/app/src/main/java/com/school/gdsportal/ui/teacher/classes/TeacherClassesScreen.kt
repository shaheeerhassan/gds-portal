package com.school.gdsportal.ui.teacher.classes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.TeacherClassDTO
import com.school.gdsportal.ui.theme.AccentTeacher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherClassesScreen(
    viewModel: TeacherClassesViewModel,
    onMenuClick: () -> Unit,
    onClassClick: (Int) -> Unit // Passes the sectionId to the next screen
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Classes")
                        Text(
                            text = "Classes and sections assigned to you",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentTeacher
                )
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadMyClasses() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeacher)
                    ) { Text("Retry") }
                }
            } else if (uiState.assignedClasses.isEmpty()) {
                Text(
                    text = "You are not assigned to any classes yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.assignedClasses) { assignedClass ->
                        AssignedClassCard(
                            assignedClass = assignedClass,
                            onClick = { onClassClick(assignedClass.sectionId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignedClassCard(
    assignedClass: TeacherClassDTO,
    onClick: () -> Unit
) {
    // A visual indicator for the grade level (e.g., "8" from "Grade 8")
    val gradeLevel = assignedClass.className.replace(Regex("[^0-9]"), "").ifEmpty {
        assignedClass.className.firstOrNull()?.toString() ?: "-"
    }

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
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(AccentTeacher.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = gradeLevel,
                    color = AccentTeacher,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = assignedClass.className,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Section ${assignedClass.sectionName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Students",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}