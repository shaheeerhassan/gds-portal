package com.school.gdsportal.ui.teacher.notifications

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
import com.school.gdsportal.ui.theme.AccentTeacher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherNotificationsScreen(
    viewModel: TeacherNotificationsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { viewModel.markAllAsRead() }) {
                        Text("Read All", color = AccentTeacher, style = MaterialTheme.typography.labelMedium)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentTeacher)
            } else if (uiState.error != null) {
                Button(onClick = { viewModel.loadData() }, modifier = Modifier.align(Alignment.Center), colors = ButtonDefaults.buttonColors(containerColor = AccentTeacher)) { Text("Retry") }
            } else if (uiState.notifications.isEmpty()) {
                Text("No notifications.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.notifications) { notification ->
                        val cardColor = if (!notification.isRead) AccentTeacher.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.markAsRead(notification.notificationId) },
                            colors = CardDefaults.cardColors(containerColor = cardColor)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(notification.title, style = MaterialTheme.typography.titleMedium, fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal)
                                    if (!notification.isRead) {
                                        Badge(containerColor = AccentTeacher)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(notification.message, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

