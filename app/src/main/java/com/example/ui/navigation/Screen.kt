package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Profile : Screen("profile")
    object AddEditEntry : Screen("add_edit_entry/{entryId}") {
        fun createRoute(entryId: Int) = "add_edit_entry/$entryId"
    }
    object EntryDetail : Screen("entry_detail/{entryId}") {
        fun createRoute(entryId: Int) = "entry_detail/$entryId"
    }
}
