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
            NavigationRowData("Academic Years", "academic-years", isImplemented = true),
            NavigationRowData("Classes", "classes", isImplemented = true),
            NavigationRowData("Sections", "sections", isImplemented = true),
            NavigationRowData("Subjects", "subjects", isImplemented = true),
            NavigationRowData("Periods", "periods", isImplemented = true)
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
            NavigationRowData("Timetable", "timetable", isImplemented = true),
            NavigationRowData("Class Teachers", "class-teachers", isImplemented = true),
            NavigationRowData("Teacher Classes", "teacher-classes", isImplemented = true),
            NavigationRowData("Teacher Subjects", "teacher-subjects", isImplemented = true)
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
            NavigationRowData("Examinations", "examinations", isImplemented = true),
            NavigationRowData("Assignments", "assignments", isImplemented = true),
            NavigationRowData("Submissions", "submissions", isImplemented = true),
            NavigationRowData("Marks", "marks", isImplemented = true)
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
            NavigationRowData("Student Attendance", "student-attendance", isImplemented = true),
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
            NavigationRowData("Announcements", "announcements", isImplemented = true),
            NavigationRowData("Notifications", "notifications", isImplemented = true)
        ),
        onBackClick = onBack,
        onRowClick = onNavigate
    )
}

@Composable
fun ReportsLandingScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    SectionLandingPage(
        title = "Reports",
        description = "Generate and view school reports",
        rows = listOf(
            NavigationRowData("Student Performance", "reports/student-performance", isImplemented = true),
            NavigationRowData("Teacher Attendance", "reports/teacher-attendance", isImplemented = true),
            NavigationRowData("Class Attendance", "reports/class-attendance", isImplemented = true),
            NavigationRowData("Teacher Performance", "reports/teacher-performance", isImplemented = true),
            NavigationRowData("Examination Report", "reports/examination", isImplemented = true),
            NavigationRowData("Student Attendance Summary", "reports/student-attendance-summary", isImplemented = true)
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
