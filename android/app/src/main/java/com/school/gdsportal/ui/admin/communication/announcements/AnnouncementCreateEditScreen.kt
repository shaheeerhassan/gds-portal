package com.school.gdsportal.ui.admin.communication.announcements

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
fun AnnouncementCreateEditScreen(
    viewModel: AnnouncementCreateEditViewModel,
    onBackClick: () -> Unit,
    onSuccess: (Long?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSuccess(uiState.createdAnnouncementId)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Announcement" else "Create Announcement") },
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
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.onTitleChanged(it) },
                        label = { Text("Title *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.content,
                        onValueChange = { viewModel.onContentChanged(it) },
                        label = { Text("Content *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Target Audience", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    val targetTypes = listOf(
                        AnnouncementTargetType.GLOBAL to "Global",
                        AnnouncementTargetType.ROLE to "Role",
                        AnnouncementTargetType.CLASS to "Class",
                        AnnouncementTargetType.SECTION to "Section"
                    )

                    var targetDropdownExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = targetDropdownExpanded,
                        onExpandedChange = { targetDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = targetTypes.find { it.first == uiState.targetType }?.second ?: "Global",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = targetDropdownExpanded,
                            onDismissRequest = { targetDropdownExpanded = false }
                        ) {
                            targetTypes.forEach { (type, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.onTargetTypeChanged(type)
                                        targetDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    when (uiState.targetType) {
                        AnnouncementTargetType.ROLE -> {
                            val roles = listOf(
                                1 to "Administrator",
                                2 to "Principal",
                                3 to "Teacher",
                                4 to "Student",
                                5 to "Parent"
                            )
                            FilterDropdown(
                                label = "Role",
                                items = roles,
                                selectedId = uiState.selectedRoleId,
                                onSelect = { viewModel.onRoleSelected(it) }
                            )
                        }
                        AnnouncementTargetType.CLASS, AnnouncementTargetType.SECTION -> {
                            val classItems = uiState.classes.map { it.classId to it.className }
                            FilterDropdown(
                                label = "Class",
                                items = classItems,
                                selectedId = uiState.selectedClassId,
                                onSelect = { viewModel.onClassSelected(it) }
                            )

                            if (uiState.targetType == AnnouncementTargetType.SECTION) {
                                Spacer(modifier = Modifier.height(16.dp))
                                val selectedClass = uiState.classes.firstOrNull { it.classId == uiState.selectedClassId }
                                val className = selectedClass?.className ?: ""
                                val sectionItems = uiState.sections.map { 
                                    it.sectionId to if (className.isNotBlank()) "$className - ${it.sectionName}" else it.sectionName 
                                }
                                FilterDropdown(
                                    label = "Section",
                                    items = sectionItems,
                                    selectedId = uiState.selectedSectionId,
                                    onSelect = { viewModel.onSectionSelected(it) },
                                    enabled = uiState.selectedClassId != null && uiState.sections.isNotEmpty()
                                )
                            }
                        }
                        else -> {
                            // Global requires no specific selector
                        }
                    }

                    if (!uiState.isEditMode) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = uiState.notify,
                                onCheckedChange = { viewModel.onNotifyChanged(it) }
                            )
                            Text("Send push/inbox alert to target audience")
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { viewModel.submit() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSubmitting
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text(if (uiState.isEditMode) "Save Changes" else "Create Announcement")
                        }
                    }

                    if (uiState.isEditMode) {
                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.material3.OutlinedButton(
                            onClick = { viewModel.disableAnnouncement() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSubmitting,
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Disable Announcement")
                        }
                    }
                }
            }
        }
    }
}
