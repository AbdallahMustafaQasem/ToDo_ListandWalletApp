package com.abdallah.taskvault.ui.navigation

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
import com.abdallah.taskvault.R
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.abdallah.taskvault.ui.addedit.AddEditTodoScreen
import com.abdallah.taskvault.ui.calendar.CalendarScreen
import com.abdallah.taskvault.ui.auth.ProfileScreen
import com.abdallah.taskvault.ui.settings.SettingsScreen
import com.abdallah.taskvault.ui.lists.TodoListsScreen
import com.abdallah.taskvault.ui.statistics.StatisticsScreen
import com.abdallah.taskvault.ui.todolist.TodoListScreen
import com.abdallah.taskvault.ui.trash.TrashScreen
import com.abdallah.taskvault.ui.wallet.WalletCategoriesScreen
import com.abdallah.taskvault.ui.wallet.WalletScreen

private val slideFadeIn = slideInHorizontally(tween(300)) { it } + fadeIn(tween(300))
private val slideExit   = slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(200))
private val slidePopEnter = slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300))
private val slidePopExit  = slideOutHorizontally(tween(300)) { it } + fadeOut(tween(200))

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route,
    onNavigateToProfile: () -> Unit = { navController.navigate(Screen.Profile.route) }
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
                onNavigateToTrash      = { navController.navigate(Screen.Trash.route) },
                onNavigateToWallet     = { navController.navigate(Screen.Wallet.route) },
                onNavigateToCalendar   = { navController.navigate(Screen.Calendar.route) },
                onNavigateToLists      = { navController.navigate(Screen.TodoLists.route) },
                onNavigateToSettings   = { navController.navigate(Screen.Settings.route) }
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

        composable(Screen.Calendar.route) {
            CalendarScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { todoId -> navController.navigate(Screen.Detail.createRoute(todoId)) }
            )
        }

        composable(Screen.TodoLists.route) {
            TodoListsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack              = { navController.popBackStack() },
                onNavigateToAbout   = { navController.navigate(Screen.About.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
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
