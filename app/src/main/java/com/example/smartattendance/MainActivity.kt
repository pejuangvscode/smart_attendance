package com.example.smartattendance

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartattendance.ui.screens.*
import com.example.smartattendance.ui.components.AppBottomNavigation
import com.example.smartattendance.ui.theme.SmartAttendanceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set status bar color to match header
        window.statusBarColor = android.graphics.Color.parseColor("#FF2C2D32")
        WindowCompat.getInsetsController(window, window.decorView)?.let { controller ->
            controller.isAppearanceLightStatusBars = false
        }

        setContent {
            SmartAttendanceTheme {
                // Set status bar color using compose
                val view = LocalView.current
                LaunchedEffect(Unit) {
                    val activity = view.context as ComponentActivity
                    activity.window.statusBarColor = Color(0xFF2C2D32).toArgb()
                    WindowCompat.getInsetsController(activity.window, view)?.let { controller ->
                        controller.isAppearanceLightStatusBars = false
                    }
                }

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

    // State for captured photo and attendance detail parameters
    var capturedPhoto by remember { mutableStateOf<Bitmap?>(null) }
    var isLateAttendance by remember { mutableStateOf(false) }
    var selectedClassName by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("") }

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
            startDestination = "login",
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
                    },
                    onBackClick = {
                        navController.popBackStack()
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
                        // For now, we'll set isLateAttendance based on current time
                        // This can be enhanced later with proper time checking logic
                        isLateAttendance = false // You can add time checking logic here
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
                    },
                    onNavigateSchedule = {
                        navController.navigate("schedule") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateHistory = {
                        navController.navigate("history") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
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
                    },
                    onSubmitAttendance = {
                        navController.navigate("attendance")
                    },
                    onScheduleItemClick = { title, time ->
                        // Extract course code from title
                        val courseCode = when {
                            title.contains("KECERDASAN KOMPUTASIONAL") && title.contains("Laboratory") -> "INF 20052 - KKLR"
                            title.contains("KECERDASAN KOMPUTASIONAL") -> "INF 20052 - KKKR"
                            title.contains("PEMBELAJARAN MESIN LANJUT") -> "INF 20151 - PMLR"
                            title.contains("PGMB. APLIKASI PLATFORM MOBILE") && title.contains("Laboratory") -> "INF 20054 - PALR"
                            title.contains("PGMB. APLIKASI PLATFORM MOBILE") -> "INF 20054 - PAPR"
                            title.contains("INFORMATIKA DALAM KOM SELULER") -> "INF 20262 - IKSR"
                            title.contains("PERANCANGAN & PEMROGRAMAN WEB") && title.contains("Laboratory") -> "INF 20053 - PPLR"
                            title.contains("PERANCANGAN & PEMROGRAMAN WEB") -> "INF 20053 - PWR"
                            title.contains("KEAMANAN KOMPUTER & JARINGAN") && title.contains("Laboratory") -> "INF 20051 - KKLR"
                            title.contains("KEAMANAN KOMPUTER & JARINGAN") -> "INF 20051 - KKJR"
                            else -> "UNKNOWN"
                        }

                        // Get current day name based on current date (October 6, 2025 is Sunday, so Friday would be index 4)
                        val dayName = "Friday" // Today is Friday based on your schedule

                        navController.navigate("class_detail/$dayName/$courseCode")
                    }
                )
            }

            composable("request_permission") {
                RequestPermissionScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onCreateRequest = {
                        // Navigate to the form screen for creating request
                        navController.navigate("create_permission_form") {
                            popUpTo("request_permission") { inclusive = false }
                            launchSingleTop = true
                        }
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
                    onNavigateToRequestList = {
                        navController.navigate("request_permission") {
                            popUpTo("request_permission") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable("create_permission_form") {
                CreatePermissionFormScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSubmit = {
                        navController.navigate("create_permission_request") {
                            popUpTo("request_permission") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable("class_detail/{dayName}/{courseCode}") { backStackEntry ->
                val dayName = backStackEntry.arguments?.getString("dayName") ?: ""
                val courseCode = backStackEntry.arguments?.getString("courseCode") ?: ""
                ClassDetailScreen(
                    dayName = dayName,
                    courseCode = courseCode,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable("submission_complete") {
                SubmitionComplete(
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

            composable("pending_request") {
                CreatePermissionRequestScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNavigateToRequestList = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable("attendance_detail") {
                AttendanceDetailScreen(
                    className = selectedClassName,
                    status = selectedStatus,
                    onBackClick = {
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

            // New route for submission complete detail from history
            composable("submission_complete_detail") {
                SubmissionCompleteScreen(
                    status = selectedStatus,
                    courseName = selectedClassName,
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