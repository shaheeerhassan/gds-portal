package com.school.gdsportal.ui.parent.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// Explicitly import your Parent theme color
import com.school.gdsportal.ui.theme.AccentParent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentProfileScreen(
    viewModel: ParentProfileViewModel,
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
                    if (uiState.parent != null && !uiState.isLoading) {
                        IconButton(onClick = { viewModel.toggleEditMode() }) {
                            Icon(
                                imageVector = if (uiState.isEditing) Icons.Default.Close else Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = AccentParent // Applied Parent Theme Color
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentParent // Applied Parent Theme Color
                )
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadProfile() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentParent)
                    ) { Text("Retry") }
                }
            } else if (uiState.parent != null) {
                val parent = uiState.parent!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Parent Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentParent // Applied Parent Theme Color
                            )
                            HorizontalDivider()

                            if (uiState.isEditing) {
                                // EDIT MODE
                                OutlinedTextField(
                                    value = uiState.editFirstName,
                                    onValueChange = { viewModel.updateField("firstName", it) },
                                    label = { Text("First Name") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = uiState.editLastName,
                                    onValueChange = { viewModel.updateField("lastName", it) },
                                    label = { Text("Last Name") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = uiState.editPhone,
                                    onValueChange = { viewModel.updateField("phone", it) },
                                    label = { Text("Phone") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = uiState.editOccupation,
                                    onValueChange = { viewModel.updateField("occupation", it) },
                                    label = { Text("Occupation") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { viewModel.saveProfile() },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isSaving && uiState.editFirstName.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentParent)
                                ) {
                                    if (uiState.isSaving) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                    } else {
                                        Text("Save Changes")
                                    }
                                }
                            } else {
                                // VIEW MODE
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("First Name", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(parent.firstName, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Last Name", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(parent.lastName, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Phone", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(parent.phone ?: "N/A", fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Occupation", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(parent.occupation ?: "N/A", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}