package com.example.smartattendance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartattendance.ui.screens.LoginScreen
import com.example.smartattendance.ui.screens.HomeScreen
import com.example.smartattendance.ui.screens.AttendanceScreen
import com.example.smartattendance.ui.screens.HistoryScreen
import com.example.smartattendance.ui.screens.RequestPermissionScreen
import com.example.smartattendance.ui.screens.CreatePermissionRequestScreen
import com.example.smartattendance.ui.screens.CameraScreen
import com.example.smartattendance.ui.screens.ScheduleScreen
import com.example.smartattendance.ui.screens.ListScreen
import com.example.smartattendance.ui.components.AppBottomNavigation
import com.example.smartattendance.ui.theme.SmartAttendanceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartAttendanceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "login"

    // Routes where bottom navigation should be shown
    val routesWithBottomNav = setOf("home", "attendance", "history", "list", "schedule", "request_permission", "create_permission_request")

    Scaffold(
        bottomBar = {
            if (currentRoute in routesWithBottomNav) {
                AppBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        when (route) {
                            "home" -> {
                                // When navigating to home, clear back stack
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            else -> {
                                navController.navigate(route) {
                                    // Pop up to the graph's start destination
                                    popUpTo("home") { saveState = true }
                                    // Avoid multiple copies of the same destination
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("login") {
                LoginScreen(
                    onLoginClick = { username, password ->
                        if (username.isNotEmpty() && password.isNotEmpty()) {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable("home") {
                HomeScreen(
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    },
                    onSubmitAttendance = {
                        navController.navigate("attendance")
                    },
                    onDateClick = {
                        navController.navigate("history")
                    },
                    onRequestPermission = {
                        navController.navigate("request_permission")
                    }
                )
            }

            composable("attendance") {
                AttendanceScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onTakePhoto = {
                        navController.navigate("camera")
                    },
                    onAskPermission = {
                        navController.navigate("request_permission")
                    }
                )
            }

            composable("camera") {
                CameraScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onPhotoTaken = { bitmap ->
                        // Photo captured successfully, navigate back to attendance
                        navController.popBackStack()
                    }
                )
            }

            composable("history") {
                HistoryScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // List screen for center navigation
            composable("list") {
                ListScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigate = { route ->
                        when (route) {
                            "home" -> {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            else -> {
                                navController.navigate(route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                )
            }

            composable("schedule") {
                ScheduleScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigate = { route ->
                        when (route) {
                            "home" -> {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            else -> {
                                navController.navigate(route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                )
            }

            composable("request_permission") {
                RequestPermissionScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onCreateRequest = {
                        navController.navigate("create_permission_request")
                    },
                    onNavigate = { route ->
                        when (route) {
                            "home" -> {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            else -> {
                                navController.navigate(route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                )
            }

            composable("create_permission_request") {
                CreatePermissionRequestScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSubmit = {
                        // Navigate back to permission list after submission
                        navController.popBackStack()
                    },
                    onNavigate = { route ->
                        when (route) {
                            "home" -> {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            else -> {
                                navController.navigate(route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}