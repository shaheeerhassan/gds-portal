package com.school.gdsportal.ui.admin.students.parents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddParentScreen(
    viewModel: AddParentViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Parent & Guardian") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = uiState.currentTab) {
                Tab(
                    selected = uiState.currentTab == 0,
                    onClick = { viewModel.setTab(0) },
                    text = { Text("Search Existing") }
                )
                Tab(
                    selected = uiState.currentTab == 1,
                    onClick = { viewModel.setTab(1) },
                    text = { Text("Create New") }
                )
            }

            if (uiState.submitError != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = uiState.submitError!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (uiState.currentTab == 0) {
                SearchExistingTab(viewModel, uiState)
            } else {
                CreateNewTab(viewModel, uiState)
            }
        }
        
        if (uiState.selectedParentId != null && uiState.currentTab == 0) {
            LinkParentBottomSheet(
                uiState = uiState,
                onDismiss = { viewModel.clearSelectedParent() },
                onRelationshipChange = { viewModel.updateRelationship(it) },
                onPrimaryContactChange = { viewModel.updatePrimaryContact(it) },
                onSubmit = { viewModel.submitLinkExisting() }
            )
        }
    }
}

@Composable
private fun SearchExistingTab(viewModel: AddParentViewModel, uiState: AddParentUiState) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            label = { Text("Search parents by name, phone...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            singleLine = true
        )

        if (uiState.isSearching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.searchError != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.searchError, color = MaterialTheme.colorScheme.error)
            }
        } else if (uiState.searchResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No parents found. Try a different search or create a new one.")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.searchResults) { parent ->
                    ListItem(
                        headlineContent = { Text("${parent.firstName} ${parent.lastName}") },
                        supportingContent = { 
                            Text("${parent.phone ?: "No phone"} • ${parent.occupation ?: "No occupation"}") 
                        },
                        leadingContent = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        trailingContent = {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        },
                        modifier = Modifier.clickable {
                            viewModel.selectParentToLink(parent.parentId)
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateNewTab(viewModel: AddParentViewModel, uiState: AddParentUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Personal Information", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = uiState.firstName,
            onValueChange = { viewModel.updateCreateField("firstName", it) },
            label = { Text("First Name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.lastName,
            onValueChange = { viewModel.updateCreateField("lastName", it) },
            label = { Text("Last Name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.phone,
            onValueChange = { viewModel.updateCreateField("phone", it) },
            label = { Text("Phone Number *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.occupation,
            onValueChange = { viewModel.updateCreateField("occupation", it) },
            label = { Text("Occupation") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))
        Text("Account Credentials", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.updateCreateField("email", it) },
            label = { Text("Email *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = uiState.username,
            onValueChange = { viewModel.updateCreateField("username", it) },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.updateCreateField("password", it) },
            label = { Text("Password *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))
        Text("Relationship to Student", style = MaterialTheme.typography.titleMedium)

        RelationshipDropdown(
            selectedRelationship = uiState.relationship,
            onRelationshipSelected = { viewModel.updateRelationship(it) }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = uiState.isPrimaryContact,
                onCheckedChange = { viewModel.updatePrimaryContact(it) }
            )
            Text("Set as Primary Contact")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.submitCreateNew() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSubmitting && uiState.isCreateFormValid
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Create & Link Parent")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LinkParentBottomSheet(
    uiState: AddParentUiState,
    onDismiss: () -> Unit,
    onRelationshipChange: (String) -> Unit,
    onPrimaryContactChange: (Boolean) -> Unit,
    onSubmit: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Link Parent to Student", style = MaterialTheme.typography.titleLarge)
            
            RelationshipDropdown(
                selectedRelationship = uiState.relationship,
                onRelationshipSelected = onRelationshipChange
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = uiState.isPrimaryContact,
                    onCheckedChange = onPrimaryContactChange
                )
                Text("Set as Primary Contact")
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSubmitting
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Confirm Link")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RelationshipDropdown(
    selectedRelationship: String,
    onRelationshipSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val relTypes = listOf("FATHER", "MOTHER", "GUARDIAN", "OTHER")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedRelationship,
            onValueChange = {},
            readOnly = true,
            label = { Text("Relationship *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            relTypes.forEach { rel ->
                DropdownMenuItem(
                    text = { Text(rel) },
                    onClick = {
                        onRelationshipSelected(rel)
                        expanded = false
                    }
                )
            }
        }
    }
}
