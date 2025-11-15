package com.example.smartattendance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.ui.theme.AppFontFamily

enum class HeaderType {
    HOME, // Home screen with greeting and logout
    BACK, // Standard header with back button and title
    BACK_WITH_ACTION // Header with back button, title and action button
}

@Composable
fun AppHeader(
    title: String = "",
    subtitle: String = "",
    headerType: HeaderType = HeaderType.BACK,
    onBackClick: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null,
    showIcon: Boolean = false,
    iconRes: ImageVector? = null
) {
    when (headerType) {
        HeaderType.HOME -> {
            HomeHeader(
                greeting = title,
                onLogout = onLogout ?: {}
            )
        }
        HeaderType.BACK -> {
            StandardHeader(
                title = title,
                onBackClick = onBackClick ?: {},
                showIcon = showIcon,
                iconRes = iconRes
            )
        }
        HeaderType.BACK_WITH_ACTION -> {
            HeaderWithAction(
                title = title,
                onBackClick = onBackClick ?: {},
                actionIcon = actionIcon ?: Icons.Default.FilterList,
                onActionClick = onActionClick ?: {},
                showIcon = showIcon,
                iconRes = iconRes
            )
        }
    }
}

@Composable
private fun HomeHeader(
    greeting: String,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2C2D32))
            .padding(25.dp, 50.dp, 25.dp, 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = greeting,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        IconButton(onClick = onLogout) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Logout",
                tint = Color(0xFFFFFFFF),
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun StandardHeader(
    title: String,
    onBackClick: () -> Unit,
    showIcon: Boolean,
    iconRes: ImageVector?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2C2D32))
            .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFFFFFFFF),
                modifier = Modifier.size(35.dp)
            )
        }

        if (showIcon && iconRes != null) {
            Icon(
                imageVector = iconRes,
                contentDescription = null,
                tint = Color(0xFFFFFFFF),
                modifier = Modifier
                    .size(35.dp)
                    .padding(start = 8.dp, end = 17.dp)
            )
        }

        Text(
            text = title,
            fontFamily = AppFontFamily,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )
    }
}

@Composable
private fun HeaderWithAction(
    title: String,
    onBackClick: () -> Unit,
    actionIcon: ImageVector,
    onActionClick: () -> Unit,
    showIcon: Boolean,
    iconRes: ImageVector?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2C2D32))
            .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFFFFFFFF),
                modifier = Modifier.size(35.dp)
            )
        }

        if (showIcon && iconRes != null) {
            Icon(
                imageVector = iconRes,
                contentDescription = null,
                tint = Color(0xFFFFFFFF),
                modifier = Modifier
                    .size(35.dp)
                    .padding(start = 8.dp, end = 17.dp)
            )
        }

        Text(
            text = title,
            color = Color.White,
            fontSize = 24.sp,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onActionClick) {
            Icon(
                imageVector = actionIcon,
                contentDescription = "Action",
                tint = Color(0xFFFFFFFF),
                modifier = Modifier.size(35.dp)
            )
        }
    }
}

@Preview
@Composable
fun AppHeaderPreview() {
    AppHeader()
}