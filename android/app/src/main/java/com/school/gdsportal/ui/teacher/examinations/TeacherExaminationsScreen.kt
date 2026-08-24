package com.school.gdsportal.ui.teacher.examinations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.Examination
import com.school.gdsportal.ui.theme.AccentTeacher
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherExaminationsScreen(
    viewModel: TeacherExaminationsViewModel,
    onBackClick: () -> Unit,
    onCreateClick: (Int?) -> Unit,
    onEditClick: (Int, Long) -> Unit,
    onDetailClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Examinations")
                        Text(
                            text = "Manage class assessments",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        floatingActionButton = {
            if (!uiState.isLoading && uiState.assignedClasses.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { onCreateClick(uiState.selectedSection?.sectionId) },
                    containerColor = AccentTeacher,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Examination")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Filter Dropdown
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Filter by Class", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentTeacher)
                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedSection?.let { "${it.className} - ${it.sectionName}" } ?: "Loading classes...",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                focusedBorderColor = AccentTeacher,
                                focusedLabelColor = AccentTeacher
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            uiState.assignedClasses.forEach { section ->
                                DropdownMenuItem(
                                    text = { Text("${section.className} - ${section.sectionName}") },
                                    onClick = {
                                        viewModel.onSectionSelected(section)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentTeacher)
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
            } else if (uiState.examinations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No examinations found for this section.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        Text(
                            text = "ALL EXAMINATIONS (${uiState.examinations.size})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                    }

                    items(uiState.examinations) { exam ->
                        val subjectName = uiState.availableSubjects.find { it.subjectId == exam.subjectId }?.subjectName ?: "Subject ${exam.subjectId}"
                        ExaminationCard(
                            exam = exam,
                            subjectName = subjectName,
                            onDetailClick = { onDetailClick(exam.examinationId) },
                            onEditClick = {
                                uiState.selectedSection?.sectionId?.let { secId ->
                                    onEditClick(secId, exam.examinationId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExaminationCard(exam: Examination, subjectName: String, onDetailClick: () -> Unit, onEditClick: () -> Unit) {
    val statusColor = when (exam.status?.name) {
        "PUBLISHED" -> Color(0xFF4CAF50)
        "SCHEDULED" -> Color(0xFF2196F3)
        "ONGOING" -> Color(0xFFFF9800)
        "COMPLETED" -> Color(0xFF9E9E9E)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onDetailClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(20.dp) // Premium corner shape
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Enlarged 56dp Icon Block to match Assignments
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AccentTeacher.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.EventNote,
                    contentDescription = "Exam Icon",
                    tint = AccentTeacher,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exam.examName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subjectName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Direct Edit Button (Replaced the Dropdown Menu)
                    Box(modifier = Modifier.offset(x = 12.dp, y = (-12).dp)) {
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit Examination", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(exam.status?.name ?: "UNKNOWN", color = statusColor, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Marks: ${exam.passingMarks ?: 0.0} / ${exam.maxMarks}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                val dateStr = try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val parsed = inputFormat.parse(exam.examDate)
                    if (parsed != null) outputFormat.format(parsed) else exam.examDate
                } catch (e: Exception) {
                    exam.examDate
                }

                val startTimeStr = exam.startTime?.take(5) ?: "--:--"
                val endTimeStr = exam.endTime?.take(5) ?: "--:--"

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = "Date", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = dateStr, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(Icons.Outlined.AccessTime, contentDescription = "Time", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "$startTimeStr - $endTimeStr", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}