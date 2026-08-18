package com.school.gdsportal.ui.admin.administrators

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.Administrator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdministratorsDirectoryScreen(
    isReadOnly: Boolean = false,
    viewModel: AdministratorsDirectoryViewModel,
    onBackClick: () -> Unit,
    onAddAdministratorClick: () -> Unit,
    onAdministratorClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administrators") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    if (!isReadOnly) {
                        IconButton(onClick = onAddAdministratorClick) {
                            Icon(Icons.Default.Add, contentDescription = "Add Administrator")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Screen Header
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Administrators",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Browse and manage administrator records",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        viewModel.onSearchQueryChanged(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by name, phone or employee id") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { 
                                searchQuery = ""
                                viewModel.onSearchQueryChanged("")
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is AdministratorsDirectoryUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is AdministratorsDirectoryUiState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { 
                                viewModel.loadAdministrators()
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                    is AdministratorsDirectoryUiState.Success -> {
                        if (state.administrators.isEmpty()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No administrators found",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "Try a different search." else "Add an administrator to get started.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(state.administrators, key = { it.adminId }) { administrator ->
                                    AdministratorRow(
                                        administrator = administrator,
                                        onClick = { onAdministratorClick(administrator.adminId) }
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdministratorRow(administrator: Administrator, onClick: () -> Unit) {
    val initials = "${administrator.firstName.firstOrNull() ?: ""}${administrator.lastName.firstOrNull() ?: ""}".uppercase()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${administrator.firstName} ${administrator.lastName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = administrator.employeeId,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!administrator.phone.isNullOrBlank()) {
                Text(
                    text = administrator.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "View Details",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
