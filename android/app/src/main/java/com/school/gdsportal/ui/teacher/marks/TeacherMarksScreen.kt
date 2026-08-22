package com.school.gdsportal.ui.teacher.marks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.MarkDisplay
import com.school.gdsportal.ui.admin.teaching.teacherclasses.FilterDropdown
import com.school.gdsportal.ui.theme.AccentTeacher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherMarksScreen(
    viewModel: TeacherMarksViewModel,
    onMenuClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Error") },
            text = { Text(uiState.error!!) },
            confirmButton = { TextButton(onClick = { viewModel.dismissError() }) { Text("OK", color = AccentTeacher) } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Marks & Grading") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, contentDescription = "Menu") }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                // Class/Section selector
                FilterDropdown(
                    label = "My Class Section",
                    items = uiState.teacherClasses.map { it.sectionId to "${it.className} - ${it.sectionName}" },
                    selectedId = uiState.selectedSectionId,
                    onSelect = { viewModel.selectSection(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Examination selector
                FilterDropdown(
                    label = "Examination",
                    items = uiState.examinations.map { it.examinationId.toInt() to it.examName },
                    selectedId = uiState.selectedExaminationId?.toInt(),
                    onSelect = { viewModel.selectExamination(it.toLong()) },
                    enabled = uiState.selectedSectionId != null && uiState.examinations.isNotEmpty()
                )
            }

            HorizontalDivider()

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading && uiState.marks.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentTeacher)
                } else if (uiState.marks.isEmpty() && uiState.selectedExaminationId != null) {
                    Text(
                        text = "No marks found for this examination.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.marks) { mark ->
                            TeacherMarkItem(mark = mark, onClick = { /* View Only for now in phase 1 */ })
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherMarkItem(mark: MarkDisplay, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mark.studentName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = mark.registrationNumber,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mark.marksObtained,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (mark.grade.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .background(AccentTeacher.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = mark.grade,
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentTeacher,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
