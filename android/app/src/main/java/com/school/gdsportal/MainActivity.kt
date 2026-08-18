package com.school.gdsportal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.school.gdsportal.ui.admin.AdminAppWrapper
import com.school.gdsportal.ui.principal.PrincipalAppWrapper
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
                val coroutineScope = rememberCoroutineScope()

                // Collect states directly from DataStore. Null means not logged in.
                val role by appContainer.tokenManager.roleFlow.collectAsState(initial = "LOADING")
                val token by appContainer.tokenManager.tokenFlow.collectAsState(initial = "LOADING")

                val onLogout: () -> Unit = {
                    coroutineScope.launch { appContainer.tokenManager.clearSession() }
                }

                when {
                    role == "LOADING" || token == "LOADING" -> {
                        // Flat, modern loading state while DataStore initializes
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    token == null || role == null -> {
                        val loginViewModel: LoginViewModel = viewModel(
                            factory = LoginViewModel.provideFactory(
                                appContainer.apiService,
                                appContainer.tokenManager
                            )
                        )
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = {
                                // Intentionally left blank.
                            }
                        )
                    }
                    role?.uppercase() == "ADMINISTRATOR" -> {
                        AdminAppWrapper(
                            appContainer = appContainer,
                            onLogout = onLogout
                        )
                    }
                    role?.uppercase() == "PRINCIPAL" -> {
                        PrincipalAppWrapper(
                            appContainer = appContainer,
                            onLogout = onLogout
                        )
                    }
                    else -> {
                        // Failsafe for unhandled or unexpected roles
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            androidx.compose.foundation.layout.Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                androidx.compose.material3.Text("Unsupported App Role: $role")
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                                androidx.compose.material3.Button(onClick = onLogout) {
                                    androidx.compose.material3.Text("Logout")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}