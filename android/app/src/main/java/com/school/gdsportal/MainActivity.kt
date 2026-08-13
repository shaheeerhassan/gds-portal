package com.school.gdsportal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.school.gdsportal.GdsApplication
import com.school.gdsportal.ui.admin.dashboard.AdminDashboardScreen
import com.school.gdsportal.ui.admin.dashboard.AdminDashboardViewModel
import com.school.gdsportal.ui.login.LoginScreen
import com.school.gdsportal.ui.login.LoginViewModel
import com.school.gdsportal.ui.theme.GDSPortalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GDSPortalTheme {
                val appContainer = (application as GdsApplication).container
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        val loginViewModel: LoginViewModel = viewModel(
                            factory = LoginViewModel.provideFactory(
                                appContainer.apiService,
                                appContainer.tokenManager
                            )
                        )

                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = {
                                navController.navigate("admin_dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("admin_dashboard") {
                        val dashboardViewModel: AdminDashboardViewModel = viewModel(
                            factory = AdminDashboardViewModel.provideFactory(
                                appContainer.apiService,
                                appContainer.tokenManager
                            )
                        )

                        AdminDashboardScreen(
                            viewModel = dashboardViewModel,
                            onNavigateToProfile = { /* TODO */ },
                            onNavigateToNotifications = { /* TODO */ }
                        )
                    }
                }
            }
        }
    }
}
