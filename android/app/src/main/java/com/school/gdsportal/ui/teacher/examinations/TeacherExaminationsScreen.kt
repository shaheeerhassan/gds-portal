package com.school.gdsportal.ui.teacher.examinations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.Examination
import com.school.gdsportal.ui.theme.AccentTeacher
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherExaminationsScreen(
    viewModel: TeacherExaminationsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Examinations") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentTeacher)
            } else if (uiState.error != null) {
                Button(onClick = { viewModel.loadData() }, modifier = Modifier.align(Alignment.Center), colors = ButtonDefaults.buttonColors(containerColor = AccentTeacher)) { Text("Retry") }
            } else if (uiState.examinations.isEmpty()) {
                Text("No examinations found for your classes.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.examinations) { exam ->
                        ExaminationCard(exam = exam)
                    }
                }
            }
        }
    }
}

@Composable
fun ExaminationCard(exam: Examination) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(exam.examName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentTeacher)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Subject ID: ${exam.subjectId}", style = MaterialTheme.typography.bodyMedium)
                Text("Max Marks: ${exam.maxMarks}", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            val dateStr = try {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                format.format(exam.examDate)
            } catch (e: Exception) { exam.examDate.toString() }
            Text("Date: $dateStr", style = MaterialTheme.typography.bodySmall)
        }
    }
}

