package com.school.gdsportal.ui.admin

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.school.gdsportal.di.AppContainer
import com.school.gdsportal.ui.admin.components.AdminDrawerContent
import com.school.gdsportal.ui.admin.dashboard.AdminDashboardScreen
import com.school.gdsportal.ui.admin.dashboard.AdminDashboardViewModel
import kotlinx.coroutines.launch

@Composable
fun AdminAppWrapper(
    appContainer: AppContainer,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "admin_dashboard"

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AdminDrawerContent(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    coroutineScope.launch {
                        drawerState.close()
                    }
                    navController.navigate(route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    ) {
        val openDrawer: () -> Unit = {
            coroutineScope.launch { drawerState.open() }
        }

        NavHost(navController = navController, startDestination = "admin_dashboard") {
            composable("admin_dashboard") {
                val dashboardViewModel: AdminDashboardViewModel = viewModel(
                    factory = AdminDashboardViewModel.provideFactory(
                        appContainer.apiService,
                        appContainer.tokenManager
                    )
                )
                AdminDashboardScreen(
                    viewModel = dashboardViewModel,
                    onMenuClick = openDrawer
                )
            }

            composable("admin_people_landing") {
                PeopleLandingScreen(
                    onBack = { navController.navigateUp() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("admin_students_list") {
                val studentsViewModel: com.school.gdsportal.ui.admin.students.StudentsDirectoryViewModel = viewModel(
                    factory = com.school.gdsportal.ui.admin.students.StudentsDirectoryViewModel.provideFactory(appContainer.apiService)
                )
                com.school.gdsportal.ui.admin.students.StudentsDirectoryScreen(
                    viewModel = studentsViewModel,
                    onMenuClick = openDrawer,
                    onStudentClick = { /* TODO: Student Profile */ },
                    onAddStudentClick = { /* TODO: Add Student */ }
                )
            }
            
            composable("admin_academics_landing") {
                AcademicsLandingScreen(
                    onBack = { navController.navigateUp() },
                    onNavigate = { /* TODO Phase 2 */ }
                )
            }
            
            composable("admin_teaching_landing") {
                TeachingLandingScreen(
                    onBack = { navController.navigateUp() },
                    onNavigate = { /* TODO Phase 2 */ }
                )
            }

            composable("admin_assessment_landing") {
                AssessmentLandingScreen(
                    onBack = { navController.navigateUp() },
                    onNavigate = { /* TODO Phase 2 */ }
                )
            }

            composable("admin_attendance_landing") {
                AttendanceLandingScreen(
                    onBack = { navController.navigateUp() },
                    onNavigate = { /* TODO Phase 2 */ }
                )
            }

            composable("admin_communication_landing") {
                CommunicationLandingScreen(
                    onBack = { navController.navigateUp() },
                    onNavigate = { /* TODO Phase 2 */ }
                )
            }

            composable("admin_reports_landing") {
                ReportsLandingScreen(
                    onBack = { navController.navigateUp() },
                    onNavigate = { /* TODO Phase 2 */ }
                )
            }

            composable("admin_profile_landing") {
                ProfileLandingScreen(
                    onBack = { navController.navigateUp() },
                    onNavigate = { /* TODO Phase 2 */ },
                    onLogout = onLogout
                )
            }
        }
    }
}
