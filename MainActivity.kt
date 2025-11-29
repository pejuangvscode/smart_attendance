package com.example.smartattendance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartattendance.api.AuthApi
import com.example.smartattendance.ui.screens.*
import com.example.smartattendance.ui.theme.SmartAttendanceTheme
import com.example.smartattendance.utils.SessionManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity
import androidx.compose.runtime.saveable.rememberSaveable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartAttendanceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // State to hold the current user
    var user by remember { mutableStateOf<AuthApi.User?>(null) }

    // State for current schedule info for submission
    var currentScheduleInfo by remember { mutableStateOf<CurrentScheduleInfo?>(null) }

    // State for selected status and class name for submission complete screen
    var selectedStatus by remember { mutableStateOf("") }
    var selectedClassName by remember { mutableStateOf("") }

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

    // Get current route for status bar color logic
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: startDestination

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
                BottomNavigationBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("login") {
                LoginScreen(
                    onNavigateToSignUp = {
                        navController.navigate("sign_up")
                    },
                    onLoginSuccess = { loggedInUser ->
                        user = loggedInUser
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    sessionManager = sessionManager
                )
            }

            composable("sign_up") {
                SignUpScreen(
                    onNavigateToLogin = {
                        navController.navigate("login") {
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
                    onSubmitAttendance = { scheduleInfo ->
                        currentScheduleInfo = scheduleInfo
                        navController.navigate("submission")
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
                    onPhotoTaken = { photoUri ->
                        // Handle photo taken, perhaps navigate to submission
                        navController.navigate("submission_complete") {
                            popUpTo("camera")
                        }
                    }
                )
            }

            composable("history") {
                HistoryScreen(
                    user = user,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable("schedule") {
                ScheduleScreen(
                    user = user,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSubmitAttendance = { scheduleItem: ScheduleItemUI ->
                        val scheduleInfo = CurrentScheduleInfo(
                            courseName = scheduleItem.title.split(" - ").getOrNull(0) ?: scheduleItem.title,
                            time = scheduleItem.time,
                            isActive = true,
                            scheduleId = scheduleItem.scheduleId,
                            courseId = scheduleItem.courseId
                        )
                        currentScheduleInfo = scheduleInfo
                        navController.navigate("submission")
                    }
                )
            }

            composable("request_permission") {
                RequestPermissionScreen(
                    user = user,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNavigateToCreateRequest = {
                        navController.navigate("create_permission_request")
                    },
                    onNavigateToPendingRequest = {
                        navController.navigate("pending_request")
                    }
                )
            }

            composable("create_permission_request") {
                CreatePermissionRequestScreen(
                    user = user,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onRequestSubmitted = {
                        navController.navigate("pending_request") {
                            popUpTo("request_permission")
                        }
                    }
                )
            }

            composable("pending_request") {
                PendingRequestScreen(
                    user = user,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable("submission") {
                currentScheduleInfo?.let { scheduleInfo ->
                    SubmissionScreen(
                        user = user,
                        sessionManager = sessionManager,
                        scheduleInfo = scheduleInfo,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onSubmissionComplete = {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
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

// Data class for current schedule info
data class CurrentScheduleInfo(
    val courseName: String,
    val time: String,
    val isActive: Boolean,
    val scheduleId: Int? = null,
    val courseId: Int? = null
)

// Data class for schedule item UI
data class ScheduleItemUI(
    val scheduleId: Int = 0,
    val courseId: Int = 0,
    val title: String,
    val time: String,
    val room: String? = null
)
