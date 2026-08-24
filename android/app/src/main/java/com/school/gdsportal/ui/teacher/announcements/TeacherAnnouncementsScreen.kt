package com.school.gdsportal.ui.teacher.announcements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.Announcement
import com.school.gdsportal.ui.theme.AccentTeacher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAnnouncementsScreen(
    viewModel: TeacherAnnouncementsViewModel,
    onBackClick: () -> Unit,
    onCreateClick: () -> Unit,
    onAnnouncementClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Error", color = MaterialTheme.colorScheme.error) },
            text = { Text(uiState.error!!) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissError()
                    viewModel.loadAnnouncements()
                }) { Text("Retry", color = AccentTeacher) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissError() }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Announcements") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = AccentTeacher,
                contentColor = MaterialTheme.colorScheme.surface
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Announcement")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentTeacher)
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        label = { Text("Search announcements") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeacher, focusedLabelColor = AccentTeacher),
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )

                    if (uiState.announcements.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No announcements yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Create an announcement to communicate.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else if (uiState.filteredAnnouncements.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No announcements found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
                            items(uiState.filteredAnnouncements) { announcement ->
                                TeacherAnnouncementRow(
                                    announcement = announcement,
                                    onClick = { onAnnouncementClick(announcement.announcementId) }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherAnnouncementRow(announcement: Announcement, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = AccentTeacher,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = announcement.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!announcement.isActive) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Disabled") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            labelColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                val targetText = when {
                    announcement.targetRoleId != null -> {
                        when (announcement.targetRoleId) {
                            1 -> "Role: Administrator"
                            2 -> "Role: Principal"
                            3 -> "Role: Teacher"
                            4 -> "Role: Student"
                            5 -> "Role: Parent"
                            else -> "Role ID: ${announcement.targetRoleId}"
                        }
                    }
                    announcement.sectionId != null -> "Section Audience"
                    announcement.classId != null -> "Class Audience"
                    else -> "Global"
                }
                Text(
                    text = targetText,
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentTeacher,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "View Details",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}