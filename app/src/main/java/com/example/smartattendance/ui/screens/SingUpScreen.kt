package com.example.smartattendance.ui.screens

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartattendance.ui.theme.AppFontFamily
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.smartattendance.api.AuthApi
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat

@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit = {}
){
    var isLoading by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordIsSame by remember { mutableStateOf(true) }
    var fullName by remember { mutableStateOf("") }
    var nim by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = ContextCompat.getSystemService(context, ConnectivityManager::class.java)
                ?: return false

            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            // Check if the device has internet capability
            val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

            // Check if it's connected via WiFi or Cellular
            val hasWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            val hasCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            val hasEthernet = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)

            return hasInternet && (hasWifi || hasCellular || hasEthernet)
        } catch (e: Exception) {
            // Fallback: if there's an error, assume network is available
            // This prevents false negatives
            true
        }
    }

    fun handleRegister(){
        when {
            fullName.isEmpty() -> {
                loginError = "Full Name is required"
            }
            email.isEmpty() -> {
                loginError = "Email is required"
            }
            nim.isEmpty() -> {
                loginError = "NIM is required"
            }
            password.isEmpty() -> {
                loginError = "Password is required"
            }
            confirmPassword.isEmpty() -> {
                loginError = "Confirm Password is required"
            }
            else -> {
                passwordIsSame = (password == confirmPassword)
                if(!passwordIsSame){
                    loginError = "Password must be the same"
                } else {
                    isLoading = true
                    loginError = ""
                    coroutineScope.launch {
                        try {
                            val result = AuthApi.signUp(email, password, fullName, nim)
                            isLoading = false
                            result.onSuccess {
                                loginError = ""
                                showSuccessDialog = true
                            }.onFailure { exception ->
                                loginError = exception.message ?: "Pendaftaran gagal. Silakan coba lagi."
                                // Log error for debugging
                                println("SignUp UI Error: ${exception.message}")
                            }
                        } catch (e: Exception) {
                            isLoading = false
                            // Log error for debugging
                            println("SignUp UI Exception: ${e.javaClass.simpleName}: ${e.message}")
                            e.printStackTrace()

                            loginError = when {
                                e.message?.contains("UnknownHostException", ignoreCase = true) == true ||
                                e.message?.contains("ConnectException", ignoreCase = true) == true ||
                                e.message?.contains("SocketTimeoutException", ignoreCase = true) == true ||
                                e.message?.contains("network", ignoreCase = true) == true ||
                                e.message?.contains("internet", ignoreCase = true) == true ->
                                    "Tidak ada koneksi internet. Silakan periksa koneksi Anda dan coba lagi."
                                e.message?.isNotBlank() == true ->
                                    "Error: ${e.message}"
                                else ->
                                    "Terjadi kesalahan. Silakan coba lagi."
                            }
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,

    ) {
        Row(
            modifier = Modifier
                .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
        ) {
            Image(
                painter = painterResource(id = com.example.smartattendance.R.drawable.logo),
                contentDescription = "Logo"
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Sign Up",
                fontSize = 32.sp,
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = 0.8f),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = fullName,
            onValueChange = {
                fullName = it
                loginError = ""
            },
            placeholder = {
                Text(
                    text = "Full Name",
                    color = Color.Gray,
                    fontFamily = AppFontFamily
                )            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(color = Color.Gray.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            singleLine = true,
            enabled = !isLoading,
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = email,
            onValueChange = {
                email = it
                loginError = ""
            },
            placeholder = {
                Text(
                    text = "Email",
                    color = Color.Gray,
                    fontFamily = AppFontFamily
                )            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(color = Color.Gray.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            singleLine = true,
            enabled = !isLoading,
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = nim,
            onValueChange = {
                nim = it
                loginError = ""
            },
            placeholder = {
                Text(
                    text = "NIM",
                    color = Color.Gray,
                    fontFamily = AppFontFamily
                )            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(color = Color.Gray.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            singleLine = true,
            enabled = !isLoading,
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = password,
            onValueChange = {
                password = it
                loginError = ""
            },
            placeholder = {
                Text(
                    text = "Password",
                    color = Color.Gray,
                    fontFamily = AppFontFamily
                )            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    color = Color.Gray.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
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
                    if (!passwordVisible) {
                        Icon(
                            imageVector = Icons.Outlined.RemoveRedEye,
                            contentDescription = "Hide Password",
                            tint = Color.Gray
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.VisibilityOff,
                            contentDescription = "Show Password",
                            tint = Color.Gray
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                loginError = ""
            },
            placeholder = {
                Text(
                    text = "Confirm Password",
                    color = Color.Gray,
                    fontFamily = AppFontFamily
                )            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    color = Color.Gray.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
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
                    if (!passwordVisible) {
                        Icon(
                            imageVector = Icons.Outlined.RemoveRedEye,
                            contentDescription = "Hide Password",
                            tint = Color.Gray
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.VisibilityOff,
                            contentDescription = "Show Password",
                            tint = Color.Gray
                        )
                    }
                }
            }
        )

        if (loginError.isNotEmpty()) {
            Text(
                text = loginError,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier
            .size(24.dp))

        Button(
            onClick = { handleRegister() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF424242)
            ),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Sign Up",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFontFamily
                )
            }
        }

        TextButton(
            onClick = { onNavigateToLogin() }
        ) {
            Text(
                text = "Already have an account? Log in",
                color = Color.Gray,
                fontFamily = AppFontFamily
            )
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = "Pendaftaran Berhasil!",
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Akun Anda telah berhasil dibuat. Silakan login dengan akun yang baru dibuat.",
                    fontFamily = AppFontFamily
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateToLogin()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF424242)
                    )
                ) {
                    Text(
                        text = "OK",
                        color = Color.White,
                        fontFamily = AppFontFamily
                    )
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview(){
    SignUpScreen()
}
