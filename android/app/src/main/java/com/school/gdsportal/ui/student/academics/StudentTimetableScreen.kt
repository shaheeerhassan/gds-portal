package com.school.gdsportal.ui.student.academics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.ui.theme.AccentStudent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentTimetableScreen(
    viewModel: StudentAcademicsViewModel,
    onMenuClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val daysOfWeek = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")
    var selectedDay by remember { mutableStateOf(daysOfWeek.first()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Timetable", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, contentDescription = "Menu") }
                },
                colors = TopAppBarDefaults.topAppBarColors(titleContentColor = AccentStudent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Day Selector Tabs
            ScrollableTabRow(
                selectedTabIndex = daysOfWeek.indexOf(selectedDay),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = AccentStudent,
                edgePadding = 16.dp
            ) {
                daysOfWeek.forEach { day ->
                    Tab(
                        selected = selectedDay == day,
                        onClick = { selectedDay = day },
                        text = { Text(day.take(3), fontWeight = FontWeight.Bold) },
                        selectedContentColor = AccentStudent,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentStudent)
                } else if (uiState.error != null) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                } else {
                    val dayTimetable = uiState.timetable.filter { it.dayOfWeek.equals(selectedDay, ignoreCase = true) }
                        .sortedBy { it.periodId } // Usually periods are sorted by ID or Start Time

                    if (dayTimetable.isEmpty()) {
                        Text("No classes scheduled for $selectedDay.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(dayTimetable) { period ->
                                val subjectName = uiState.subjects.find { it.subjectId == period.subjectId }?.subjectName ?: "Unknown Subject"
                                val teacher = uiState.teachers.find { it.teacherId == period.teacherId }
                                val teacherName = if (teacher != null) "${teacher.firstName} ${teacher.lastName}" else "Unknown Teacher"

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.width(60.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(AccentStudent).padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text("P${period.periodId}", color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(subjectName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(teacherName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}