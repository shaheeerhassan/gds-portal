package com.school.gdsportal.ui.principal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.ui.theme.AccentPrincipal
import com.school.gdsportal.ui.theme.BackgroundColor
import com.school.gdsportal.ui.theme.TextPrimary

@Composable
fun PrincipalDrawerContent(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = BackgroundColor,
        drawerContentColor = TextPrimary,
        modifier = Modifier.width(320.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "GDS Portal",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "Principal",
                style = MaterialTheme.typography.bodyLarge,
                color = AccentPrincipal
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable section for main items
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                PrincipalDrawerItem(
                    label = "Dashboard",
                    icon = Icons.Outlined.Home,
                    isSelected = currentRoute == "principal_dashboard",
                    onClick = { onNavigate("principal_dashboard") }
                )
                PrincipalDrawerItem(
                    label = "People",
                    icon = Icons.Outlined.People,
                    isSelected = currentRoute.startsWith("admin_people") ||
                            currentRoute.startsWith("students") ||
                            currentRoute.startsWith("teachers") ||
                            currentRoute.startsWith("principals") ||
                            currentRoute.startsWith("administrators") ||
                            currentRoute.startsWith("parents"),
                    onClick = { onNavigate("admin_people_landing") }
                )
                PrincipalDrawerItem(
                    label = "Academics",
                    icon = Icons.Outlined.MenuBook,
                    isSelected = currentRoute.startsWith("admin_academics") ||
                            currentRoute.startsWith("academic-years") ||
                            currentRoute.startsWith("classes") ||
                            currentRoute.startsWith("sections") ||
                            currentRoute.startsWith("subjects") ||
                            currentRoute.startsWith("periods"),
                    onClick = { onNavigate("admin_academics_landing") }
                )
                PrincipalDrawerItem(
                    label = "Teaching",
                    icon = Icons.Outlined.CalendarToday,
                    isSelected = currentRoute.startsWith("admin_teaching") ||
                            currentRoute.startsWith("class-teachers") ||
                            currentRoute.startsWith("teacher-classes") ||
                            currentRoute.startsWith("teacher-subjects") ||
                            currentRoute.startsWith("timetable"),
                    onClick = { onNavigate("admin_teaching_landing") }
                )
                PrincipalDrawerItem(
                    label = "Assessment",
                    icon = Icons.Outlined.Assessment,
                    isSelected = currentRoute.startsWith("admin_assessment") ||
                            currentRoute.startsWith("examinations") ||
                            currentRoute.startsWith("assignments") ||
                            currentRoute.startsWith("submissions") ||
                            currentRoute.startsWith("marks"),
                    onClick = { onNavigate("admin_assessment_landing") }
                )
                PrincipalDrawerItem(
                    label = "Attendance",
                    icon = Icons.Outlined.Checklist,
                    isSelected = currentRoute.startsWith("admin_attendance") ||
                            currentRoute.startsWith("student-attendance") ||
                            currentRoute.startsWith("admin_teacher_attendance"),
                    onClick = { onNavigate("admin_attendance_landing") }
                )
                PrincipalDrawerItem(
                    label = "Communication",
                    icon = Icons.Outlined.Campaign,
                    isSelected = currentRoute.startsWith("admin_communication") ||
                            currentRoute.startsWith("announcements") ||
                            currentRoute.startsWith("notifications"),
                    onClick = { onNavigate("admin_communication_landing") }
                )
                PrincipalDrawerItem(
                    label = "Reports",
                    icon = Icons.Outlined.BarChart,
                    isSelected = currentRoute.startsWith("admin_reports") ||
                            currentRoute.startsWith("reports"),
                    onClick = { onNavigate("admin_reports_landing") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            PrincipalDrawerItem(
                label = "Profile & Account",
                icon = Icons.Outlined.Person,
                isSelected = currentRoute.startsWith("admin_profile") ||
                        currentRoute.startsWith("admin_my_profile") ||
                        currentRoute.startsWith("admin_change_password"),
                onClick = { onNavigate("admin_profile_landing") }
            )
            Spacer(modifier = Modifier.height(16.dp)) // padding at bottom
        }
    }
}

@Composable
private fun PrincipalDrawerItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val backgroundColor = if (isSelected) AccentPrincipal.copy(alpha = 0.08f) else Color.Transparent
    val contentColor = when {
        isSelected -> AccentPrincipal
        isDestructive -> MaterialTheme.colorScheme.error
        else -> TextPrimary
    }
    val fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp)
    ) {
        // Subtle indicator bar on the left
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AccentPrincipal)
                    .align(Alignment.CenterStart)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = if (isSelected) 12.dp else 0.dp) // shift text if indicator is present
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = fontWeight)
            )
            Spacer(modifier = Modifier.weight(1f))
            if (label != "Dashboard" && label != "Logout") {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}