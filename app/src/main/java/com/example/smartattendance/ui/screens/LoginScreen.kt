package com.example.smartattendance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.R
import com.example.smartattendance.ui.theme.AppFontFamily
import com.example.smartattendance.ui.theme.SmartAttendanceTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit = { _, _ -> }
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val validUsername = "a"
    val validPassword = "a"

    fun validateLogin() {
        isLoading = true
        loginError = ""

        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)

            when {
                username.isEmpty() -> {
                    loginError = "Username tidak boleh kosong"
                }
                password.isEmpty() -> {
                    loginError = "Password tidak boleh kosong"
                }
                username != validUsername -> {
                    loginError = "Username salah"
                }
                password != validPassword -> {
                    loginError = "Password salah"
                }
                else -> {
                    loginError = ""
                    onLoginClick(username, password)
                }
            }
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(60.dp))
        Box(
            modifier = Modifier
                .size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(150.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Login",
            fontSize = 52.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (loginError.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = loginError,
                    fontSize = 14.sp,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Username",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                loginError = ""
            },
            placeholder = {
                Text(
                    text = "Username",
                    color = Color.Gray
                )            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border( // Add the border modifier here
                    BorderStroke(
                        width = 2.dp,
                        color = if (loginError.contains("Username")) Color.Red else Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp) // Use the same shape as the text field
                ),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                // Set default border colors to transparent to avoid drawing two borders
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            singleLine = true,
            enabled = !isLoading,
        )


        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Password",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                loginError = ""
            },
            placeholder = {
                Text(
                    text = "••••••••••••••••",
                    color = Color.Gray
                )            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border( // Add the border modifier here
                    BorderStroke(
                        width = 2.dp,
                        color = if (loginError.contains("Password")) Color.Red else Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp) // Use the same shape as the text field
                ),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                // Set default border colors to transparent to avoid drawing two borders
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            enabled = !isLoading,
            trailingIcon = {
                TextButton(
                    onClick = { passwordVisible = !passwordVisible },
                    enabled = !isLoading
                ) {
                    Text(
                        text = if (passwordVisible) "Hide" else "Show",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = { /* Handle forgot password */ },
                enabled = !isLoading
            ) {
                Text(
                    text = "Forgot Password?",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontFamily = AppFontFamily
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { validateLogin() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF424242)
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Log in",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = AppFontFamily
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SmartAttendanceTheme {
        LoginScreen()
    }
}
