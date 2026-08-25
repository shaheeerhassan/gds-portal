package com.school.gdsportal.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.school.gdsportal.di.AppContainer
import com.school.gdsportal.ui.student.components.StudentDrawerContent
import com.school.gdsportal.ui.theme.AccentStudent
import kotlinx.coroutines.launch

@Composable
fun StudentAppWrapper(
    appContainer: AppContainer,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "student_dashboard"

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StudentDrawerContent(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    coroutineScope.launch { drawerState.close() }
                    if (route == "student_dashboard") {
                        navController.popBackStack(navController.graph.startDestinationId, inclusive = false)
                    } else {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) {
        val openDrawer: () -> Unit = { coroutineScope.launch { drawerState.open() } }

        // OVERRIDE DEFAULT PRIMARY COLOR WITH STUDENT THEME
        val studentColorScheme = MaterialTheme.colorScheme.copy(primary = AccentStudent)

        MaterialTheme(colorScheme = studentColorScheme) {

            val sharedAcademicsViewModel: com.school.gdsportal.ui.student.academics.StudentAcademicsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.school.gdsportal.ui.student.academics.StudentAcademicsViewModel.provideFactory(appContainer.apiService)
            )

            NavHost(navController = navController, startDestination = "student_dashboard") {

                composable("student_dashboard") {
                    val viewModel: com.school.gdsportal.ui.student.dashboard.StudentDashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.student.dashboard.StudentDashboardViewModel.provideFactory(
                            appContainer.apiService,
                            appContainer.tokenManager
                        )
                    )
                    com.school.gdsportal.ui.student.dashboard.StudentDashboardScreen(
                        viewModel = viewModel,
                        onMenuClick = openDrawer,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }

                composable("student_subjects") {
                    com.school.gdsportal.ui.student.academics.StudentSubjectsScreen(viewModel = sharedAcademicsViewModel, onMenuClick = openDrawer)
                }

                composable("student_teachers") {
                    com.school.gdsportal.ui.student.academics.StudentTeachersScreen(viewModel = sharedAcademicsViewModel, onMenuClick = openDrawer)
                }

                composable("student_timetable") {
                    com.school.gdsportal.ui.student.academics.StudentTimetableScreen(viewModel = sharedAcademicsViewModel, onMenuClick = openDrawer)
                }

                composable("student_assignments") {
                    val viewModel: com.school.gdsportal.ui.student.assessment.assignments.StudentAssignmentsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.student.assessment.assignments.StudentAssignmentsViewModel.provideFactory(appContainer.apiService)
                    )
                    com.school.gdsportal.ui.student.assessment.assignments.StudentAssignmentsScreen(
                        viewModel = viewModel,
                        onMenuClick = openDrawer,
                        onAssignmentClick = { id -> navController.navigate("student_assignments/$id") }
                    )
                }

                composable("student_assignments/{assignmentId}") { backStackEntry ->
                    val assignmentId = backStackEntry.arguments?.getString("assignmentId")?.toLongOrNull() ?: 0L
                    val viewModel: com.school.gdsportal.ui.student.assessment.assignments.StudentAssignmentDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.student.assessment.assignments.StudentAssignmentDetailViewModel.provideFactory(appContainer.apiService, assignmentId)
                    )
                    com.school.gdsportal.ui.student.assessment.assignments.StudentAssignmentDetailScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable("student_examinations") {
                    val viewModel: com.school.gdsportal.ui.student.assessment.examinations.StudentExaminationsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.student.assessment.examinations.StudentExaminationsViewModel.provideFactory(appContainer.apiService)
                    )
                    com.school.gdsportal.ui.student.assessment.examinations.StudentExaminationsScreen(viewModel = viewModel, onMenuClick = openDrawer)
                }

                composable("student_grades") {
                    val viewModel: com.school.gdsportal.ui.student.assessment.grades.StudentGradesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.student.assessment.grades.StudentGradesViewModel.provideFactory(appContainer.apiService)
                    )
                    com.school.gdsportal.ui.student.assessment.grades.StudentGradesScreen(viewModel = viewModel, onMenuClick = openDrawer)
                }

                composable("student_attendance") {
                    val viewModel: com.school.gdsportal.ui.student.attendance.StudentAttendanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.student.attendance.StudentAttendanceViewModel.provideFactory(appContainer.apiService)
                    )
                    com.school.gdsportal.ui.student.attendance.StudentAttendanceScreen(
                        viewModel = viewModel,
                        onMenuClick = openDrawer
                    )
                }

                composable("student_announcements") {
                    val viewModel: com.school.gdsportal.ui.student.announcements.StudentAnnouncementsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.student.announcements.StudentAnnouncementsViewModel.provideFactory(appContainer.apiService)
                    )
                    com.school.gdsportal.ui.student.announcements.StudentAnnouncementsScreen(
                        viewModel = viewModel,
                        onMenuClick = openDrawer,
                        onAnnouncementClick = { id -> navController.navigate("student_announcements/$id") }
                    )
                }

                composable("student_announcements/{announcementId}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("announcementId")?.toLongOrNull() ?: 0L
                    val viewModel: com.school.gdsportal.ui.admin.communication.announcements.AnnouncementDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.admin.communication.announcements.AnnouncementDetailViewModel.Factory(appContainer.apiService, id)
                    )
                    com.school.gdsportal.ui.admin.communication.announcements.AnnouncementDetailScreen(
                        isReadOnly = true,
                        viewModel = viewModel,
                        onBackClick = { navController.navigateUp() },
                        onEditClick = { }
                    )
                }

                composable("student_notifications") {
                    val viewModel: com.school.gdsportal.ui.admin.communication.notifications.NotificationsDirectoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.admin.communication.notifications.NotificationsDirectoryViewModel.Factory(appContainer.apiService)
                    )
                    com.school.gdsportal.ui.admin.communication.notifications.NotificationsDirectoryScreen(
                        isReadOnly = true,
                        viewModel = viewModel,
                        onBackClick = { navController.navigateUp() },
                        onCreateClick = { },
                        onNotificationClick = { id -> navController.navigate("student_notifications/$id") }
                    )
                }

                composable("student_notifications/{notificationId}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("notificationId")?.toLongOrNull() ?: 0L
                    val viewModel: com.school.gdsportal.ui.admin.communication.notifications.NotificationDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.admin.communication.notifications.NotificationDetailViewModel.Factory(appContainer.apiService, id)
                    )
                    com.school.gdsportal.ui.admin.communication.notifications.NotificationDetailScreen(
                        isReadOnly = true,
                        viewModel = viewModel,
                        onBackClick = { navController.navigateUp() }
                    )
                }

                // -------------------------------------------------------------
                // PROFILE & ACCOUNT LANDING PAGE
                // -------------------------------------------------------------
                composable("student_profile_landing") {
                    com.school.gdsportal.ui.admin.components.SectionLandingPage(
                        title = "Profile & Account",
                        description = "Manage your student account",
                        rows = listOf(
                            com.school.gdsportal.ui.admin.components.NavigationRowData("My Profile", "student_profile", isImplemented = true),
                            com.school.gdsportal.ui.admin.components.NavigationRowData("Change Password", "change_password", isImplemented = true),
                            com.school.gdsportal.ui.admin.components.NavigationRowData("Logout", "logout_action", isImplemented = true)
                        ),
                        onBackClick = openDrawer,
                        onRowClick = { route ->
                            if (route == "logout_action") {
                                onLogout()
                            } else {
                                navController.navigate(route)
                            }
                        }
                    )
                }

                composable("student_profile") {
                    val viewModel: com.school.gdsportal.ui.student.profile.StudentProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.student.profile.StudentProfileViewModel.provideFactory(appContainer.apiService)
                    )
                    com.school.gdsportal.ui.student.profile.StudentProfileScreen(
                        viewModel = viewModel,
                        onMenuClick = { navController.navigateUp() } // Now acts as a back button from the landing page
                    )
                }

                composable("change_password") {
                    val viewModel: com.school.gdsportal.ui.admin.profile.ChangePasswordViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.admin.profile.ChangePasswordViewModel.provideFactory(appContainer.apiService)
                    )
                    com.school.gdsportal.ui.admin.profile.ChangePasswordScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.navigateUp() }
                    )
                }
            }
        }
    }
}