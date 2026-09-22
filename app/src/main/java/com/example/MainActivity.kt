package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.bot.BotStudioScreen
import com.example.ui.bot.BotStudioViewModel
import com.example.ui.builder.BuilderScreen
import com.example.ui.builder.BuilderViewModel
import com.example.ui.export.ExportScreen
import com.example.ui.export.ExportViewModel
import com.example.ui.projects.ProjectsViewModel
import com.example.ui.theme.MiniAppForgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiniAppForgeTheme {
                MiniAppForgeNavHost()
            }
        }
    }
}

private const val ROUTE_PROJECTS = "projects"
private const val ROUTE_BUILDER = "builder/{projectId}"
private const val ROUTE_EXPORT = "export/{projectId}"

@Composable
fun MiniAppForgeNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ROUTE_PROJECTS,
        modifier = Modifier.fillMaxSize()
    ) {
        // Main Bot Studio Screen with Bottom Navigation (Forge, AI Chat, Canvas, Ideas & Kits, Simulator)
        composable(ROUTE_PROJECTS) {
            val projectsViewModel: ProjectsViewModel = viewModel()
            val botViewModel: BotStudioViewModel = viewModel()
            BotStudioScreen(
                botViewModel = botViewModel,
                projectsViewModel = projectsViewModel,
                onNewProject = {
                    navController.navigate("builder/0")
                },
                onOpenProject = { projectId ->
                    navController.navigate("builder/$projectId")
                },
                onExportProject = { projectId ->
                    navController.navigate("export/$projectId")
                }
            )
        }

        // Builder Flow Screen
        composable(
            route = ROUTE_BUILDER,
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
            val builderViewModel: BuilderViewModel = viewModel()
            LaunchedEffect(projectId) {
                builderViewModel.loadProject(projectId)
            }
            BuilderScreen(
                viewModel = builderViewModel,
                onNavigateBack = { navController.popBackStack() },
                onGenerated = { savedId ->
                    navController.navigate("export/$savedId") {
                        popUpTo(ROUTE_PROJECTS)
                    }
                }
            )
        }

        // Export & AI Refinement Screen
        composable(
            route = ROUTE_EXPORT,
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
            val exportViewModel: ExportViewModel = viewModel()
            LaunchedEffect(projectId) {
                exportViewModel.loadProject(projectId)
            }
            ExportScreen(
                viewModel = exportViewModel,
                onNavigateBack = {
                    navController.popBackStack(ROUTE_PROJECTS, false)
                }
            )
        }
    }
}
