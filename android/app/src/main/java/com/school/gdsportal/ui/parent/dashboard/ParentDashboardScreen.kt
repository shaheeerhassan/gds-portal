package com.school.gdsportal.ui.parent.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.util.Calendar
// Import the parent theme color
import com.school.gdsportal.ui.theme.AccentParent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    viewModel: ParentDashboardViewModel,
    onMenuClick: () -> Unit,
    onNavigateToChildren: () -> Unit,
    onNavigateToAnnouncements: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onAnnouncementClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parent Portal") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    // 1. ADDED EXPLICIT REFRESH BUTTON HERE
                    IconButton(onClick = { viewModel.refreshDashboard() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Dashboard",
                            tint = AccentParent // Themed Icon
                        )
                    }

                    // 2. EXISTING NOTIFICATIONS ICON
                    Box(modifier = Modifier.padding(end = 16.dp)) {
                        IconButton(onClick = onNavigateToNotifications) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications")
                        }
                        if (uiState.unreadNotifications > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 8.dp),
                                containerColor = MaterialTheme.colorScheme.error
                            ) {
                                Text(uiState.unreadNotifications.toString())
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentParent) // Themed Spinner
            }
        } else {
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refreshDashboard() },
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = getGreeting(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.parentFirstName,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (uiState.error != null) {
                        item {
                            Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            MetricCard(
                                title = "My Children",
                                value = uiState.childrenCount.toString(),
                                icon = Icons.Default.FamilyRestroom,
                                color = AccentParent,
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToChildren
                            )
                            MetricCard(
                                title = "Unread Messages",
                                value = uiState.unreadNotifications.toString(),
                                icon = Icons.Default.MailOutline,
                                color = AccentParent, // Themed Metric Card
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToNotifications
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Announcements",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "See All",
                                style = MaterialTheme.typography.labelLarge,
                                color = AccentParent, // Themed Text
                                modifier = Modifier.clickable { onNavigateToAnnouncements() }.padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (uiState.recentAnnouncements.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "No active announcements",
                                    modifier = Modifier.padding(24.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                uiState.recentAnnouncements.forEach { announcement ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().clickable { onAnnouncementClick(announcement.announcementId) },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = announcement.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = announcement.content,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).clickable { onClick() },
        color = color.copy(alpha = 0.15f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = color.copy(alpha = 0.8f))
        }
    }
}

private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning,"
        hour < 17 -> "Good afternoon,"
        else -> "Good evening,"
    }
}