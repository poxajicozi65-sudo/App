package com.example.ui.projects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FormData
import com.example.data.model.Project
import com.example.ui.components.ApiKeySettingsDialog
import com.example.ui.components.DisclaimerDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    viewModel: ProjectsViewModel,
    onNewProject: () -> Unit,
    onOpenProject: (Long) -> Unit,
    onExportProject: (Long) -> Unit,
    onOpenBotChat: () -> Unit = {},
    onOpenCanvas: () -> Unit = {},
    onOpenTemplates: () -> Unit = {},
    onOpenStore: () -> Unit = {}
) {
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val hasSeenDisclaimer by viewModel.hasSeenDisclaimer.collectAsStateWithLifecycle()
    val apiKey by viewModel.geminiApiKey.collectAsStateWithLifecycle()

    var showDisclaimerDialog by remember { mutableStateOf(!hasSeenDisclaimer) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<Project?>(null) }

    // First-launch disclaimer
    if (showDisclaimerDialog) {
        DisclaimerDialog(
            onDismiss = {
                showDisclaimerDialog = false
                viewModel.markDisclaimerSeen()
            },
            onAccept = {
                showDisclaimerDialog = false
                viewModel.markDisclaimerSeen()
            }
        )
    }

    if (showApiKeyDialog) {
        ApiKeySettingsDialog(
            currentKey = apiKey,
            onDismiss = { showApiKeyDialog = false },
            onSaveKey = { newKey ->
                viewModel.saveGeminiApiKey(newKey)
                showApiKeyDialog = false
            }
        )
    }

    projectToDelete?.let { proj ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("Delete Project?") },
            text = { Text("Are you sure you want to delete \"${proj.name}\"? Generated files will remain in your private cache unless re-generated.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProject(proj.id)
                        projectToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("⚡", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "MiniApp Forge",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Telegram Mini App Builder",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showApiKeyDialog = true },
                        modifier = Modifier.testTag("settings_api_key_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "API Key Settings",
                            tint = if (apiKey.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewProject,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Mini App") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("new_project_fab")
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (projects.isEmpty()) {
                EmptyProjectsState(onNewProject = onNewProject)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        BannerHeaderCard(
                            onOpenBotChat = onOpenBotChat,
                            onOpenCanvas = onOpenCanvas,
                            onOpenTemplates = onOpenTemplates,
                            onOpenStore = onOpenStore
                        )
                    }
                    items(projects, key = { it.id }) { project ->
                        ProjectItemCard(
                            project = project,
                            onOpen = { onOpenProject(project.id) },
                            onExport = { onExportProject(project.id) },
                            onDuplicate = { viewModel.duplicateProject(project) },
                            onDelete = { projectToDelete = project }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BannerHeaderCard(
    onOpenBotChat: () -> Unit = {},
    onOpenCanvas: () -> Unit = {},
    onOpenTemplates: () -> Unit = {},
    onOpenStore: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Telegram Mini Bot & App Forge",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Build Watch & Earn apps or design interactive Telegram Mini Bots with Gemini AI & visual Canvas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("⚡", fontSize = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick feature shortcuts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenBotChat() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("💬", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenCanvas() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🎨", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Canvas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenStore() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🛍️", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Store", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenTemplates() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("💡", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Kits", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyProjectsState(onNewProject: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(88.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "No Mini Apps Yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Build a complete Watch & Earn Telegram Mini App with admin panel and Firestore rules in minutes.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNewProject,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.testTag("create_first_project_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create First Mini App")
        }
    }
}

@Composable
fun ProjectItemCard(
    project: Project,
    onOpen: () -> Unit,
    onExport: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val formData = remember(project.formDataJson) {
        FormData.fromJson(project.formDataJson)
    }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault()) }
    val formattedDate = remember(project.updatedAt) { dateFormat.format(Date(project.updatedAt)) }

    val accentColor = remember(formData.accentColor) {
        try {
            Color(android.graphics.Color.parseColor(formData.accentColor))
        } catch (e: Exception) {
            Color(0xFF0284C7)
        }
    }

    val activeFeatureCount = remember(formData) {
        listOf(
            formData.enableWatchEarn,
            formData.enableVisitEarn,
            formData.enableSocialTasks,
            formData.enableColorGuess,
            formData.enableSpinWheel,
            formData.enableQuizGame,
            formData.enableTicTacToe,
            formData.enableReferral,
            formData.enableWithdrawal
        ).count { it }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_card_${project.id}")
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Accent color indicator circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (project.name.isNotBlank()) project.name.take(1).uppercase() else "M",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "Updated $formattedDate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Delete button
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "$activeFeatureCount Features",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = formData.theme.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${formData.coinRateCoins} = ${formData.currencySymbol}${formData.coinRateCurrency}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpen,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Form", fontSize = 13.sp)
                }

                Button(
                    onClick = onExport,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export", fontSize = 13.sp)
                }

                IconButton(
                    onClick = onDuplicate,
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
