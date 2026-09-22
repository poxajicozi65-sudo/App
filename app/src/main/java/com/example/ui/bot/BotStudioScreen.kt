package com.example.ui.bot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.bot.canvas.CanvasBuilderView
import com.example.ui.bot.chat.BotChatView
import com.example.ui.bot.preview.BotPreviewView
import com.example.ui.bot.templates.BotIdeaTemplateView
import com.example.ui.projects.ProjectsScreen
import com.example.ui.projects.ProjectsViewModel
import com.example.ui.store.AssetStoreScreen

@Composable
fun BotStudioScreen(
    botViewModel: BotStudioViewModel,
    projectsViewModel: ProjectsViewModel,
    onNewProject: () -> Unit,
    onOpenProject: (Long) -> Unit,
    onExportProject: (Long) -> Unit
) {
    val currentTab by botViewModel.currentTab.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                BotStudioTab.values().forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { botViewModel.selectTab(tab) },
                        icon = {
                            Text(tab.icon, fontSize = if (selected) 20.sp else 17.sp)
                        },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentTab) {
                BotStudioTab.PROJECTS -> {
                    ProjectsScreen(
                        viewModel = projectsViewModel,
                        onNewProject = onNewProject,
                        onOpenProject = onOpenProject,
                        onExportProject = onExportProject,
                        onOpenBotChat = { botViewModel.selectTab(BotStudioTab.AI_CHAT) },
                        onOpenCanvas = { botViewModel.selectTab(BotStudioTab.CANVAS) },
                        onOpenTemplates = { botViewModel.selectTab(BotStudioTab.IDEAS_TEMPLATES) },
                        onOpenStore = { botViewModel.selectTab(BotStudioTab.STORE) }
                    )
                }
                BotStudioTab.STORE -> {
                    AssetStoreScreen()
                }
                BotStudioTab.AI_CHAT -> {
                    BotChatView(viewModel = botViewModel)
                }
                BotStudioTab.CANVAS -> {
                    CanvasBuilderView(viewModel = botViewModel)
                }
                BotStudioTab.IDEAS_TEMPLATES -> {
                    BotIdeaTemplateView(viewModel = botViewModel)
                }
                BotStudioTab.PREVIEW -> {
                    BotPreviewView(viewModel = botViewModel)
                }
            }
        }
    }
}
