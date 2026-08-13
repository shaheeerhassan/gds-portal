package com.school.gdsportal.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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


            // Student Module Routes
            composable("students") {
                val studentsViewModel: com.school.gdsportal.ui.admin.students.StudentsDirectoryViewModel = viewModel(
                    factory = com.school.gdsportal.ui.admin.students.StudentsDirectoryViewModel.provideFactory(
                        appContainer.apiService
                    )
                )
                com.school.gdsportal.ui.admin.students.StudentsDirectoryScreen(
                    viewModel = studentsViewModel,
                    onBackClick = { navController.navigateUp() },
                    onAddStudentClick = { navController.navigate("students/create") },
                    onStudentClick = { studentId -> navController.navigate("students/$studentId") }
                )
            }
            composable("students/create") {
                val factory = com.school.gdsportal.ui.admin.students.create.StudentCreateViewModel.provideFactory(
                    apiService = appContainer.apiService
                )
                val createViewModel: com.school.gdsportal.ui.admin.students.create.StudentCreateViewModel = viewModel(factory = factory)
                
                com.school.gdsportal.ui.admin.students.create.StudentCreateScreen(
                    viewModel = createViewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }
            composable("students/{studentId}") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.students.profile.StudentProfileViewModel.provideFactory(
                    studentId = studentId,
                    apiService = appContainer.apiService
                )
                val profileViewModel: com.school.gdsportal.ui.admin.students.profile.StudentProfileViewModel = viewModel(factory = factory)
                
                com.school.gdsportal.ui.admin.students.profile.StudentProfileScreen(
                    viewModel = profileViewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditClick = { navController.navigate("students/$studentId/edit") },
                    onPersonalClick = { navController.navigate("students/$studentId/personal") },
                    onEnrollmentClick = { navController.navigate("students/$studentId/enrollment") },
                    onParentsClick = { navController.navigate("students/$studentId/parents") },
                    onEnrollClick = { navController.navigate("students/$studentId/enroll") },
                    onTransferClick = { navController.navigate("students/$studentId/transfer") }
                )
            }
            composable("students/{studentId}/personal") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.students.personal.StudentPersonalViewModel.provideFactory(
                    studentId = studentId,
                    apiService = appContainer.apiService
                )
                val personalViewModel: com.school.gdsportal.ui.admin.students.personal.StudentPersonalViewModel = viewModel(factory = factory)
                
                com.school.gdsportal.ui.admin.students.personal.StudentPersonalScreen(
                    viewModel = personalViewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }
            composable("students/{studentId}/edit") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.students.edit.StudentEditViewModel.provideFactory(
                    studentId = studentId,
                    apiService = appContainer.apiService
                )
                val editViewModel: com.school.gdsportal.ui.admin.students.edit.StudentEditViewModel = viewModel(factory = factory)
                
                com.school.gdsportal.ui.admin.students.edit.StudentEditScreen(
                    viewModel = editViewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }
            composable("students/{studentId}/enrollment") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.students.enrollment.StudentEnrollmentViewModel.provideFactory(
                    studentId = studentId,
                    apiService = appContainer.apiService
                )
                val enrollmentViewModel: com.school.gdsportal.ui.admin.students.enrollment.StudentEnrollmentViewModel = viewModel(factory = factory)
                
                com.school.gdsportal.ui.admin.students.enrollment.StudentEnrollmentScreen(
                    viewModel = enrollmentViewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }
            composable("students/{studentId}/enroll") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.students.enroll.StudentEnrollViewModel.provideFactory(
                    studentId = studentId,
                    apiService = appContainer.apiService
                )
                val enrollViewModel: com.school.gdsportal.ui.admin.students.enroll.StudentEnrollViewModel = viewModel(factory = factory)
                
                com.school.gdsportal.ui.admin.students.enroll.StudentEnrollScreen(
                    viewModel = enrollViewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }
            composable("students/{studentId}/transfer") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.students.transfer.StudentTransferViewModel.provideFactory(
                    studentId = studentId,
                    apiService = appContainer.apiService
                )
                val transferViewModel: com.school.gdsportal.ui.admin.students.transfer.StudentTransferViewModel = viewModel(factory = factory)
                
                com.school.gdsportal.ui.admin.students.transfer.StudentTransferScreen(
                    viewModel = transferViewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }
            composable("students/{studentId}/promote") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")
                StudentPlaceholderScreen(title = "Promote Student ($studentId)", onBack = { navController.navigateUp() })
            }
            composable("students/{studentId}/end-enrollment") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")
                StudentPlaceholderScreen(title = "End Enrollment ($studentId)", onBack = { navController.navigateUp() })
            }
            composable("students/{studentId}/roll-number") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")
                StudentPlaceholderScreen(title = "Change Roll Number ($studentId)", onBack = { navController.navigateUp() })
            }
            composable("students/{studentId}/parents") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.students.parents.StudentParentsViewModel.provideFactory(
                    studentId = studentId,
                    apiService = appContainer.apiService
                )
                val parentsViewModel: com.school.gdsportal.ui.admin.students.parents.StudentParentsViewModel = viewModel(factory = factory)
                
                com.school.gdsportal.ui.admin.students.parents.StudentParentsScreen(
                    viewModel = parentsViewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }
            composable("students/{studentId}/parents/link") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")
                StudentPlaceholderScreen(title = "Link Parent ($studentId)", onBack = { navController.navigateUp() })
            }

            // Parent Module Routes
            composable("parents") {
                val factory = com.school.gdsportal.ui.admin.parents.ParentsDirectoryViewModelFactory(appContainer.apiService)
                val parentsViewModel: com.school.gdsportal.ui.admin.parents.ParentsDirectoryViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.parents.ParentsDirectoryScreen(
                    viewModel = parentsViewModel,
                    onBackClick = { navController.navigateUp() },
                    onParentClick = { parentId ->
                        navController.navigate("parents/$parentId")
                    }
                )
            }
            composable("parents/{parentId}") { backStackEntry ->
                val parentIdStr = backStackEntry.arguments?.getString("parentId")
                val parentId = parentIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.parents.ParentProfileViewModel.Companion.provideFactory(
                    parentId = parentId,
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.parents.ParentProfileViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.parents.ParentProfileScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onViewAllStudentsClick = { navController.navigate("parents/$parentId/students") },
                    onParentInformationClick = { navController.navigate("parents/$parentId/information") }
                )
            }
            composable("parents/{parentId}/information") { backStackEntry ->
                val parentIdStr = backStackEntry.arguments?.getString("parentId")
                val parentId = parentIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.parents.ParentProfileViewModel.Companion.provideFactory(
                    parentId = parentId,
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.parents.ParentProfileViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.parents.ParentInformationScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }
            composable("parents/{parentId}/edit") { backStackEntry ->
                val parentId = backStackEntry.arguments?.getString("parentId")
                ParentPlaceholderScreen(title = "Edit Parent ($parentId)", onBack = { navController.navigateUp() })
            }
            composable("parents/{parentId}/students") { backStackEntry ->
                val parentIdStr = backStackEntry.arguments?.getString("parentId")
                val parentId = parentIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.parents.ParentProfileViewModel.Companion.provideFactory(
                    parentId = parentId,
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.parents.ParentProfileViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.parents.ParentStudentsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
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

@Composable
private fun StudentPlaceholderScreen(title: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back"
            )
        }
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Coming soon")
    }
}

@Composable
private fun ParentPlaceholderScreen(title: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back"
            )
        }
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Coming soon")
    }
}
