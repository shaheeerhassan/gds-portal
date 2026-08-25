package com.school.gdsportal.ui.student.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.ui.theme.AccentStudent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    viewModel: StudentProfileViewModel,
    onMenuClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.successMessage, uiState.error) {
        if (uiState.successMessage != null || uiState.error != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Menu") }
                },
                colors = TopAppBarDefaults.topAppBarColors(titleContentColor = AccentStudent)
            )
        },
        bottomBar = {
            if (!uiState.isLoading && uiState.student != null) {
                Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                    Button(
                        onClick = { viewModel.saveProfile() },
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentStudent),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Save Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentStudent)
            } else if (uiState.student != null && uiState.user != null) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Header Avatar
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape).background(AccentStudent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            // FIX: Pull initials from student
                            val initials = "${uiState.student!!.firstName?.firstOrNull() ?: ""}${uiState.student!!.lastName?.firstOrNull() ?: ""}"
                            Text(initials.uppercase(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = AccentStudent)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("${uiState.student!!.firstName} ${uiState.student!!.lastName}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Student · ${uiState.className} · ${uiState.sectionName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    if (uiState.successMessage != null) {
                        Text(uiState.successMessage!!, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    }
                    if (uiState.error != null) {
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }

                    // Personal Information (Editable)
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Personal Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        OutlinedTextField(
                            value = uiState.firstName,
                            onValueChange = { viewModel.onFirstNameChange(it) },
                            label = { Text("First Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentStudent, focusedLabelColor = AccentStudent)
                        )

                        OutlinedTextField(
                            value = uiState.lastName,
                            onValueChange = { viewModel.onLastNameChange(it) },
                            label = { Text("Last Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentStudent, focusedLabelColor = AccentStudent)
                        )

                        OutlinedTextField(
                            value = uiState.dateOfBirth,
                            onValueChange = { viewModel.onDateOfBirthChange(it) },
                            label = { Text("Date of Birth (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentStudent, focusedLabelColor = AccentStudent)
                        )

                        // Gender Dropdown
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                                value = uiState.gender,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Gender") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentStudent, focusedLabelColor = AccentStudent)
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                listOf("MALE", "FEMALE", "OTHER").forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            viewModel.onGenderChange(selectionOption)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Academic Information (Read-Only)
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Academic Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                ReadOnlyField("Email", uiState.user!!.email ?: uiState.user!!.username ?: "N/A")
                                ReadOnlyField("Registration Number", uiState.registrationNumber)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    ReadOnlyField("Class", uiState.className)
                                    ReadOnlyField("Section", uiState.sectionName)
                                    ReadOnlyField("Roll No.", uiState.rollNumber)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ReadOnlyField(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}