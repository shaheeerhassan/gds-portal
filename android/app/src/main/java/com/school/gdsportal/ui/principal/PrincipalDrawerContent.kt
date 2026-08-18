package com.school.gdsportal.ui.principal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.school.gdsportal.ui.theme.AccentPrincipal

@Composable
fun PrincipalDrawerContent(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AccentPrincipal)
                .padding(24.dp)
        ) {
            Text(
                text = "GDS Portal",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Principal Portal",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            label = { Text("Dashboard") },
            selected = currentRoute == "principal_dashboard",
            onClick = { onNavigate("principal_dashboard") },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        NavigationDrawerItem(
            label = { Text("Academics") },
            selected = currentRoute.startsWith("admin_academics") ||
                    currentRoute.startsWith("academic-years") ||
                    currentRoute.startsWith("classes") ||
                    currentRoute.startsWith("sections") ||
                    currentRoute.startsWith("subjects") ||
                    currentRoute.startsWith("periods"),
            onClick = { onNavigate("admin_academics_landing") },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider()

        NavigationDrawerItem(
            label = { Text("Logout") },
            selected = false,
            onClick = onLogout,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}