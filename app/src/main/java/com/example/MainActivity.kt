package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.JournalViewModel
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: JournalViewModel = viewModel()
                val authState by viewModel.authState.collectAsStateWithLifecycle()
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Dynamically set startDestination based on verified logged-in session state
                    val startDest = if (authState.isLoggedIn) Screen.Dashboard.route else Screen.Login.route

                    // Make sure that when startDest changes, we sync NavController destination appropriately
                    LaunchedEffect(authState.isLoggedIn) {
                        if (authState.isLoggedIn) {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = startDest,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // User Authentication Login OTP verification Screen
                        composable(Screen.Login.route) {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = {
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Feed Dashboard list Screen
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToAddEdit = { entryId ->
                                    navController.navigate(Screen.AddEditEntry.createRoute(entryId))
                                },
                                onNavigateToDetail = { entryId ->
                                    navController.navigate(Screen.EntryDetail.createRoute(entryId))
                                },
                                onNavigateToProfile = {
                                    navController.navigate(Screen.Profile.route)
                                }
                            )
                        }

                        // Composer editor Screen (Adds new or edit entry)
                        composable(
                            route = Screen.AddEditEntry.route,
                            arguments = listOf(navArgument("entryId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val entryId = backStackEntry.arguments?.getInt("entryId") ?: -1
                            AddEditEntryScreen(
                                entryId = entryId,
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // Detail Viewer Screen
                        composable(
                            route = Screen.EntryDetail.route,
                            arguments = listOf(navArgument("entryId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val entryId = backStackEntry.arguments?.getInt("entryId") ?: -1
                            EntryDetailScreen(
                                entryId = entryId,
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToEdit = { id ->
                                    navController.navigate(Screen.AddEditEntry.createRoute(id))
                                }
                            )
                        }

                        // Profile, Reminders & Cloud Sync settings Screen
                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onLogout = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

