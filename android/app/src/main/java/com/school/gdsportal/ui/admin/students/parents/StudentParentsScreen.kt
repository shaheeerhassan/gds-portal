package com.school.gdsportal.ui.admin.students.parents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.data.remote.Parent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentParentsScreen(
    viewModel: StudentParentsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parents & Guardians") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.retry() }) {
                        Text("Retry")
                    }
                }
            }
        } else {
            val parents = uiState.parents
            if (parents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("No parents or guardians linked.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(parents, key = { it.parentId }) { parent ->
                        ParentCard(
                            parent = parent,
                            onUnlinkClick = { viewModel.unlinkParent(parent.parentId) }
                        )
                    }
                }
            }
        }
        
        var showLinkDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
        
        if (showLinkDialog) {
            LinkParentDialog(
                onDismiss = { showLinkDialog = false },
                onConfirm = { parentId, relType, isPrimary -> 
                    showLinkDialog = false
                    viewModel.linkParent(parentId, relType, isPrimary)
                }
            )
        }
        
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            FloatingActionButton(
                onClick = { showLinkDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Link Parent")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LinkParentDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long, String, Boolean) -> Unit
) {
    var parentIdStr by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var selectedRel by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("FATHER") }
    var isPrimary by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    val relTypes = listOf("FATHER", "MOTHER", "GUARDIAN", "OTHER")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Link Parent") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = parentIdStr,
                    onValueChange = { parentIdStr = it },
                    label = { Text("Parent ID *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedRel,
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
                                    selectedRel = rel
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Checkbox(
                        checked = isPrimary,
                        onCheckedChange = { isPrimary = it }
                    )
                    Text("Primary Contact")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val pid = parentIdStr.toLongOrNull()
                    if (pid != null) {
                        onConfirm(pid, selectedRel, isPrimary)
                    }
                },
                enabled = parentIdStr.isNotBlank() && parentIdStr.toLongOrNull() != null
            ) {
                Text("Link")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ParentCard(parent: Parent, onUnlinkClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${parent.firstName} ${parent.lastName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                var showUnlinkConfirm by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                
                IconButton(onClick = { showUnlinkConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Unlink", tint = MaterialTheme.colorScheme.error)
                }
                
                if (showUnlinkConfirm) {
                    AlertDialog(
                        onDismissRequest = { showUnlinkConfirm = false },
                        title = { Text("Unlink Parent?") },
                        text = { Text("Are you sure you want to unlink ${parent.firstName} ${parent.lastName} from this student?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showUnlinkConfirm = false
                                    onUnlinkClick()
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) { Text("Unlink") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showUnlinkConfirm = false }) { Text("Cancel") }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Parent ID: ${parent.parentId}", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(16.dp))
            
            InfoRow(label = "Phone", value = parent.phone ?: "-")
            InfoRow(label = "Occupation", value = parent.occupation ?: "-")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
