package com.school.gdsportal.ui.admin.academics.academicyears

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicYearDetailScreen(
    isReadOnly: Boolean = false,
    viewModel: AcademicYearDetailViewModel,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showSetCurrentDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAcademicYear()
    }

    if (showSetCurrentDialog && uiState.academicYear != null) {
        AlertDialog(
            onDismissRequest = { showSetCurrentDialog = false },
            title = { Text("Set as Current") },
            text = { Text("Set ${uiState.academicYear!!.yearName} as the current academic year?\n\nThis will make it the active academic year.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSetCurrentDialog = false
                        viewModel.setAsCurrent()
                    }
                ) {
                    Text("Set Current")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetCurrentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Academic Year") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Hide the entire options menu in read-only mode
                    if (!isReadOnly && uiState.academicYear != null) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Academic Year") },
                                onClick = {
                                    showMenu = false
                                    onEditClick()
                                }
                            )
                            if (!uiState.academicYear!!.isCurrent) {
                                DropdownMenuItem(
                                    text = { Text("Set as Current") },
                                    onClick = {
                                        showMenu = false
                                        showSetCurrentDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadAcademicYear() }) {
                            Text("Retry")
                        }
                    }
                }
                uiState.academicYear != null -> {
                    val year = uiState.academicYear!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (uiState.isSettingCurrent) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Text(
                            text = year.yearName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (year.isCurrent) {
                              val statusColor = androidx.compose.ui.graphics.Color(0xFF4CAF50)
                              Surface(
                                  color = statusColor.copy(alpha = 0.1f),
                                  shape = MaterialTheme.shapes.small,
                                  modifier = Modifier.padding(vertical = 4.dp)
                              ) {
                                  Text(
                                      text = "Current Academic Year",
                                      color = statusColor,
                                      style = MaterialTheme.typography.labelMedium,
                                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                      fontWeight = FontWeight.Bold
                                  )
                              }
                          }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "ACADEMIC YEAR DETAILS",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    DetailField("Year Name", year.yearName)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                    DetailField("Start Date", formatDateString(year.startDate))
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                    DetailField("End Date", formatDateString(year.endDate))
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
