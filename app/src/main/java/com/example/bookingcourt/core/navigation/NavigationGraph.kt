package com.example.bookingcourt.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.bookingcourt.presentation.auth.screen.ForgotPasswordScreen
import com.example.bookingcourt.presentation.auth.screen.LoginScreen
import com.example.bookingcourt.presentation.auth.screen.RegisterScreen
import com.example.bookingcourt.presentation.auth.screen.SplashScreen
import com.example.bookingcourt.presentation.booking.screen.BookingDetailScreen
import com.example.bookingcourt.presentation.booking.screen.BookingHistoryScreen
import com.example.bookingcourt.presentation.booking.screen.BookingScreen
import com.example.bookingcourt.presentation.court.screen.CourtDetailScreen
import com.example.bookingcourt.presentation.court.screen.CourtListScreen
import com.example.bookingcourt.presentation.home.screen.HomeScreen
import com.example.bookingcourt.presentation.payment.screen.PaymentScreen
import com.example.bookingcourt.presentation.profile.screen.EditProfileScreen
import com.example.bookingcourt.presentation.profile.screen.ProfileScreen
import com.example.bookingcourt.presentation.settings.screen.SettingsScreen

@Composable
fun NavigationGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        navigation(
            route = Route.AUTH,
            startDestination = Screen.Login.route,
        ) {
            composable(route = Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPassword.route)
                    },
                    onLoginSuccess = {
                        navController.navigate(Route.MAIN) {
                            popUpTo(Route.AUTH) { inclusive = true }
                        }
                    },
                )
            }

            composable(route = Screen.Register.route) {
                RegisterScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Route.AUTH) { inclusive = true }
                        }
                    },
                )
            }

            composable(route = Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onNavigateBack = { navController.navigateUp() },
                )
            }
        }

        navigation(
            route = Route.MAIN,
            startDestination = Screen.Home.route,
        ) {
            composable(route = Screen.Home.route) {
                HomeScreen(
                    onNavigateToCourtDetail = { courtId ->
                        navController.navigate(
                            Screen.CourtDetail.createRoute(courtId),
                        )
                    },
                    onNavigateToEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Route.MAIN) { inclusive = true }
                        }
                    },
                )
            }

            composable(route = Screen.CourtList.route) { backStackEntry ->
                val sportType = backStackEntry.arguments?.getString("sportType")
                CourtListScreen(
                    sportType = sportType,
                    onNavigateBack = { navController.navigateUp() },
                    onNavigateToCourtDetail = { courtId ->
                        navController.navigate(
                            Screen.CourtDetail.createRoute(courtId),
                        )
                    },
                )
            }

            composable(
                route = Screen.CourtDetail.route,
                arguments = listOf(
                    navArgument("courtId") {
                        type = NavType.StringType
                    },
                ),
            ) { backStackEntry ->
                val courtId = backStackEntry.arguments?.getString("courtId") ?: ""
                CourtDetailScreen(
                    courtId = courtId,
                    onNavigateBack = { navController.navigateUp() },
                    onNavigateToBooking = {
                        navController.navigate(
                            Screen.Booking.createRoute(courtId),
                        )
                    },
                    onNavigateToBookingDetail = { bookingId ->
                        navController.navigate(
                            Screen.BookingDetail.createRoute(bookingId),
                        )
                    },
                )
            }

            composable(
                route = Screen.Booking.route,
                arguments = listOf(
                    navArgument("courtId") {
                        type = NavType.StringType
                    },
                ),
            ) { backStackEntry ->
                val courtId = backStackEntry.arguments?.getString("courtId") ?: ""
                BookingScreen(
                    courtId = courtId,
                    onNavigateBack = { navController.navigateUp() },
                    onNavigateToPayment = { bookingId ->
                        navController.navigate(
                            Screen.Payment.createRoute(bookingId),
                        )
                    },
                )
            }

            composable(
                route = Screen.Payment.route,
                arguments = listOf(
                    navArgument("bookingId") {
                        type = NavType.StringType
                    },
                ),
            ) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                PaymentScreen(
                    bookingId = bookingId,
                    onNavigateBack = { navController.navigateUp() },
                    onPaymentSuccess = {
                        navController.navigate(Screen.BookingHistory.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                )
            }

            composable(route = Screen.BookingHistory.route) {
                BookingHistoryScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onNavigateToBookingDetail = { bookingId ->
                        navController.navigate(
                            Screen.BookingDetail.createRoute(bookingId),
                        )
                    },
                )
            }

            composable(
                route = Screen.BookingDetail.route,
                arguments = listOf(
                    navArgument("bookingId") {
                        type = NavType.StringType
                    },
                ),
            ) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                BookingDetailScreen(
                    bookingId = bookingId,
                    onNavigateBack = { navController.navigateUp() },
                )
            }

            composable(route = Screen.Profile.route) {
                ProfileScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onNavigateToEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Route.MAIN) { inclusive = true }
                        }
                    },
                )
            }

            composable(route = Screen.EditProfile.route) {
                EditProfileScreen(
                    onNavigateBack = { navController.navigateUp() },
                )
            }

            composable(route = Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.navigateUp() },
                )
            }
        }
    }
}
