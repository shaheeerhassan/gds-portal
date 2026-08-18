package com.school.gdsportal.ui.principal

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.school.gdsportal.ui.theme.AccentPrincipal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrincipalDashboardScreen(
    onMenuClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AccentPrincipal,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome, Principal",
                style = MaterialTheme.typography.headlineMedium,
                color = AccentPrincipal
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Open the side menu to view Academics data.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}