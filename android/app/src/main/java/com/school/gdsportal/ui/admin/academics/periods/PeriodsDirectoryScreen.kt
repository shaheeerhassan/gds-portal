package com.school.gdsportal.ui.admin.academics.periods

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.school.gdsportal.data.remote.Period
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodsDirectoryScreen(
    isReadOnly: Boolean = false,
    viewModel: PeriodsDirectoryViewModel,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onPeriodClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Error") },
            text = { Text(uiState.error!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Periods", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Manage daily school periods",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isReadOnly) {
                        IconButton(onClick = onAddClick) {
                            Icon(Icons.Default.Add, contentDescription = "Create Period")
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
            if (uiState.periods.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No periods found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.periods, key = { it.periodId }) { period ->
                        PeriodRow(
                            period = period,
                            onClick = { onPeriodClick(period.periodId) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun PeriodRow(
    period: Period,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Period ${period.periodNumber}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${formatTime(period.startTime)} – ${formatTime(period.endTime)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = ">",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

fun formatTime(timeString: String): String {
    return try {
        val formatter24 = DateTimeFormatter.ofPattern("HH:mm:ss")
        val formatter12 = DateTimeFormatter.ofPattern("hh:mm a")
        val localTime = LocalTime.parse(timeString, formatter24)
        localTime.format(formatter12)
    } catch (e: Exception) {
        try {
            val formatter24Short = DateTimeFormatter.ofPattern("HH:mm")
            val formatter12 = DateTimeFormatter.ofPattern("hh:mm a")
            val localTime = LocalTime.parse(timeString, formatter24Short)
            localTime.format(formatter12)
        } catch (e2: Exception) {
            timeString // fallback
        }
    }
}
