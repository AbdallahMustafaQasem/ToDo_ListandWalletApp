package com.abdallah.taskvault.ui.navigation

sealed class Screen(val route: String) {
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
}
