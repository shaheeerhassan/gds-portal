package com.school.gdsportal.ui.admin.communication.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.school.gdsportal.ui.admin.teaching.teacherclasses.FilterDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCreateScreen(
    viewModel: NotificationCreateViewModel,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSuccess()
            viewModel.resetSuccess()
        }
    }

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Error") },
            text = { Text(uiState.error!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) {
                    Text("OK")
                }
            }
        )
    }

    val roles = listOf(
        1 to "Administrator",
        2 to "Principal",
        3 to "Teacher",
        4 to "Student",
        5 to "Parent"
    )

    val notificationTypes = listOf(
        "NEW_ANNOUNCEMENT" to "New Announcement",
        "NEW_MESSAGE" to "New Message",
        "NEW_ASSIGNMENT" to "New Assignment",
        "RESULT_PUBLISHED" to "Result Published",
        "ATTENDANCE_ALERT" to "Attendance Alert"
    )

    var typeDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send Notification") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Send To (Single vs Bulk)
                Text(
                    text = "RECIPIENT AUDIENCE",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.recipientMode == NotificationRecipientMode.SINGLE,
                        onClick = { viewModel.onRecipientModeChanged(NotificationRecipientMode.SINGLE) },
                        label = { Text("Single Recipient") }
                    )
                    FilterChip(
                        selected = uiState.recipientMode == NotificationRecipientMode.BULK_ROLE,
                        onClick = { viewModel.onRecipientModeChanged(NotificationRecipientMode.BULK_ROLE) },
                        label = { Text("Multiple (By Role)") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Role Filter / Selector
                FilterDropdown(
                    label = if (uiState.recipientMode == NotificationRecipientMode.SINGLE) "Filter by Role *" else "Select Target Role *",
                    items = roles,
                    selectedId = uiState.selectedRoleId,
                    onSelect = { viewModel.onRoleSelected(it) }
                )

                if (uiState.isLoadingUsers) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                // If Single Recipient, show User Dropdown
                if (uiState.recipientMode == NotificationRecipientMode.SINGLE && uiState.selectedRoleId != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    val userItems = uiState.users.map { user ->
                        val name = user.username ?: "User #${user.userId}"
                        user.userId.toInt() to (if (user.email.isNotBlank()) "$name (${user.email})" else name)
                    }
                    FilterDropdown(
                        label = "Select Recipient *",
                        items = userItems,
                        selectedId = uiState.selectedUserId?.toInt(),
                        onSelect = { viewModel.onUserSelected(it.toLong()) },
                        enabled = uiState.users.isNotEmpty()
                    )
                    if (uiState.users.isEmpty() && !uiState.isLoadingUsers) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No users found in this role.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else if (uiState.recipientMode == NotificationRecipientMode.BULK_ROLE && uiState.selectedRoleId != null && !uiState.isLoadingUsers) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = "Will send to ${uiState.users.size} user(s) in selected role.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "NOTIFICATION CONTENT",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Notification Type Dropdown
                Box {
                    val currentTypeLabel = notificationTypes.find { it.first == uiState.selectedType }?.second ?: "New Announcement"
                    OutlinedTextField(
                        value = currentTypeLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Notification Type *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { typeDropdownExpanded = true },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        enabled = false,
                        singleLine = true
                    )
                    DropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        notificationTypes.forEach { (type, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.onTypeSelected(type)
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onTitleChanged(it) },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.message,
                    onValueChange = { viewModel.onMessageChanged(it) },
                    label = { Text("Message *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    minLines = 4
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.submit() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSubmitting
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Send Notification")
                    }
                }
            }
        }
    }
}
