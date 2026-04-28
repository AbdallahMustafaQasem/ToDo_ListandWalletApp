package com.example.todoapp.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.todoapp.R
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.todoapp.ui.addedit.AddEditTodoScreen
import com.example.todoapp.ui.statistics.StatisticsScreen
import com.example.todoapp.ui.todolist.TodoListScreen
import com.example.todoapp.ui.trash.TrashScreen
import com.example.todoapp.ui.wallet.WalletCategoriesScreen
import com.example.todoapp.ui.wallet.WalletScreen

private val slideFadeIn = slideInHorizontally(tween(300)) { it } + fadeIn(tween(300))
private val slideExit   = slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(200))
private val slidePopEnter = slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300))
private val slidePopExit  = slideOutHorizontally(tween(300)) { it } + fadeOut(tween(200))

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination,
        enterTransition  = { slideFadeIn },
        exitTransition   = { slideExit },
        popEnterTransition  = { slidePopEnter },
        popExitTransition   = { slidePopExit }
    ) {
        composable(Screen.Home.route) {
            TodoListScreen(
                onNavigateToAdd        = { navController.navigate(Screen.Add.route) },
                onNavigateToDetail     = { todoId -> navController.navigate(Screen.Detail.createRoute(todoId)) },
                onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) },
                onNavigateToAbout      = { navController.navigate(Screen.About.route) },
                onNavigateToTrash      = { navController.navigate(Screen.Trash.route) },
                onNavigateToWallet     = { navController.navigate(Screen.Wallet.route) }
            )
        }

        composable(Screen.Add.route) {
            AddEditTodoScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route     = Screen.Detail.route,
            arguments = listOf(navArgument("todoId") { type = NavType.LongType }),
            deepLinks = listOf(navDeepLink { uriPattern = "todoapp://detail/{todoId}" })
        ) {
            AddEditTodoScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.About.route) {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Trash.route) {
            TrashScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Wallet.route) {
            WalletScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCategories = { navController.navigate(Screen.WalletCategories.route) }
            )
        }

        composable(Screen.WalletCategories.route) {
            WalletCategoriesScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.large
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.padding(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
                            Text(stringResource(R.string.about_version), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    HorizontalDivider()
                    Text(
                        stringResource(R.string.about_description),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.about_title),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
