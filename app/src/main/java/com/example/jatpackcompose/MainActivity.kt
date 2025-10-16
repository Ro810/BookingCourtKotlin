package com.example.jatpackcompose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jatpackcompose.ui.screens.ForgotPasswordScreen
import com.example.jatpackcompose.ui.screens.LoginScreen
import com.example.jatpackcompose.ui.screens.OwnerManagementScreen
import com.example.jatpackcompose.ui.screens.RegScreen
import com.example.jatpackcompose.ui.theme.JatpackComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JatpackComposeTheme { // Áp dụng theme cho toàn bộ ứng dụng
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background // Nền từ theme
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {
                        composable("login") {
                            LoginScreen(
                                navController = navController,
                                onLoginClicked = { username, phoneNumber, password, rememberMe ->
                                    // Xử lý logic đăng nhập ở đây (ví dụ: gọi ViewModel)
                                    Log.d("Login", "Username: $username, Phone: $phoneNumber, Password: $password, Remember: $rememberMe")
                                },
                                onForgotPasswordClicked = {
                                    navController.navigate("forgotpassword")
                                },
                                onSignUpClicked = {
                                    navController.navigate("reg")
                                }
                            )
                        }
                        composable("forgotpassword") {
                            ForgotPasswordScreen(
                                onResetPasswordClicked = { /* Xử lý reset mật khẩu */ },
                                onBackToLoginClicked = {
                                    navController.popBackStack("login", false)
                                }
                            )
                        }
                        composable("reg") {
                            RegScreen(
                                onRegisterClicked = { _, _, _, _, _, _ -> /* Xử lý đăng ký */ },
                                onBackToLoginClicked = {
                                    navController.popBackStack("login", false)
                                }
                            )
                        }
                        composable("home") {
                            OwnerManagementScreen()
                        }
                        composable("ownervenue") {
                            OwnerManagementScreen()
                        }
                    }
                }
            }
        }
    }
}
