package com.example.smartattendance.ui.utils

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun SetStatusBarColor(color: Color = Color(0xFF2C2D32)) {
    val view = LocalView.current
    LaunchedEffect(color) {
        val activity = view.context as? ComponentActivity
        activity?.let {
            it.window.statusBarColor = color.toArgb()
            WindowCompat.getInsetsController(it.window, view)?.let { controller ->
                // Set light status bar icons if the status bar color is light, dark icons if dark
                controller.isAppearanceLightStatusBars = isColorLight(color)
            }
        }
    }
}

private fun isColorLight(color: Color): Boolean {
    // Calculate luminance to determine if we should use light or dark icons
    val red = color.red
    val green = color.green
    val blue = color.blue

    // Calculate relative luminance
    val luminance = 0.2126 * red + 0.7152 * green + 0.0722 * blue
    return luminance > 0.5
}
