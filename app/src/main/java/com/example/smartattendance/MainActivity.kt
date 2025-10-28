package com.example.smartattendance

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartattendance.api.AuthApi
import com.example.smartattendance.ui.components.AppBottomNavigation
import com.example.smartattendance.ui.screens.AttendanceScreen
import com.example.smartattendance.ui.screens.CameraScreen
import com.example.smartattendance.ui.screens.HistoryScreen
import com.example.smartattendance.ui.screens.HomeScreen
import com.example.smartattendance.ui.screens.ListScreen
import com.example.smartattendance.ui.screens.LoginScreen
import com.example.smartattendance.ui.screens.ScheduleScreen
import com.example.smartattendance.ui.screens.SignUpScreen
import com.example.smartattendance.ui.screens.SubmissionCompleteScreen
import com.example.smartattendance.ui.screens.SubmitAttendanceScreen
import com.example.smartattendance.ui.theme.SmartAttendanceTheme
import com.example.smartattendance.utils.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This enables edge-to-edge display and makes the status bar transparent.
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "login"
    // Initialize SessionManager
    val context = LocalView.current.context
    val sessionManager = remember { SessionManager(context) }

    // State for captured photo and attendance detail parameters
    var capturedPhoto by remember { mutableStateOf<Bitmap?>(null) }
    var isLateAttendance by remember { mutableStateOf(false) }
    var selectedClassName by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("") }

    // State to hold the current user
    var user by remember { mutableStateOf<AuthApi.User?>(null) }

    // Check session on app start
    var startDestination by remember { mutableStateOf("login") }
    LaunchedEffect(Unit) {
        sessionManager.userFlow.collect { savedUser ->
            if (savedUser != null) {
                user = savedUser
                startDestination = "home"
            } else {
                startDestination = "login"
            }
        }
    }


    // --- Dynamic Status Bar Color ---
    val view = LocalView.current
    // This effect runs whenever the currentRoute changes.
    // It adjusts the status bar icon color based on the screen's background.
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // When on 'home' screen (dark header), icons are light (false).
            // On other screens (light background), icons are dark (true).
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = currentRoute != "home"
        }
    }
    // --- End of Status Bar Logic ---

    // Routes where bottom navigation should be shown
    val routesWithBottomNav = setOf("home", "attendance", "history", "schedule", "request_permission", "create_permission_request", "submission_complete", "pending_request")

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = currentRoute in routesWithBottomNav,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                AppBottomNavigation(
                    currentRoute = currentRoute,
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
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(300)) }
        ) {
            composable(
                "login",
                enterTransition = { fadeIn(animationSpec = tween(500)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                LoginScreen(
                    sessionManager = sessionManager,
                    onLoginSuccess = { loggedInUser ->
                        user = loggedInUser
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onSignUpClick = {
                        navController.navigate("sign_up")
                    }
                )
            }

            composable(
                "sign_up"
            ){
                SignUpScreen(
                    onNavigateToLogin ={
                        navController.navigate("login"){
                            popUpTo(0)
                        }
                    }
                )
            }

            composable("home") {
                HomeScreen(
                    user = user,
                    sessionManager = sessionManager,
                    onLogout = {
                        user = null // Clear user on logout
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
                        capturedPhoto = bitmap
                        isLateAttendance = false
                        navController.navigate("submit_attendance")
                    }
                )
            }

            composable("submit_attendance") {
                SubmitAttendanceScreen(
                    capturedPhoto = capturedPhoto,
                    isLateAttendance = isLateAttendance,
                    onBackClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onTakePhotoClick = {
                        navController.navigate("camera")
                    },
                    onSubmitSuccess = {
                        capturedPhoto = null
                        isLateAttendance = false
                        navController.navigate("submission_complete") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onNavigateHome = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable("history") {
                HistoryScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onCardClick = { className, status ->
                        selectedClassName = className
                        selectedStatus = status
                        navController.navigate("attendance_detail")
                    },
                    onNavigateToSubmissionComplete = { status, courseName ->
                        selectedClassName = courseName
                        selectedStatus = status
                        navController.navigate("submission_complete_detail")
                    },
                    onNavigateToSchedule = {
                        navController.navigate("schedule") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
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
                    },
                    onCardClick = { className, status ->
                        selectedClassName = className
                        selectedStatus = status
                        navController.navigate("attendance_detail")
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

            composable("submission_complete") {
                SubmissionCompleteScreen(
                    status = selectedStatus, // pass the selected status
                    courseName = selectedClassName, // pass the selected class name
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNavigateHome = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
