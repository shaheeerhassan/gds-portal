package com.school.gdsportal.ui.admin.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class NavigationRowData(
    val title: String,
    val route: String,
    val isImplemented: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionLandingPage(
    title: String,
    description: String,
    rows: List<NavigationRowData>,
    onBackClick: () -> Unit,
    onRowClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(rows) { row ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = row.isImplemented) {
                            onRowClick(row.route)
                        }
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = row.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (row.isImplemented) MaterialTheme.colorScheme.onBackground 
                                   else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = if (row.isImplemented) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                    }
                }
            }
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
            }
        }
    }
}
