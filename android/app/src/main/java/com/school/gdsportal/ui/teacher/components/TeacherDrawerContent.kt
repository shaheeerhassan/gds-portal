package com.school.gdsportal.ui.teacher.components

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
import com.school.gdsportal.ui.theme.AccentTeacher
import com.school.gdsportal.ui.theme.BackgroundColor
import com.school.gdsportal.ui.theme.TextPrimary

@Composable
fun TeacherDrawerContent(
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
                text = "Teacher Portal",
                style = MaterialTheme.typography.bodyLarge,
                color = AccentTeacher
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            // Categorized, compact list matching the Admin architecture perfectly
            Column(modifier = Modifier.weight(1f)) {
                TeacherDrawerItem(
                    label = "Dashboard",
                    icon = Icons.Outlined.Home,
                    isSelected = currentRoute == "teacher_dashboard",
                    onClick = { onNavigate("teacher_dashboard") }
                )
                TeacherDrawerItem(
                    label = "Academics",
                    icon = Icons.Outlined.MenuBook,
                    isSelected = currentRoute.startsWith("teacher_academics_landing") ||
                            currentRoute.startsWith("teacher_classes") ||
                            currentRoute.startsWith("teacher_subjects") ||
                            currentRoute.startsWith("teacher_class_teacher") ||
                            currentRoute.startsWith("teacher_timetable"),
                    onClick = { onNavigate("teacher_academics_landing") }
                )
                TeacherDrawerItem(
                    label = "Assessment",
                    icon = Icons.Outlined.Assessment,
                    isSelected = currentRoute.startsWith("teacher_assessment_landing") ||
                            currentRoute.startsWith("teacher_assignments") ||
                            currentRoute.startsWith("teacher_examinations") ||
                            currentRoute.startsWith("teacher_marks"),
                    onClick = { onNavigate("teacher_assessment_landing") }
                )
                TeacherDrawerItem(
                    label = "Attendance",
                    icon = Icons.Outlined.Checklist,
                    isSelected = currentRoute.startsWith("teacher_attendance_landing") ||
                            currentRoute.startsWith("teacher_my_attendance") ||
                            currentRoute.startsWith("teacher_student_attendance"),
                    onClick = { onNavigate("teacher_attendance_landing") }
                )
                TeacherDrawerItem(
                    label = "Communication",
                    icon = Icons.Outlined.Campaign,
                    isSelected = currentRoute.startsWith("teacher_communication_landing") ||
                            currentRoute.startsWith("teacher_announcements") ||
                            currentRoute.startsWith("teacher_notifications"),
                    onClick = { onNavigate("teacher_communication_landing") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            TeacherDrawerItem(
                label = "Profile & Account",
                icon = Icons.Outlined.Person,
                isSelected = currentRoute.startsWith("teacher_profile") || currentRoute.startsWith("teacher_change_password"),
                onClick = { onNavigate("teacher_profile_landing") }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TeacherDrawerItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) AccentTeacher.copy(alpha = 0.08f) else Color.Transparent
    val contentColor = if (isSelected) AccentTeacher else TextPrimary
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
                    .background(AccentTeacher)
                    .align(Alignment.CenterStart)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = if (isSelected) 12.dp else 0.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, color = contentColor, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = fontWeight))
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