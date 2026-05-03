package com.abdallah.taskvault.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard  : Screen("dashboard")
    object Home       : Screen("home")
    object Add        : Screen("add")
    object Detail     : Screen("detail/{todoId}") {
        fun createRoute(todoId: Long) = "detail/$todoId"
    }
    object About      : Screen("about")
    object Statistics : Screen("statistics")
    object Trash      : Screen("trash")
    object Wallet     : Screen("wallet")
    object WalletCategories : Screen("wallet/categories")
    object Calendar   : Screen("calendar")
    object TodoLists  : Screen("lists")
    object Login      : Screen("login")
    object Profile    : Screen("profile")
    object Settings   : Screen("settings")
    object Notes      : Screen("notes")
    object NoteDetail : Screen("notes/detail?noteId={noteId}") {
        fun createRoute(noteId: Long = -1L) = "notes/detail?noteId=$noteId"
    }
    object Memoirs    : Screen("memoirs")
    object MemoirDetail : Screen("memoirs/detail?memoirId={memoirId}") {
        fun createRoute(memoirId: Long = -1L) = "memoirs/detail?memoirId=$memoirId"
    }
    object Passwords    : Screen("passwords")
    object PasswordDetail : Screen("passwords/detail?passwordId={passwordId}") {
        fun createRoute(passwordId: Long = -1L) = "passwords/detail?passwordId=$passwordId"
    }
    object Search       : Screen("search")
    object Habits       : Screen("habits")
    object HabitDetail  : Screen("habits/detail?habitId={habitId}") {
        fun createRoute(habitId: Long = -1L) = "habits/detail?habitId=$habitId"
    }
    object Bills        : Screen("bills")
    object BillDetail   : Screen("bills/detail?billId={billId}") {
        fun createRoute(billId: Long = -1L) = "bills/detail?billId=$billId"
    }
    object Contacts     : Screen("contacts")
    object TaskAssignment : Screen("assign")
    object AssignedToMe : Screen("assigned_to_me")
    object AssignmentStats : Screen("assign_stats")
}
