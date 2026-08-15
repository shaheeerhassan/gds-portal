package com.school.gdsportal.ui.admin.principals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrincipalProfileScreen(
    viewModel: PrincipalProfileViewModel,
    onBackClick: () -> Unit,
    onEditPrincipalClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var expandedMenu by remember { mutableStateOf(false) }
    var showDeactivateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.deactivateSuccess) {
        if (uiState.deactivateSuccess) {
            onBackClick() // Navigate back to directory
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Principal Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { expandedMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Principal") },
                            onClick = {
                                expandedMenu = false
                                onEditPrincipalClick()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Deactivate Principal", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                expandedMenu = false
                                showDeactivateDialog = true
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        
        if (showDeactivateDialog) {
            AlertDialog(
                onDismissRequest = { showDeactivateDialog = false },
                title = { Text("Deactivate Principal?") },
                text = { Text("This will deactivate the principal and disable their access to the portal. Their records will remain available.") },
                confirmButton = {
                    Button(
                        onClick = { 
                            showDeactivateDialog = false
                            viewModel.deactivatePrincipal()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        if (uiState.isDeactivating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onError)
                        } else {
                            Text("Deactivate")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeactivateDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadPrincipalProfile() }) {
                        Text("Retry")
                    }
                }
            } else if (uiState.principal != null) {
                val principal = uiState.principal!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Avatar
                    val initials = "${principal.firstName.firstOrNull() ?: ""}${principal.lastName.firstOrNull() ?: ""}".uppercase()
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Name & ID
                    Text(
                        text = "${principal.firstName} ${principal.lastName}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = principal.employeeId,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val statusText = if (principal.isActive) "Active" else "Inactive"
                    val statusColor = if (principal.isActive) androidx.compose.ui.graphics.Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    
                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = statusText,
                            color = statusColor,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Personal Information Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "PERSONAL INFORMATION",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                InfoRow(label = "First Name", value = principal.firstName)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                InfoRow(label = "Last Name", value = principal.lastName)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                InfoRow(
                                    label = "Phone",
                                    value = if (principal.phone.isNotBlank()) principal.phone else "Not provided"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Employment Information Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "EMPLOYMENT INFORMATION",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                InfoRow(label = "Employee ID", value = principal.employeeId)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            if (uiState.deactivateError != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.dismissDeactivateError() }) {
                            Text("Dismiss", color = MaterialTheme.colorScheme.inversePrimary)
                        }
                    }
                ) {
                    Text(uiState.deactivateError!!)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label, 
            style = MaterialTheme.typography.bodyMedium, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyLarge, 
            fontWeight = FontWeight.Medium
        )
    }
}
