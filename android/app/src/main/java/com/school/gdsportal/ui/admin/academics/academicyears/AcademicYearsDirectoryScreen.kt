package com.school.gdsportal.ui.admin.academics.academicyears

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.AcademicYear
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicYearsDirectoryScreen(
    isReadOnly: Boolean = false,
    viewModel: AcademicYearsDirectoryViewModel,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onYearClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Refresh when navigating back
    LaunchedEffect(Unit) {
        viewModel.loadAcademicYears()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Academic Years") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isReadOnly) {
                        IconButton(onClick = onAddClick) {
                            Icon(Icons.Default.Add, contentDescription = "Add Academic Year")
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
                    text = "Academic Years",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Manage academic sessions and the current academic year",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is AcademicYearsDirectoryUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is AcademicYearsDirectoryUiState.Error -> {
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
                            Button(onClick = { viewModel.loadAcademicYears() }) {
                                Text("Retry")
                            }
                        }
                    }
                    is AcademicYearsDirectoryUiState.Success -> {
                        if (state.academicYears.isEmpty()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No academic years found",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Add an academic year to get started.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(state.academicYears, key = { it.academicYearId }) { academicYear ->
                                    AcademicYearRow(
                                        academicYear = academicYear,
                                        onClick = { onYearClick(academicYear.academicYearId) }
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
fun AcademicYearRow(academicYear: AcademicYear, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = academicYear.yearName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            val formattedStartDate = formatDateString(academicYear.startDate)
            val formattedEndDate = formatDateString(academicYear.endDate)
            
            Text(
                text = "$formattedStartDate – $formattedEndDate",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (academicYear.isCurrent) {
            val statusColor = androidx.compose.ui.graphics.Color(0xFF4CAF50)
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Text(
                    text = "Current",
                    color = statusColor,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold
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

fun formatDateString(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr)
        date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
    } catch (e: DateTimeParseException) {
        dateStr
    }
}
