package com.school.gdsportal.ui.teacher.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.school.gdsportal.ui.theme.AccentTeacher
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherProfileScreen(
    viewModel: TeacherProfileViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!uiState.isLoading && uiState.error == null && !uiState.isEditing) {
                        IconButton(onClick = { viewModel.startEditing() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = AccentTeacher)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
                    Button(onClick = { viewModel.loadProfile() }, colors = ButtonDefaults.buttonColors(containerColor = AccentTeacher)) {
                        Text("Retry")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar Header
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(AccentTeacher.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            style = MaterialTheme.typography.displayLarge,
                            color = AccentTeacher,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = uiState.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(text = uiState.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(24.dp))

                    // Status Messages
                    if (uiState.saveSuccess) {
                        StatusCard(
                            message = "Profile updated successfully!",
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.15f),
                            contentColor = Color(0xFF388E3C),
                            onDismiss = { viewModel.dismissSaveSuccess() }
                        )
                    }
                    if (uiState.saveError != null) {
                        StatusCard(
                            message = uiState.saveError!!,
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            onDismiss = { viewModel.dismissSaveError() }
                        )
                    }

                    // Content Area
                    if (uiState.isEditing) {
                        EditProfileForm(uiState, viewModel)
                    } else {
                        ViewProfileDetails(uiState)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusCard(message: String, containerColor: Color, contentColor: Color, onDismiss: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(message, color = contentColor, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = contentColor)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileForm(uiState: TeacherProfileUiState, viewModel: TeacherProfileViewModel) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val formattedDate = formatter.format(Date(millis))
                            viewModel.updateEditField("dateOfBirth", formattedDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = AccentTeacher)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            colors = DatePickerDefaults.colors(
                titleContentColor = AccentTeacher,
                headlineContentColor = AccentTeacher,
                selectedDayContainerColor = AccentTeacher,
                todayDateBorderColor = AccentTeacher
            )
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Edit Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentTeacher)

            OutlinedTextField(
                value = uiState.editUsername,
                onValueChange = { viewModel.updateEditField("username", it) },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.editFirstName,
                    onValueChange = { viewModel.updateEditField("firstName", it) },
                    label = { Text("First Name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
                )
                OutlinedTextField(
                    value = uiState.editLastName,
                    onValueChange = { viewModel.updateEditField("lastName", it) },
                    label = { Text("Last Name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
                )
            }

            OutlinedTextField(
                value = uiState.editPhone,
                onValueChange = { viewModel.updateEditField("phone", it) },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.editGender,
                    onValueChange = { viewModel.updateEditField("gender", it) },
                    label = { Text("Gender") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
                )

                // Clickable overlay pattern for Date Picker
                Box(modifier = Modifier.weight(1.2f)) {
                    OutlinedTextField(
                        value = uiState.editDateOfBirth,
                        onValueChange = { },
                        label = { Text("DOB") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = true, // Prevents keyboard from popping up
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Select Date",
                                tint = AccentTeacher
                            )
                        }
                    )
                    // This invisible box catches all clicks over the text field
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Transparent)
                            .clickable { showDatePicker = true }
                    )
                }
            }

            OutlinedTextField(
                value = uiState.editQualification,
                onValueChange = { viewModel.updateEditField("qualification", it) },
                label = { Text("Qualification") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { viewModel.cancelEditing() }, enabled = !uiState.isSaving) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.saveProfile() },
                    enabled = !uiState.isSaving && uiState.isFormValid,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeacher)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
fun ViewProfileDetails(uiState: TeacherProfileUiState) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {

        ProfileSectionCard(title = "Account & Contact") {
            ProfileItemRow(label = "Username", value = uiState.username)
            ProfileItemRow(label = "Phone", value = uiState.phone.ifBlank { "Not provided" })
        }

        ProfileSectionCard(title = "Personal Details") {
            ProfileItemRow(label = "Gender", value = uiState.gender)
            ProfileItemRow(label = "Date of Birth", value = uiState.dateOfBirth.ifBlank { "Not provided" })
        }

        ProfileSectionCard(title = "Employment Information") {
            ProfileItemRow(label = "Employee ID", value = uiState.employeeId)
            ProfileItemRow(label = "Hire Date", value = uiState.hireDate)
            ProfileItemRow(label = "Qualification", value = uiState.qualification.ifBlank { "Not provided" })
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun ProfileSectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentTeacher)
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            content()
        }
    }
}

@Composable
private fun ProfileItemRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}