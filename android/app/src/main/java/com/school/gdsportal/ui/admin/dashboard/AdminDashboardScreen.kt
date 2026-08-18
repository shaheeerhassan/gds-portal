package com.school.gdsportal.ui.admin.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.Announcement
import com.school.gdsportal.ui.admin.components.AdminTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    isReadOnly: Boolean = false, // Added to protect Principal view
    viewModel: AdminDashboardViewModel,
    onMenuClick: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AdminTopAppBar(
                title = "GDS Portal",
                onMenuClick = onMenuClick,
                actions = {
                    IconButton(onClick = { viewModel.refreshDashboard() }) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Dashboard")
                        }
                    }

                    Box(modifier = Modifier.padding(end = 16.dp)) {
                        // Routes to unread notifications view
                        IconButton(onClick = { onNavigate("notifications") }) {
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
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        Text(
                            text = "Good morning,",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.user?.firstName?: "Administrator",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        uiState.currentAcademicYear?.let { year ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Session: ${year.yearName}",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                if (uiState.error != null) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = uiState.error!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Attendance Overview
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Today's Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FlatMetricCard(
                                label = "Students Present",
                                value = "${uiState.presentStudents}/${uiState.totalStudents}",
                                icon = Icons.Default.Groups,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigate("student-attendance") }
                            )
                            FlatMetricCard(
                                label = "Teachers Present",
                                value = "${uiState.presentTeachers}/${uiState.totalTeachers}",
                                icon = Icons.Default.School,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigate("admin_teacher_attendance") }
                            )
                        }
                    }
                }

                // Quick Actions (Hidden from Principal)
                if (!isReadOnly) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = "Quick Actions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FlatQuickAction(
                                    title = "Add Student",
                                    icon = Icons.Default.PersonAdd,
                                    onClick = { onNavigate("students/create") },
                                    modifier = Modifier.weight(1f)
                                )
                                FlatQuickAction(
                                    title = "Add Teacher",
                                    icon = Icons.Default.PersonAddAlt,
                                    onClick = { onNavigate("teachers/create") },
                                    modifier = Modifier.weight(1f)
                                )
                                FlatQuickAction(
                                    title = "Broadcast",
                                    icon = Icons.AutoMirrored.Filled.Announcement,
                                    onClick = { onNavigate("announcements/create") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // NEW: School Management Section (Visible to both)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "School Management",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FlatQuickAction(
                                title = "Timetable",
                                icon = Icons.Default.CalendarToday,
                                onClick = { onNavigate("timetable") },
                                modifier = Modifier.weight(1f)
                            )
                            FlatQuickAction(
                                title = "Exams",
                                icon = Icons.Default.Book,
                                onClick = { onNavigate("examinations") },
                                modifier = Modifier.weight(1f)
                            )
                            FlatQuickAction(
                                title = "Assignments",
                                icon = Icons.AutoMirrored.Filled.Assignment,
                                onClick = { onNavigate("assignments") },
                                modifier = Modifier.weight(1f)
                            )
                            FlatQuickAction(
                                title = "Parents",
                                icon = Icons.Default.FamilyRestroom,
                                onClick = { onNavigate("parents") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Latest Announcements
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Latest Announcements",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "See All",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { onNavigate("announcements") }
                                    .padding(4.dp)
                            )
                        }

                        if (uiState.globalAnnouncements.isEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "No active announcements",
                                    modifier = Modifier.padding(24.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                uiState.globalAnnouncements.take(3).forEach { announcement ->
                                    FlatAnnouncementItem(
                                        announcement = announcement,
                                        // Routes to the specific announcement view page
                                        onClick = { onNavigate("announcements/${announcement.announcementId}") }
                                    )
                                }
                            }
                        }
                    }
                }

                // Academics Summary
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Academics Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                FlatStatItem("Classes", uiState.totalClasses.toString()) { onNavigate("classes") }
                                FlatStatItem("Sections", uiState.totalSections.toString()) { onNavigate("sections") }
                                FlatStatItem("Subjects", uiState.totalSubjects.toString()) { onNavigate("subjects") }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun FlatMetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = containerColor
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun FlatQuickAction(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FlatAnnouncementItem(
    announcement: Announcement,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
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
                    color = MaterialTheme.colorScheme.onSurface,
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun FlatStatItem(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}