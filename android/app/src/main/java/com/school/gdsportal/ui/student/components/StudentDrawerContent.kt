package com.school.gdsportal.ui.student.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.school.gdsportal.ui.theme.AccentStudent
import com.school.gdsportal.ui.theme.BackgroundColor
import com.school.gdsportal.ui.theme.TextPrimary

@Composable
fun StudentDrawerContent(
    currentRoute: String,
    onNavigate: (String) -> Unit
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
                text = "Student",
                style = MaterialTheme.typography.bodyLarge,
                color = AccentStudent
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable section for main items
            Column(modifier = Modifier.weight(1f)) {
                StudentDrawerItem(
                    label = "Dashboard",
                    icon = Icons.Outlined.Home,
                    isSelected = currentRoute == "student_dashboard",
                    onClick = { onNavigate("student_dashboard") }
                )
                StudentDrawerItem(
                    label = "My Subjects",
                    icon = Icons.Outlined.Book,
                    isSelected = currentRoute == "student_subjects",
                    onClick = { onNavigate("student_subjects") }
                )
                StudentDrawerItem(
                    label = "My Teachers",
                    icon = Icons.Outlined.People,
                    isSelected = currentRoute == "student_teachers",
                    onClick = { onNavigate("student_teachers") }
                )
                StudentDrawerItem(
                    label = "My Timetable",
                    icon = Icons.Outlined.CalendarToday,
                    isSelected = currentRoute == "student_timetable",
                    onClick = { onNavigate("student_timetable") }
                )
                StudentDrawerItem(
                    label = "Assignments",
                    icon = Icons.Outlined.Assignment,
                    isSelected = currentRoute.startsWith("student_assignments"),
                    onClick = { onNavigate("student_assignments") }
                )
                StudentDrawerItem(
                    label = "Examinations",
                    icon = Icons.Outlined.EventNote,
                    isSelected = currentRoute == "student_examinations",
                    onClick = { onNavigate("student_examinations") }
                )
                StudentDrawerItem(
                    label = "Marks & Grades",
                    icon = Icons.Outlined.Assessment,
                    isSelected = currentRoute == "student_grades",
                    onClick = { onNavigate("student_grades") }
                )
                StudentDrawerItem(
                    label = "My Attendance",
                    icon = Icons.Outlined.Checklist,
                    isSelected = currentRoute == "student_attendance",
                    onClick = { onNavigate("student_attendance") }
                )
                StudentDrawerItem(
                    label = "Announcements",
                    icon = Icons.Outlined.Campaign,
                    isSelected = currentRoute.startsWith("student_announcements"),
                    onClick = { onNavigate("student_announcements") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            // Combined Profile & Account Landing
            StudentDrawerItem(
                label = "Profile & Account",
                icon = Icons.Outlined.Person,
                isSelected = currentRoute.startsWith("student_profile") || currentRoute == "change_password",
                onClick = { onNavigate("student_profile_landing") }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StudentDrawerItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) AccentStudent.copy(alpha = 0.08f) else Color.Transparent
    val contentColor = if (isSelected) AccentStudent else TextPrimary
    val fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp)
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AccentStudent)
                    .align(Alignment.CenterStart)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = if (isSelected) 12.dp else 0.dp)
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
            if (label != "Dashboard") {
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