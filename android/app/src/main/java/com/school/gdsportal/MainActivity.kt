package com.school.gdsportal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.rememberCoroutineScope
import com.school.gdsportal.ui.admin.AdminAppWrapper
import com.school.gdsportal.ui.login.LoginScreen
import com.school.gdsportal.ui.login.LoginViewModel
import com.school.gdsportal.ui.theme.GDSPortalTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GDSPortalTheme {
                val appContainer = (application as GdsApplication).container
                val navController = rememberNavController()
                val coroutineScope = rememberCoroutineScope()

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
                                navController.navigate("admin_home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("admin_home") {
                        AdminAppWrapper(
                            appContainer = appContainer,
                            onLogout = {
                                coroutineScope.launch { appContainer.tokenManager.clearSession() }
                                navController.navigate("login") {
                                    popUpTo("admin_home") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
