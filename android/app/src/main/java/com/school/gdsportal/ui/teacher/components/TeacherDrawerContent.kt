package com.school.gdsportal.ui.teacher.components

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
import com.school.gdsportal.ui.theme.AccentTeacher // Ensure this exists in your Color.kt
import com.school.gdsportal.ui.theme.BackgroundColor
import com.school.gdsportal.ui.theme.TextPrimary

@Composable
fun TeacherDrawerContent(
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
                text = "Teacher Workspace",
                style = MaterialTheme.typography.bodyLarge,
                color = AccentTeacher
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DrawerCategoryTitle("MAIN")
                TeacherDrawerItem("Dashboard", Icons.Outlined.Home, currentRoute == "teacher_dashboard") { onNavigate("teacher_dashboard") }

                Spacer(modifier = Modifier.height(8.dp))
                DrawerCategoryTitle("MY TEACHING")
                TeacherDrawerItem("My Classes", Icons.Outlined.Class, currentRoute.startsWith("teacher_classes")) { onNavigate("teacher_classes") }
                TeacherDrawerItem("My Subjects", Icons.Outlined.MenuBook, currentRoute.startsWith("teacher_subjects")) { onNavigate("teacher_subjects") }
                TeacherDrawerItem("Class Teacher", Icons.Outlined.Stars, currentRoute.startsWith("teacher_class_teacher")) { onNavigate("teacher_class_teacher") }

                Spacer(modifier = Modifier.height(8.dp))
                DrawerCategoryTitle("WORKFLOW")
                TeacherDrawerItem("Timetable", Icons.Outlined.CalendarToday, currentRoute.startsWith("teacher_timetable")) { onNavigate("teacher_timetable") }
                TeacherDrawerItem("Assignments", Icons.Outlined.Assignment, currentRoute.startsWith("teacher_assignments")) { onNavigate("teacher_assignments") }

                Spacer(modifier = Modifier.height(8.dp))
                DrawerCategoryTitle("ATTENDANCE")
                TeacherDrawerItem("My Attendance", Icons.Outlined.Badge, currentRoute.startsWith("teacher_my_attendance")) { onNavigate("teacher_my_attendance") }
                TeacherDrawerItem("Student Attendance", Icons.Outlined.FactCheck, currentRoute.startsWith("teacher_student_attendance")) { onNavigate("teacher_student_attendance") }

                Spacer(modifier = Modifier.height(8.dp))
                DrawerCategoryTitle("ASSESSMENT")
                TeacherDrawerItem("Examinations", Icons.Outlined.EventNote, currentRoute.startsWith("teacher_examinations")) { onNavigate("teacher_examinations") }
                TeacherDrawerItem("Marks & Grading", Icons.Outlined.Assessment, currentRoute.startsWith("teacher_marks")) { onNavigate("teacher_marks") }

                Spacer(modifier = Modifier.height(8.dp))
                DrawerCategoryTitle("COMMUNICATION")
                TeacherDrawerItem("Announcements", Icons.Outlined.Campaign, currentRoute.startsWith("teacher_announcements")) { onNavigate("teacher_announcements") }
                TeacherDrawerItem("Notifications", Icons.Outlined.Notifications, currentRoute.startsWith("teacher_notifications")) { onNavigate("teacher_notifications") }
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
private fun DrawerCategoryTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp)
    )
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
        }
    }
}