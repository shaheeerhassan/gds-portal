package com.school.gdsportal.ui.student.assessment.examinations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.ExaminationStatus
import com.school.gdsportal.ui.theme.AccentStudent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentExaminationsScreen(
    viewModel: StudentExaminationsViewModel,
    onMenuClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Trigger data fetch every time the screen is opened
    LaunchedEffect(Unit) {
        viewModel.loadExaminations(isRefresh = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Examinations", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, contentDescription = "Menu") }
                },
                colors = TopAppBarDefaults.topAppBarColors(titleContentColor = AccentStudent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Replaced Box with PullToRefreshBox
            androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.loadExaminations(isRefresh = true) },
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.isLoading && !uiState.isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentStudent)
                } else if (uiState.error != null) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                } else if (uiState.examinations.isEmpty()) {
                    Text("No examinations scheduled.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.examinations) { exam ->
                            val subjectName = uiState.subjectNames[exam.subjectId] ?: "Subject ID: ${exam.subjectId}"
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(exam.examName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Box(
                                            modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    when(exam.status) {
                                                        ExaminationStatus.PUBLISHED -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                                        ExaminationStatus.ONGOING -> AccentStudent.copy(alpha = 0.15f)
                                                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                                                    }
                                                ).padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = exam.status.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = when(exam.status) {
                                                    ExaminationStatus.PUBLISHED -> Color(0xFF4CAF50)
                                                    ExaminationStatus.ONGOING -> AccentStudent
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(subjectName, style = MaterialTheme.typography.bodyMedium, color = AccentStudent, fontWeight = FontWeight.Bold)

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("Date & Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${exam.examDate} · ${exam.startTime ?: "TBD"}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Max Marks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(exam.maxMarks.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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