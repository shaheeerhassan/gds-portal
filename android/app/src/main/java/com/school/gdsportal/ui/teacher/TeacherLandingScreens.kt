package com.school.gdsportal.ui.teacher

import androidx.compose.runtime.Composable
import com.school.gdsportal.ui.admin.components.NavigationRowData
import com.school.gdsportal.ui.admin.components.SectionLandingPage

@Composable
fun TeacherAcademicsLandingScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    SectionLandingPage(
        title = "Academics",
        description = "Manage your assigned classes, subjects, and timetable",
        rows = listOf(
            NavigationRowData("My Classes", "teacher_classes", isImplemented = true),
            NavigationRowData("My Subjects", "teacher_subjects", isImplemented = true),
            NavigationRowData("Class Teacher", "teacher_class_teacher", isImplemented = true),
            NavigationRowData("Timetable", "teacher_timetable", isImplemented = true)
        ),
        onBackClick = onBack,
        onRowClick = onNavigate
    )
}

@Composable
fun TeacherAssessmentLandingScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    SectionLandingPage(
        title = "Assessment",
        description = "Manage assignments, examinations, and marks",
        rows = listOf(
            NavigationRowData("Assignments", "teacher_assignments", isImplemented = true),
            NavigationRowData("Examinations", "teacher_examinations", isImplemented = true),
            NavigationRowData("Marks & Grading", "teacher_marks", isImplemented = true)
        ),
        onBackClick = onBack,
        onRowClick = onNavigate
    )
}

@Composable
fun TeacherAttendanceLandingScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    SectionLandingPage(
        title = "Attendance",
        description = "Manage your attendance and student attendance",
        rows = listOf(
            NavigationRowData("My Attendance", "teacher_my_attendance", isImplemented = true),
            NavigationRowData("Student Attendance", "teacher_student_attendance", isImplemented = true)
        ),
        onBackClick = onBack,
        onRowClick = onNavigate
    )
}

@Composable
fun TeacherCommunicationLandingScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    SectionLandingPage(
        title = "Communication",
        description = "View announcements and notifications",
        rows = listOf(
            NavigationRowData("Announcements", "teacher_announcements", isImplemented = true),
            NavigationRowData("Notifications", "teacher_notifications", isImplemented = true)
        ),
        onBackClick = onBack,
        onRowClick = onNavigate
    )
}
