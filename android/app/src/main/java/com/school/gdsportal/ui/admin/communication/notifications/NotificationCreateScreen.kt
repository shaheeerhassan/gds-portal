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
                TextButton(onClick = { viewModel.dismissError() }) { Text("OK") }
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
                // ── Mode chips ──────────────────────────────────────────────
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
                        selected = uiState.recipientMode == NotificationRecipientMode.BROADCAST,
                        onClick = { viewModel.onRecipientModeChanged(NotificationRecipientMode.BROADCAST) },
                        label = { Text("Broadcast") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.recipientMode == NotificationRecipientMode.SINGLE) {
                    SingleRecipientSection(uiState = uiState, viewModel = viewModel, roles = roles)
                } else {
                    BroadcastSection(uiState = uiState, viewModel = viewModel, roles = roles)
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // ── Notification Content ─────────────────────────────────────
                Text(
                    text = "NOTIFICATION CONTENT",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

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
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Send Notification")
                    }
                }
            }
        }
    }
}

// ── Single Recipient sub-composable ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleRecipientSection(
    uiState: NotificationCreateUiState,
    viewModel: NotificationCreateViewModel,
    roles: List<Pair<Int, String>>
) {
    // Role picker
    FilterDropdown(
        label = "Select Role *",
        items = roles,
        selectedId = uiState.selectedRoleId,
        onSelect = { viewModel.onRoleSelected(it) }
    )

    Spacer(modifier = Modifier.height(16.dp))

    when (uiState.selectedRoleId) {
        // ── Admin or Principal: plain dropdown ──────────────────────────────
        1, 2 -> {
            if (uiState.isLoadingListedUsers) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else if (uiState.listedUsers.isNotEmpty()) {
                val items = uiState.listedUsers.map { (id, name) -> id.toInt() to name }
                FilterDropdown(
                    label = "Select Recipient *",
                    items = items,
                    selectedId = uiState.selectedListedUserId?.toInt(),
                    onSelect = { viewModel.onListedUserSelected(it.toLong()) }
                )
            } else {
                Text(
                    "No users found.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // ── Teacher: autocomplete name search ───────────────────────────────
        3 -> {
            SearchAutocomplete(
                label = "Search Teacher by Name (min 2 chars) *",
                query = uiState.searchQuery,
                results = uiState.searchResults,
                isLoading = uiState.isSearching,
                onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                onResultSelected = { id, name -> viewModel.onUserSelected(id, name) }
            )
        }

        // ── Student: Class → Section → Student search ───────────────────────
        4 -> {
            ClassSectionStudentPicker(
                uiState = uiState,
                viewModel = viewModel,
                searchLabel = "Search Student by Name or Reg. No. (min 2 chars) *"
            )
        }

        // ── Parent: Class → Section → Student search → notify linked parents ─
        5 -> {
            Text(
                text = "Select the student whose parents should receive the notification.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            ClassSectionStudentPicker(
                uiState = uiState,
                viewModel = viewModel,
                searchLabel = "Search Student by Name or Reg. No. (min 2 chars) *"
            )
            if (uiState.selectedStudentId != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "Notification will be sent to all parents linked to ${uiState.selectedUserName}.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        null -> {
            Text(
                "Please select a role to continue.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Class → Section → Student search ────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClassSectionStudentPicker(
    uiState: NotificationCreateUiState,
    viewModel: NotificationCreateViewModel,
    searchLabel: String
) {
    if (uiState.isLoadingClasses) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    } else {
        val classItems = uiState.classes.map { it.classId to it.className }
        FilterDropdown(
            label = "Class *",
            items = classItems,
            selectedId = uiState.selectedClassId,
            onSelect = { viewModel.onClassSelected(it) }
        )
    }

    if (uiState.selectedClassId != null) {
        Spacer(modifier = Modifier.height(16.dp))
        if (uiState.isLoadingSections) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            val sectionItems = uiState.sections.map { it.sectionId to it.sectionName }
            FilterDropdown(
                label = "Section *",
                items = sectionItems,
                selectedId = uiState.selectedSectionId,
                onSelect = { viewModel.onSectionSelected(it) },
                enabled = uiState.sections.isNotEmpty()
            )
        }
    }

    if (uiState.selectedSectionId != null) {
        Spacer(modifier = Modifier.height(16.dp))
        SearchAutocomplete(
            label = searchLabel,
            query = uiState.searchQuery,
            results = uiState.searchResults,
            isLoading = uiState.isSearching,
            onQueryChanged = { viewModel.onSearchQueryChanged(it) },
            onResultSelected = { id, name -> viewModel.onUserSelected(id, name) }
        )
    }
}

// ── Reusable autocomplete search field ──────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAutocomplete(
    label: String,
    query: String,
    results: List<Pair<Long, String>>,
    isLoading: Boolean,
    onQueryChanged: (String) -> Unit,
    onResultSelected: (Long, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && results.isNotEmpty(),
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                onQueryChanged(it)
                expanded = true
            },
            label = { Text(label) },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && results.isNotEmpty())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true),
            singleLine = true
        )
        if (results.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                results.forEach { (id, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            onResultSelected(id, name)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// ── Broadcast sub-composable ─────────────────────────────────────────────────

@Composable
private fun BroadcastSection(
    uiState: NotificationCreateUiState,
    viewModel: NotificationCreateViewModel,
    roles: List<Pair<Int, String>>
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = uiState.broadcastGlobal,
            onCheckedChange = { viewModel.onBroadcastGlobalChanged(it) }
        )
        Text("Global (All Users)")
    }

    if (!uiState.broadcastGlobal) {
        Spacer(modifier = Modifier.height(8.dp))
        Text("Or select specific roles:", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Column {
            roles.forEach { (roleId, roleName) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onBroadcastRoleToggled(roleId) }
                ) {
                    Checkbox(
                        checked = uiState.broadcastSelectedRoleIds.contains(roleId),
                        onCheckedChange = { viewModel.onBroadcastRoleToggled(roleId) }
                    )
                    Text(roleName)
                }
            }
        }
    }
}
