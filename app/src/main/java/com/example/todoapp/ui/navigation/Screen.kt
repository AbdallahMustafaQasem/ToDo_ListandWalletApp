package com.example.todoapp.ui.navigation

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
}
