package com.school.gdsportal.ui.admin

import androidx.compose.runtime.Composable
import com.school.gdsportal.ui.admin.components.NavigationRowData
import com.school.gdsportal.ui.admin.components.SectionLandingPage

@Composable
fun PeopleLandingScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    SectionLandingPage(
        title = "People",
        description = "Manage people and accounts",
        rows = listOf(
            NavigationRowData("Students", "students", isImplemented = true),
            NavigationRowData("Teachers", "teachers", isImplemented = true),
            NavigationRowData("Parents", "parents", isImplemented = true),
            NavigationRowData("Principals", "principals", isImplemented = true),
            NavigationRowData("Administrators", "administrators", isImplemented = true)
        ),
        onBackClick = onBack,
        onRowClick = onNavigate
    )
}

@Composable
fun AcademicsLandingScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    SectionLandingPage(
        title = "Academics",
        description = "Academic structure and enrollment",
        rows = listOf(
            NavigationRowData("Academic Years", "admin_academic_years", isImplemented = false),
            NavigationRowData("Classes", "admin_classes", isImplemented = false),
            NavigationRowData("Sections", "admin_sections", isImplemented = false),
            NavigationRowData("Subjects", "admin_subjects", isImplemented = false),
            NavigationRowData("Periods", "admin_periods", isImplemented = false),
            NavigationRowData("Enrollments", "admin_enrollments", isImplemented = false)
        ),
        onBackClick = onBack,
        onRowClick = onNavigate
    )
}

@Composable
fun TeachingLandingScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    SectionLandingPage(
        title = "Teaching",
        description = "Teaching assignments and schedules",
        rows = listOf(
            NavigationRowData("Timetable", "admin_timetable", isImplemented = false),
            NavigationRowData("Class Teachers", "admin_class_teachers", isImplemented = false),
            NavigationRowData("Teacher Classes", "admin_teacher_classes", isImplemented = false),
            NavigationRowData("Teacher Subjects", "admin_teacher_subjects", isImplemented = false)
        ),
        onBackClick = onBack,
        onRowClick = onNavigate
    )
}

@Composable
fun AssessmentLandingScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    SectionLandingPage(
        title = "Assessment",
        description = "Examinations and academic assessment",
        rows = listOf(
            NavigationRowData("Examinations", "admin_examinations", isImplemented = false),
            NavigationRowData("Assignments", "admin_assignments", isImplemented = false),
            NavigationRowData("Submissions", "admin_submissions", isImplemented = false),
            NavigationRowData("Marks", "admin_marks", isImplemented = false)
        ),
        onBackClick = onBack,
        onRowClick = onNavigate
    )
}

@Composable
fun AttendanceLandingScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    SectionLandingPage(
        title = "Attendance",
        description = "Monitor and manage attendance",
        rows = listOf(
            NavigationRowData("Student Attendance", "admin_student_attendance", isImplemented = false),
            NavigationRowData("Teacher Attendance", "admin_teacher_attendance", isImplemented = true)
        ),
        onBackClick = onBack,
        onRowClick = onNavigate
    )
}

@Composable
fun CommunicationLandingScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    SectionLandingPage(
        title = "Communication",
        description = "School communication",
        rows = listOf(
            NavigationRowData("Announcements", "admin_announcements", isImplemented = false),
            NavigationRowData("Notifications", "admin_notifications", isImplemented = false)
        ),
        onBackClick = onBack,
        onRowClick = onNavigate
    )
}

@Composable
fun ReportsLandingScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    SectionLandingPage(
        title = "Reports",
        description = "School insights and summaries",
        rows = listOf(
            NavigationRowData("Student Performance", "admin_report_student_perf", isImplemented = false),
            NavigationRowData("Teacher Attendance", "admin_report_teacher_att", isImplemented = false),
            NavigationRowData("Class Attendance", "admin_report_class_att", isImplemented = false),
            NavigationRowData("Teacher Performance", "admin_report_teacher_perf", isImplemented = false),
            NavigationRowData("Examination Report", "admin_report_exams", isImplemented = false),
            NavigationRowData("Student Attendance Summary", "admin_report_student_att_sum", isImplemented = false)
        ),
        onBackClick = onBack,
        onRowClick = onNavigate
    )
}

@Composable
fun ProfileLandingScreen(onBack: () -> Unit, onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    SectionLandingPage(
        title = "Profile & Account",
        description = "Manage your administrator account",
        rows = listOf(
            NavigationRowData("My Profile", "admin_my_profile", isImplemented = false),
            NavigationRowData("Edit Profile Picture", "admin_edit_picture", isImplemented = false),
            NavigationRowData("Change Password", "admin_change_password", isImplemented = false),
            NavigationRowData("Logout", "admin_logout_action", isImplemented = true)
        ),
        onBackClick = onBack,
        onRowClick = { route -> 
            if (route == "admin_logout_action") onLogout() else onNavigate(route)
        }
    )
}
