package com.school.gdsportal.ui.admin.academics.periods

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodCreateScreen(
    viewModel: PeriodCreateViewModel,
    onBackClick: () -> Unit,
    onPeriodCreated: (Int?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.createdPeriodId) {
        if (uiState.createdPeriodId != null) {
            val id = if (uiState.createdPeriodId == -1) null else uiState.createdPeriodId
            onPeriodCreated(id)
        }
    }

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

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    if (showStartTimePicker) {
        TimePickerDialogCustom(
            onDismissRequest = { showStartTimePicker = false },
            onTimeSelected = { hour, minute ->
                val timeString = String.format("%02d:%02d:00", hour, minute)
                viewModel.updateForm(startTime = timeString)
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialogCustom(
            onDismissRequest = { showEndTimePicker = false },
            onTimeSelected = { hour, minute ->
                val timeString = String.format("%02d:%02d:00", hour, minute)
                viewModel.updateForm(endTime = timeString)
                showEndTimePicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Period") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    "PERIOD DETAILS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = uiState.periodNumber,
                    onValueChange = { viewModel.updateForm(periodNumber = it) },
                    label = { Text("Period Number *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = if (uiState.startTime.isNotEmpty()) formatTime(uiState.startTime) else "",
                    onValueChange = { },
                    label = { Text("Start Time *") },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable { showStartTimePicker = true }
                )

                OutlinedTextField(
                    value = if (uiState.endTime.isNotEmpty()) formatTime(uiState.endTime) else "",
                    onValueChange = { },
                    label = { Text("End Time *") },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                        .clickable { showEndTimePicker = true }
                )

                Button(
                    onClick = { viewModel.createPeriod() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Period")
                }
            }

            if (uiState.isSaving) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogCustom(
    onDismissRequest: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    val timePickerState = rememberTimePickerState()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}
