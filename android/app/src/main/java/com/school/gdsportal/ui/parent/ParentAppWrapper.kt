package com.school.gdsportal.ui.parent

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.school.gdsportal.di.AppContainer
import com.school.gdsportal.ui.parent.components.ParentDrawerContent
import com.school.gdsportal.ui.admin.ProfileLandingScreen
import com.school.gdsportal.ui.admin.profile.ChangePasswordScreen
import com.school.gdsportal.ui.admin.profile.ChangePasswordViewModel
import com.school.gdsportal.ui.admin.profile.ProfileScreen
import com.school.gdsportal.ui.admin.profile.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun ParentAppWrapper(
    appContainer: AppContainer,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "parent_dashboard"

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ParentDrawerContent(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    coroutineScope.launch { drawerState.close() }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = onLogout
            )
        }
    ) {
        val openDrawer: () -> Unit = {
            coroutineScope.launch { drawerState.open() }
        }

        val parentColorScheme = MaterialTheme.colorScheme.copy(
            primary = com.school.gdsportal.ui.theme.AccentParent
        )

        MaterialTheme(colorScheme = parentColorScheme) {
            NavHost(navController = navController, startDestination = "parent_dashboard") {

                // -------------------------------------------------------------
                // DASHBOARD
                // -------------------------------------------------------------
                composable("parent_dashboard") {
                    val viewModel: com.school.gdsportal.ui.parent.dashboard.ParentDashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.parent.dashboard.ParentDashboardViewModel.provideFactory(
                            appContainer.apiService, appContainer.tokenManager
                        )
                    )
                    com.school.gdsportal.ui.parent.dashboard.ParentDashboardScreen(
                        viewModel = viewModel,
                        onMenuClick = openDrawer,
                        onNavigateToChildren = { navController.navigate("my_children") },
                        onNavigateToAnnouncements = { navController.navigate("announcements") },
                        onNavigateToNotifications = { navController.navigate("notifications") },
                        onAnnouncementClick = { id -> navController.navigate("announcements/$id") }
                    )
                }

                // -------------------------------------------------------------
                // MY CHILDREN
                // -------------------------------------------------------------
                composable("my_children") {
                    val viewModel: com.school.gdsportal.ui.parent.children.MyChildrenViewModel = viewModel(
                        factory = com.school.gdsportal.ui.parent.children.MyChildrenViewModel.provideFactory(
                            appContainer.apiService,
                            appContainer.tokenManager
                        )
                    )
                    com.school.gdsportal.ui.parent.children.MyChildrenScreen(
                        viewModel = viewModel,
                        onMenuClick = openDrawer,
                        onChildClick = { studentId ->
                            navController.navigate("children/$studentId")
                        }
                    )
                }

                composable("children/{studentId}") { backStackEntry ->
                    val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                    val viewModel: com.school.gdsportal.ui.parent.children.ChildOverviewViewModel = viewModel(
                        factory = com.school.gdsportal.ui.parent.children.ChildOverviewViewModel.provideFactory(
                            studentId,
                            appContainer.apiService
                        )
                    )
                    com.school.gdsportal.ui.parent.children.ChildOverviewScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.navigateUp() },
                        onNavigateToExamsAndMarks = { navController.navigate("children/$studentId/marks") },
                        onNavigateToAssignments = { navController.navigate("children/$studentId/assignments") },
                        onNavigateToAttendance = { navController.navigate("children/$studentId/attendance") },
                        onNavigateToInfo = { navController.navigate("children/$studentId/info") }
                    )
                }

                composable("children/{studentId}/marks") { backStackEntry ->
                    val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                    val viewModel: com.school.gdsportal.ui.parent.children.marks.ChildMarksViewModel = viewModel(
                        factory = com.school.gdsportal.ui.parent.children.marks.ChildMarksViewModel.provideFactory(
                            studentId, appContainer.apiService
                        )
                    )
                    com.school.gdsportal.ui.parent.children.marks.ChildMarksScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable("children/{studentId}/assignments") { backStackEntry ->
                    val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                    val viewModel: com.school.gdsportal.ui.parent.children.assignments.ChildAssignmentsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.parent.children.assignments.ChildAssignmentsViewModel.provideFactory(
                            studentId, appContainer.apiService
                        )
                    )
                    com.school.gdsportal.ui.parent.children.assignments.ChildAssignmentsScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable("children/{studentId}/info") { backStackEntry ->
                    val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                    val viewModel: com.school.gdsportal.ui.parent.children.info.ChildInfoViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.parent.children.info.ChildInfoViewModel.provideFactory(
                            studentId, appContainer.apiService
                        )
                    )
                    com.school.gdsportal.ui.parent.children.info.ChildInfoScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable("children/{studentId}/attendance") { backStackEntry ->
                    val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                    val viewModel: com.school.gdsportal.ui.parent.children.attendance.ChildAttendanceViewModel = viewModel(
                        factory = com.school.gdsportal.ui.parent.children.attendance.ChildAttendanceViewModel.provideFactory(
                            studentId, appContainer.apiService
                        )
                    )
                    com.school.gdsportal.ui.parent.children.attendance.ChildAttendanceScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.navigateUp() }
                    )
                }

                // -------------------------------------------------------------
                // ANNOUNCEMENTS
                // -------------------------------------------------------------
                composable("announcements") {
                    val viewModel: com.school.gdsportal.ui.parent.announcements.ParentAnnouncementsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.parent.announcements.ParentAnnouncementsViewModel.provideFactory(appContainer.apiService)
                    )
                    com.school.gdsportal.ui.parent.announcements.ParentAnnouncementsScreen(
                        viewModel = viewModel,
                        onMenuClick = openDrawer,
                        onAnnouncementClick = { id -> navController.navigate("announcements/$id") }
                    )
                }

                composable(
                    route = "announcements/{announcementId}",
                    arguments = listOf(navArgument("announcementId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getLong("announcementId") ?: return@composable
                    val viewModel: com.school.gdsportal.ui.admin.communication.announcements.AnnouncementDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.admin.communication.announcements.AnnouncementDetailViewModel.Factory(appContainer.apiService, id)
                    )
                    com.school.gdsportal.ui.admin.communication.announcements.AnnouncementDetailScreen(
                        isReadOnly = true, // Force read-only for Parent
                        viewModel = viewModel,
                        onBackClick = { navController.navigateUp() },
                        onEditClick = { }
                    )
                }

                // -------------------------------------------------------------
                // NOTIFICATIONS
                // -------------------------------------------------------------
                composable("notifications") {
                    val viewModel: com.school.gdsportal.ui.admin.communication.notifications.NotificationsDirectoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.admin.communication.notifications.NotificationsDirectoryViewModel.Factory(appContainer.apiService)
                    )
                    com.school.gdsportal.ui.admin.communication.notifications.NotificationsDirectoryScreen(
                        isReadOnly = true, // Force read-only for Parent
                        viewModel = viewModel,
                        onBackClick = { openDrawer() },
                        onCreateClick = { },
                        onNotificationClick = { id -> navController.navigate("notifications/$id") }
                    )
                }

                composable(
                    route = "notifications/{notificationId}",
                    arguments = listOf(navArgument("notificationId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getLong("notificationId") ?: return@composable
                    val viewModel: com.school.gdsportal.ui.admin.communication.notifications.NotificationDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.admin.communication.notifications.NotificationDetailViewModel.Factory(appContainer.apiService, id)
                    )
                    com.school.gdsportal.ui.admin.communication.notifications.NotificationDetailScreen(
                        isReadOnly = true, // Force read-only for Parent
                        viewModel = viewModel,
                        onBackClick = { navController.navigateUp() }
                    )
                }

                // -------------------------------------------------------------
                // PROFILE & ACCOUNT
                // -------------------------------------------------------------
                composable("parent_profile_landing") {
                    ProfileLandingScreen(
                        onBack = { openDrawer() },
                        onNavigate = { route ->
                            when (route) {
                                "admin_my_profile" -> navController.navigate("parent_my_profile")
                                "admin_change_password" -> navController.navigate("parent_change_password")
                                "logout_action" -> onLogout()
                            }
                        },
                        onLogout = onLogout
                    )
                }

                composable("parent_my_profile") {
                    val profileViewModel: com.school.gdsportal.ui.parent.profile.ParentProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.school.gdsportal.ui.parent.profile.ParentProfileViewModel.provideFactory(appContainer.apiService)
                    )
                    com.school.gdsportal.ui.parent.profile.ParentProfileScreen(
                        viewModel = profileViewModel,
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable("parent_change_password") {
                    val changePasswordViewModel: ChangePasswordViewModel = viewModel(
                        factory = ChangePasswordViewModel.provideFactory(appContainer.apiService)
                    )
                    ChangePasswordScreen(
                        viewModel = changePasswordViewModel,
                        onBackClick = { navController.navigateUp() }
                    )
                }
            } // Closes NavHost
        } // Closes MaterialTheme
    } // Closes ModalNavigationDrawer
} // Closes ParentAppWrapper (Fixed missing brace)

// Temporary placeholder for screens we haven't built yet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScreen(title: String, onMenuClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Coming Soon in Phase 2 & 3!")
        }
    }
}