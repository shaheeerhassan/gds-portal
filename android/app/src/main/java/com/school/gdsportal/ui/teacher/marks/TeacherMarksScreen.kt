package com.school.gdsportal.ui.teacher.marks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.school.gdsportal.data.remote.MarkDisplay
import com.school.gdsportal.ui.admin.teaching.teacherclasses.FilterDropdown
import com.school.gdsportal.ui.theme.AccentTeacher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherMarksScreen(
    viewModel: TeacherMarksViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Error", color = MaterialTheme.colorScheme.error) },
            text = { Text(uiState.error!!) },
            confirmButton = { TextButton(onClick = { viewModel.dismissError() }) { Text("OK", color = AccentTeacher) } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Marks & Grading")
                        Text(
                            text = "Record student performance",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // Premium Header Selectors
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Exam Context", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentTeacher)
                    Spacer(modifier = Modifier.height(12.dp))

                    FilterDropdown(
                        label = "My Class Section",
                        items = uiState.teacherClasses.map { it.sectionId to "${it.className} - ${it.sectionName}" },
                        selectedId = uiState.selectedSectionId,
                        onSelect = { viewModel.selectSection(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FilterDropdown(
                        label = "Examination",
                        items = uiState.examinations.map { it.examinationId.toInt() to it.examName },
                        selectedId = uiState.selectedExaminationId?.toInt(),
                        onSelect = { viewModel.selectExamination(it.toLong()) },
                        enabled = uiState.selectedSectionId != null && uiState.examinations.isNotEmpty()
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading && uiState.marks.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentTeacher)
                } else if (uiState.marks.isEmpty() && uiState.selectedExaminationId != null) {
                    Text(
                        text = "No students found for this examination.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (uiState.marks.isNotEmpty()) {
                    var selectedMarkForGrading by remember { mutableStateOf<MarkDisplay?>(null) }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "STUDENT MARKS (${uiState.marks.size})",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                        }

                        items(uiState.marks) { mark ->
                            TeacherMarkCard(mark = mark, onClick = { selectedMarkForGrading = mark })
                        }
                    }

                    // -------------------------------------------------------------
                    // THE NEW PREMIUM GRADING POP-UP DIALOG
                    // -------------------------------------------------------------
                    if (selectedMarkForGrading != null) {
                        val markToGrade = selectedMarkForGrading!!
                        var obtainedInput by remember { mutableStateOf(if (markToGrade.marksObtained != "—") markToGrade.marksObtained.substringBefore(" /").trim() else "") }
                        var gradeInput by remember { mutableStateOf(markToGrade.grade) }
                        var remarksInput by remember { mutableStateOf(markToGrade.remarks) }

                        Dialog(onDismissRequest = { selectedMarkForGrading = null }) {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Text(
                                        text = "Grade Student",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentTeacher
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Student Info
                                    Text(markToGrade.studentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Text("Reg No: ${markToGrade.registrationNumber}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Input Fields
                                    OutlinedTextField(
                                        value = obtainedInput,
                                        onValueChange = { obtainedInput = it },
                                        label = { Text("Marks Obtained") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = gradeInput,
                                        onValueChange = { gradeInput = it },
                                        label = { Text("Grade (e.g. A, B, Pass)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = remarksInput,
                                        onValueChange = { remarksInput = it },
                                        label = { Text("Remarks (Optional)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Action Buttons
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(onClick = { selectedMarkForGrading = null }) {
                                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                viewModel.updateMark(
                                                    markId = markToGrade.markId,
                                                    studentId = markToGrade.studentId,
                                                    examinationId = uiState.selectedExaminationId!!,
                                                    obtained = obtainedInput.toDoubleOrNull(), // Safely passes null if blank
                                                    grade = gradeInput,
                                                    remarks = remarksInput
                                                )
                                                selectedMarkForGrading = null
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeacher)
                                        ) {
                                            Text("Save Marks")
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

@Composable
fun TeacherMarkCard(mark: MarkDisplay, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Premium Student Avatar Block
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AccentTeacher.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Student Icon",
                    tint = AccentTeacher
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mark.studentName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = mark.registrationNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = mark.marksObtained,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (mark.marksObtained == "—") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                )
                if (mark.grade.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentTeacher.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = mark.grade,
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentTeacher,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}