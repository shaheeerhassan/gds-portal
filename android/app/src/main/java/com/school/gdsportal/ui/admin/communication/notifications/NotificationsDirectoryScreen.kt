package com.school.gdsportal.ui.admin.communication.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.Notification

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsDirectoryScreen(
    isReadOnly: Boolean = false,
    viewModel: NotificationsDirectoryViewModel,
    onBackClick: () -> Unit,
    onCreateClick: () -> Unit,
    onNotificationClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    // Error Alert Dialog
    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Error") },
            text = { Text(uiState.error!!) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissError()
                    viewModel.loadData()
                }) {
                    Text("Retry")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissError() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear All Confirmation Dialog
    if (uiState.showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showClearAllDialog(false) },
            title = { Text("Clear all notifications?") },
            text = { Text("All of your notifications will be permanently removed.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.clearAllNotifications() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showClearAllDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Notifications", style = MaterialTheme.typography.titleLarge)
                        if (uiState.unreadCount > 0) {
                            Text(
                                text = "${uiState.unreadCount} unread",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isReadOnly) {
                        IconButton(onClick = onCreateClick) {
                            Icon(Icons.Default.Add, contentDescription = "Send Notification")
                        }
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Mark all as read") },
                            onClick = {
                                showMenu = false
                                viewModel.markAllAsRead()
                            },
                            leadingIcon = { Icon(Icons.Default.DoneAll, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear all notifications", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                viewModel.showClearAllDialog(true)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Filter Tabs (All / Unread)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedFilter == NotificationFilter.ALL,
                        onClick = { viewModel.setFilter(NotificationFilter.ALL) },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = uiState.selectedFilter == NotificationFilter.UNREAD,
                        onClick = { viewModel.setFilter(NotificationFilter.UNREAD) },
                        label = {
                            Text(
                                if (uiState.unreadCount > 0) "Unread (${uiState.unreadCount})" else "Unread"
                            )
                        }
                    )
                }

                // Search Bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    placeholder = { Text("Search notifications...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Content State
                when {
                    uiState.isLoading || uiState.isActionLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.filteredNotifications.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.NotificationsNone,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (uiState.selectedFilter == NotificationFilter.UNREAD) {
                                        "No unread notifications"
                                    } else if (uiState.searchQuery.isNotEmpty()) {
                                        "No matching notifications"
                                    } else {
                                        "No notifications"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (uiState.selectedFilter == NotificationFilter.UNREAD) {
                                        "You have caught up with all your notifications."
                                    } else {
                                        "You don't have any notifications right now."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(
                                items = uiState.filteredNotifications,
                                key = { it.notificationId }
                            ) { notification ->
                                NotificationRowItem(
                                    notification = notification,
                                    onClick = { onNotificationClick(notification.notificationId) }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRowItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val isUnread = !notification.isRead

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon according to type
        val icon = getNotificationIcon(notification.notificationType)
        Surface(
            shape = MaterialTheme.shapes.small,
            color = if (isUnread) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isUnread) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (isUnread) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!notification.createdAt.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = notification.createdAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "View Details",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getNotificationIcon(type: String): ImageVector {
    return when (type) {
        "NEW_ASSIGNMENT" -> Icons.Default.Assignment
        "NEW_MESSAGE" -> Icons.Default.Chat
        "RESULT_PUBLISHED" -> Icons.Default.Assessment
        "ATTENDANCE_ALERT" -> Icons.Default.EventBusy
        "NEW_ANNOUNCEMENT" -> Icons.Default.Campaign
        else -> Icons.Default.Notifications
    }
}
