package com.school.gdsportal.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.school.gdsportal.di.AppContainer
import com.school.gdsportal.ui.admin.ProfileLandingScreen
import com.school.gdsportal.ui.admin.profile.ChangePasswordScreen
import com.school.gdsportal.ui.admin.profile.ChangePasswordViewModel
import com.school.gdsportal.ui.admin.profile.ProfileScreen
import com.school.gdsportal.ui.admin.profile.ProfileViewModel
import com.school.gdsportal.ui.teacher.components.TeacherDrawerContent
import com.school.gdsportal.ui.theme.AccentTeacher // Ensure this exists
import kotlinx.coroutines.launch

@Composable
fun TeacherAppWrapper(
    appContainer: AppContainer,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "teacher_dashboard"

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            TeacherDrawerContent(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    coroutineScope.launch { drawerState.close() }
                    if (route == "teacher_dashboard") {
                        navController.popBackStack(navController.graph.startDestinationId, inclusive = false)
                    } else {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onLogout = onLogout
            )
        }
    ) {
        val openDrawer: () -> Unit = { coroutineScope.launch { drawerState.open() } }

        // OVERRIDE DEFAULT PRIMARY COLOR WITH TEACHER THEME
        val teacherColorScheme = MaterialTheme.colorScheme.copy(primary = AccentTeacher)

        MaterialTheme(colorScheme = teacherColorScheme) {
            NavHost(navController = navController, startDestination = "teacher_dashboard") {

                composable("teacher_dashboard") {
                    PlaceholderScreen("Dashboard", openDrawer)
                }

                composable("teacher_classes") {
                    val viewModel: com.school.gdsportal.ui.teacher.classes.TeacherClassesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.teacher.classes.TeacherClassesViewModel.provideFactory(
                            appContainer.apiService,
                            appContainer.tokenManager
                        )
                    )
                    com.school.gdsportal.ui.teacher.classes.TeacherClassesScreen(
                        viewModel = viewModel,
                        onMenuClick = openDrawer,
                        onClassClick = { sectionId ->
                            navController.navigate("teacher_classes/$sectionId")
                        }
                    )
                }

                composable("teacher_classes/{sectionId}") { backStackEntry ->
                    val sectionId = backStackEntry.arguments?.getString("sectionId")?.toIntOrNull() ?: 0
                    val viewModel: com.school.gdsportal.ui.teacher.classes.students.TeacherClassStudentsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.teacher.classes.students.TeacherClassStudentsViewModel.provideFactory(sectionId, appContainer.apiService)
                    )
                    com.school.gdsportal.ui.teacher.classes.students.TeacherClassStudentsScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.navigateUp() },
                        onStudentClick = { studentId ->
                            navController.navigate("teacher_students/$studentId")
                        }
                    )
                }

                composable("teacher_students/{studentId}") { backStackEntry ->
                    val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                    val viewModel: com.school.gdsportal.ui.teacher.students.TeacherStudentProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.teacher.students.TeacherStudentProfileViewModel.provideFactory(studentId, appContainer.apiService)
                    )
                    com.school.gdsportal.ui.teacher.students.TeacherStudentProfileScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable("teacher_subjects") {
                    val viewModel: com.school.gdsportal.ui.teacher.subjects.TeacherSubjectsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.teacher.subjects.TeacherSubjectsViewModel.provideFactory(
                            appContainer.apiService,
                            appContainer.tokenManager
                        )
                    )
                    com.school.gdsportal.ui.teacher.subjects.TeacherSubjectsScreen(
                        viewModel = viewModel,
                        onMenuClick = openDrawer
                    )
                }

                composable("teacher_class_teacher") {
                    val viewModel: com.school.gdsportal.ui.teacher.classteacher.TeacherClassTeacherViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.teacher.classteacher.TeacherClassTeacherViewModel.provideFactory(
                            appContainer.apiService,
                            appContainer.tokenManager
                        )
                    )
                    com.school.gdsportal.ui.teacher.classteacher.TeacherClassTeacherScreen(
                        viewModel = viewModel,
                        onMenuClick = openDrawer
                    )
                }

                composable("teacher_timetable") {
                    val viewModel: com.school.gdsportal.ui.teacher.timetable.TeacherTimetableViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.teacher.timetable.TeacherTimetableViewModel.provideFactory(
                            appContainer.apiService,
                            appContainer.tokenManager
                        )
                    )
                    com.school.gdsportal.ui.teacher.timetable.TeacherTimetableScreen(
                        viewModel = viewModel,
                        onMenuClick = openDrawer
                    )
                }

                // Replace the old composable("teacher_assignments") placeholder with this:
                composable("teacher_assignments") {
                    val viewModel: com.school.gdsportal.ui.teacher.assignments.TeacherAssignmentsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.teacher.assignments.TeacherAssignmentsViewModel.provideFactory(
                            appContainer.apiService,
                            appContainer.tokenManager
                        )
                    )
                    com.school.gdsportal.ui.teacher.assignments.TeacherAssignmentsScreen(
                        viewModel = viewModel,
                        onMenuClick = openDrawer,
                        onCreateClick = { navController.navigate("teacher_assignments/create") },
                        onAssignmentClick = { assignmentId ->
                            navController.navigate("teacher_assignments/$assignmentId")
                        }
                    )
                }

                // Temporary placeholders for the next steps
                composable("teacher_assignments/create") {
                    PlaceholderScreen("Create Assignment") { navController.navigateUp() }
                }

                composable("teacher_assignments/{assignmentId}") {
                    PlaceholderScreen("Assignment Details") { navController.navigateUp() }
                }

                composable("teacher_my_attendance") {
                    val viewModel: com.school.gdsportal.ui.teacher.attendance.myattendance.TeacherMyAttendanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.teacher.attendance.myattendance.TeacherMyAttendanceViewModel.provideFactory(
                            appContainer.apiService,
                            appContainer.tokenManager
                        )
                    )
                    com.school.gdsportal.ui.teacher.attendance.myattendance.TeacherMyAttendanceScreen(
                        viewModel = viewModel,
                        onMenuClick = openDrawer
                    )
                }

                composable("teacher_student_attendance") {
                    val viewModel: com.school.gdsportal.ui.teacher.attendance.studentattendance.TeacherStudentAttendanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.teacher.attendance.studentattendance.TeacherStudentAttendanceViewModel.provideFactory(
                            appContainer.apiService,
                            appContainer.tokenManager
                        )
                    )
                    com.school.gdsportal.ui.teacher.attendance.studentattendance.TeacherStudentAttendanceScreen(
                        viewModel = viewModel,
                        onMenuClick = openDrawer
                    )
                }

                composable("teacher_examinations") { PlaceholderScreen("Examinations", openDrawer) }
                composable("teacher_marks") { PlaceholderScreen("Marks & Grading", openDrawer) }
                composable("teacher_announcements") { PlaceholderScreen("Announcements", openDrawer) }
                composable("teacher_notifications") { PlaceholderScreen("Notifications", openDrawer) }

                // -------------------------------------------------------------
                // PROFILE & ACCOUNT (Reusing existing components)
                // -------------------------------------------------------------
                composable("teacher_profile_landing") {
                    ProfileLandingScreen(
                        onBack = { openDrawer() },
                        onNavigate = { route ->
                            when (route) {
                                "admin_my_profile" -> navController.navigate("teacher_profile")
                                "admin_change_password" -> navController.navigate("teacher_change_password")
                                "logout_action" -> onLogout()
                            }
                        },
                        onLogout = onLogout
                    )
                }

                composable("teacher_profile") {
                    val profileViewModel: ProfileViewModel = viewModel(
                        factory = ProfileViewModel.provideFactory(appContainer.apiService)
                    )
                    ProfileScreen(
                        viewModel = profileViewModel,
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable("teacher_change_password") {
                    val changePasswordViewModel: ChangePasswordViewModel = viewModel(
                        factory = ChangePasswordViewModel.provideFactory(appContainer.apiService)
                    )
                    ChangePasswordScreen(
                        viewModel = changePasswordViewModel,
                        onBackClick = { navController.navigateUp() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScreen(title: String, onMenuClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, contentDescription = "Menu") } }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Teacher $title - Coming in Phase 2!")
        }
    }
}