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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import com.abdallah.taskvault.analytics.AnalyticsEntryPoint
import dagger.hilt.android.EntryPointAccessors
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdallah.taskvault.R
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.NavType
import com.abdallah.taskvault.ui.about.AboutScreen
import com.abdallah.taskvault.ui.addedit.AddEditTodoScreen
import com.abdallah.taskvault.ui.assign.AssignedToMeScreen
import com.abdallah.taskvault.ui.assign.AssignmentStatsScreen
import com.abdallah.taskvault.ui.assign.TaskAssignmentScreen
import com.abdallah.taskvault.ui.contacts.ContactListScreen
import com.abdallah.taskvault.ui.calendar.CalendarScreen
import com.abdallah.taskvault.ui.auth.ProfileScreen
import com.abdallah.taskvault.ui.dashboard.DashboardScreen
import com.abdallah.taskvault.ui.bills.BillDetailScreen
import com.abdallah.taskvault.ui.bills.BillListScreen
import com.abdallah.taskvault.ui.habits.HabitDetailScreen
import com.abdallah.taskvault.ui.habits.HabitListScreen
import com.abdallah.taskvault.ui.memoirs.MemoirDetailScreen
import com.abdallah.taskvault.ui.memoirs.MemoirListScreen
import com.abdallah.taskvault.ui.notes.NoteDetailScreen
import com.abdallah.taskvault.ui.notes.NoteListScreen
import com.abdallah.taskvault.ui.passwords.PasswordDetailScreen
import com.abdallah.taskvault.ui.passwords.PasswordListScreen
import com.abdallah.taskvault.ui.pomodoro.PomodoroScreen
import com.abdallah.taskvault.ui.search.SearchScreen
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
    startDestination: String = Screen.Dashboard.route,
    onNavigateToProfile: () -> Unit = { navController.navigate(Screen.Profile.route) }
) {
    val context = LocalContext.current
    val analytics = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AnalyticsEntryPoint::class.java
        ).analyticsHelper()
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(backStackEntry?.destination?.route) {
        val route = backStackEntry?.destination?.route ?: return@LaunchedEffect
        val screenName = route
            .substringBefore("?")
            .substringAfterLast("/")
            .replaceFirstChar { it.uppercase() }
            .ifBlank { route }
        analytics.logScreenView(screenName)
    }

    NavHost(
        navController    = navController,
        startDestination = startDestination,
        enterTransition  = { slideFadeIn },
        exitTransition   = { slideExit },
        popEnterTransition  = { slidePopEnter },
        popExitTransition   = { slidePopExit }
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToTasks      = { navController.navigate(Screen.Home.route) },
                onNavigateToNotes      = { navController.navigate(Screen.Notes.route) },
                onNavigateToMemoirs    = { navController.navigate(Screen.Memoirs.route) },
                onNavigateToPasswords  = { navController.navigate(Screen.Passwords.route) },
                onNavigateToWallet     = { navController.navigate(Screen.Wallet.route) },
                onNavigateToCalendar   = { navController.navigate(Screen.Calendar.route) },
                onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) },
                onNavigateToSearch     = { navController.navigate(Screen.Search.route) },
                onNavigateToHabits     = { navController.navigate(Screen.Habits.route) },
                onNavigateToBills      = { navController.navigate(Screen.Bills.route) },
                onNavigateToSettings   = { navController.navigate(Screen.Settings.route) },
                onNavigateToProfile    = { navController.navigate(Screen.Profile.route) },
                onNavigateToContacts   = { navController.navigate(Screen.Contacts.route) },
                onNavigateToAssign     = { navController.navigate(Screen.TaskAssignment.route) },
                onNavigateToAssignedToMe = { navController.navigate(Screen.AssignedToMe.route) },
                onNavigateToAssignmentStats = { navController.navigate(Screen.AssignmentStats.route) },
                onNavigateToPomodoro = { navController.navigate(Screen.Pomodoro.route) }
            )
        }

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

        composable(Screen.Notes.route) {
            NoteListScreen(
                onNavigateBack     = { navController.popBackStack() },
                onNavigateToDetail = { noteId -> navController.navigate(Screen.NoteDetail.createRoute(noteId)) },
                onNavigateToNew    = { navController.navigate(Screen.NoteDetail.createRoute()) }
            )
        }

        composable(
            route     = Screen.NoteDetail.route,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            NoteDetailScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Memoirs.route) {
            MemoirListScreen(
                onNavigateBack     = { navController.popBackStack() },
                onNavigateToDetail = { memoirId -> navController.navigate(Screen.MemoirDetail.createRoute(memoirId)) },
                onNavigateToNew    = { navController.navigate(Screen.MemoirDetail.createRoute()) }
            )
        }

        composable(
            route     = Screen.MemoirDetail.route,
            arguments = listOf(
                navArgument("memoirId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            MemoirDetailScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Passwords.route) {
            PasswordListScreen(
                onNavigateBack     = { navController.popBackStack() },
                onNavigateToDetail = { id -> navController.navigate(Screen.PasswordDetail.createRoute(id)) },
                onNavigateToNew    = { navController.navigate(Screen.PasswordDetail.createRoute()) }
            )
        }

        composable(
            route     = Screen.PasswordDetail.route,
            arguments = listOf(
                navArgument("passwordId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            PasswordDetailScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateBack    = { navController.popBackStack() },
                onNavigateToTodo  = { navController.navigate(Screen.Detail.createRoute(it)) },
                onNavigateToNote  = { navController.navigate(Screen.NoteDetail.createRoute(it)) },
                onNavigateToMemoir= { navController.navigate(Screen.MemoirDetail.createRoute(it)) },
                onNavigateToPassword={ navController.navigate(Screen.PasswordDetail.createRoute(it)) },
                onNavigateToContacts = { navController.navigate(Screen.Contacts.route) },
                onNavigateToHabit = { navController.navigate(Screen.HabitDetail.createRoute(it)) },
                onNavigateToBill  = { navController.navigate(Screen.BillDetail.createRoute(it)) }
            )
        }

        composable(Screen.Habits.route) {
            HabitListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNew = { navController.navigate(Screen.HabitDetail.createRoute()) },
                onNavigateToEdit = { navController.navigate(Screen.HabitDetail.createRoute(it)) }
            )
        }

        composable(
            route = Screen.HabitDetail.route,
            arguments = listOf(navArgument("habitId") { type = NavType.LongType })
        ) {
            HabitDetailScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Bills.route) {
            BillListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNew = { navController.navigate(Screen.BillDetail.createRoute()) },
                onNavigateToEdit = { navController.navigate(Screen.BillDetail.createRoute(it)) }
            )
        }

        composable(
            route = Screen.BillDetail.route,
            arguments = listOf(navArgument("billId") { type = NavType.LongType })
        ) {
            BillDetailScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Contacts.route) {
            ContactListScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.TaskAssignment.route) {
            TaskAssignmentScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToContacts = { navController.navigate(Screen.Contacts.route) }
            )
        }

        composable(Screen.AssignedToMe.route) {
            AssignedToMeScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.AssignmentStats.route) {
            AssignmentStatsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Pomodoro.route) {
            PomodoroScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

