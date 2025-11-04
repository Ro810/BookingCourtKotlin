package com.example.bookingcourt.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.bookingcourt.presentation.auth.screen.ForgotPasswordScreen
import com.example.bookingcourt.presentation.auth.screen.LoginScreen
import com.example.bookingcourt.presentation.auth.screen.RegisterScreen
import com.example.bookingcourt.presentation.auth.screen.ResetPasswordScreen
import com.example.bookingcourt.presentation.auth.screen.SplashScreen
import com.example.bookingcourt.presentation.booking.screen.BookingDetailScreen
import com.example.bookingcourt.presentation.booking.screen.BookingHistoryScreen
import com.example.bookingcourt.presentation.booking.screen.BookingScreen
import com.example.bookingcourt.presentation.court.screen.DetailScreen
import com.example.bookingcourt.presentation.court.screen.CourtListScreen
import com.example.bookingcourt.presentation.court.screen.CourtDetailScreen
import com.example.bookingcourt.presentation.home.screen.HomeScreen // HomeScreen của User (Customer)
import com.example.bookingcourt.presentation.home.screen.OwnerHomeScreen // OwnerHomeScreen của chủ sân
import com.example.bookingcourt.presentation.payment.screen.PaymentScreen
import com.example.bookingcourt.presentation.profile.screen.EditProfileScreen
import com.example.bookingcourt.presentation.profile.screen.ProfileScreen
import com.example.bookingcourt.presentation.profile.screen.ChangePasswordScreen
import com.example.bookingcourt.presentation.owner.screen.BecomeOwnerScreen
import com.example.bookingcourt.presentation.owner.screen.CreateVenueScreen
import com.example.bookingcourt.presentation.settings.screen.SettingsScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bookingcourt.presentation.home.viewmodel.HomeViewModel
import com.example.bookingcourt.presentation.profile.viewmodel.ProfileViewModel
import com.example.bookingcourt.presentation.owner.viewmodel.BecomeOwnerViewModel
import com.example.bookingcourt.domain.model.UserRole
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bookingcourt.presentation.notification.screen.NotificationsScreen

@Composable
fun NavigationGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // SplashScreen - màn hình khởi đầu
        composable(route = Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Route.MAIN) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Route.AUTH) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        // AUTH navigation
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
                        navController.navigate(Route.MAIN) {
                            popUpTo(Route.AUTH) { inclusive = true }
                        }
                    },
                )
            }

            composable(route = Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onNavigateToResetPassword = { email ->
                        navController.navigate(Screen.ResetPassword.createRoute(email))
                    },
                )
            }

            composable(
                route = Screen.ResetPassword.route,
                arguments = listOf(
                    navArgument("email") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                ResetPasswordScreen(
                    email = email,
                    onNavigateBack = { navController.navigateUp() },
                    onResetSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Route.AUTH) { inclusive = false }
                        }
                    },
                )
            }
        }

        // MAIN navigation
        navigation(
            route = Route.MAIN,
            startDestination = Screen.Home.route,
        ) {

            // 1. HomeScreen (USER - Code của ThuyTien)
            composable(route = Screen.Home.route) {
                HomeScreen(
                    onVenueClick = { venue -> // Đã đổi từ onCourtClick -> onVenueClick
                        navController.navigate(
                            Screen.CourtDetail.createRoute(venue.id.toString()),
                        )
                    },
                    onSearchClick = { // Tính năng mới của ThuyTien
                        // Không cần navigate vì đã có search ngay trên HomeScreen
                    },
                    onProfileClick = { // Tính năng mới của ThuyTien
                        navController.navigate(Screen.Profile.route)
                    },
                )
            }

            // 2. OwnerHomeScreen (OWNER - Màn hình quản lý sân của chủ sân)
            composable(route = Screen.OwnerHome.route) {
                OwnerHomeScreen(
                    onNavigateToCourtDetail = { courtId ->
                        navController.navigate(
                            Screen.OwnerCourtDetail.createRoute(courtId),
                        )
                    },
                    onNavigateToEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onNavigateToCreateVenue = {
                        navController.navigate(Screen.CreateVenue.route)
                    },
                    onNavigateToBecomeCustomer = {
                        // Chuyển về HomeScreen (chế độ khách đặt sân)
                        navController.navigate(Screen.Home.route) {
                            // Clear back stack up to OwnerHome and remove it
                            popUpTo(Screen.OwnerHome.route) {
                                inclusive = true
                            }
                            // Prevent multiple copies of Home screen
                            launchSingleTop = true
                        }
                    },
                    onLogout = {
                        navController.navigate(Route.AUTH) {
                            popUpTo(Route.MAIN) { inclusive = true }
                        }
                    },
                )
            }

            // 4. OwnerCourtDetail (OWNER - Màn hình chi tiết sân cho chủ sân)
            composable(
                route = Screen.OwnerCourtDetail.route,
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
                        // TODO: Navigate to booking management if needed
                    },
                    onNavigateToBookingDetail = { bookingId ->
                        navController.navigate(
                            Screen.BookingDetail.createRoute(bookingId),
                        )
                    },
                )
            }

            // 4.2. CreateVenue (OWNER - Màn hình tạo sân mới)
            composable(route = Screen.CreateVenue.route) {
                CreateVenueScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onVenueCreated = { venueId ->
                        // Navigate back to OwnerHome after successful creation
                        navController.navigate(Screen.OwnerHome.route) {
                            popUpTo(Screen.CreateVenue.route) { inclusive = true }
                        }
                    }
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

            // 5. CourtDetail (USER/OWNER - Code của ThuyTien)
            // Hiển thị chi tiết Venue và danh sách Courts bên trong
            composable(
                route = Screen.CourtDetail.route,
                arguments = listOf(
                    navArgument("courtId") { // Thực chất là venueId
                        type = NavType.StringType
                    },
                ),
            ) { backStackEntry ->
                val venueId = backStackEntry.arguments?.getString("courtId") ?: ""

                // Lấy venue từ HomeViewModel state để tránh gọi API lại
                val parentEntry = remember(backStackEntry) {
                    try {
                        navController.getBackStackEntry(Screen.Home.route)
                    } catch (_: Exception) {
                        backStackEntry
                    }
                }
                val homeViewModel: HomeViewModel = hiltViewModel(parentEntry)
                val state by homeViewModel.state.collectAsState()
                val venue = remember(venueId, state) {
                    state.featuredVenues.find { it.id.toString() == venueId }
                        ?: state.recommendedVenues.find { it.id.toString() == venueId }
                        ?: state.nearbyVenues.find { it.id.toString() == venueId }
                }
                venue?.let {
                    DetailScreen(
                        venue = it,
                        onBackClick = { navController.navigateUp() },
                        onBookClick = { selectedVenue ->
                            navController.navigate(
                                Screen.Booking.createRoute(selectedVenue.id.toString()),
                            )
                        }
                    )
                }
            }

            composable(
                route = Screen.Booking.route,
                arguments = listOf(
                    navArgument("courtId") { // Thực chất là venueId
                        type = NavType.StringType
                    },
                ),
            ) { backStackEntry ->
                val venueId = backStackEntry.arguments?.getString("courtId") ?: ""

                // Lấy venue từ HomeViewModel state
                val parentEntry = remember(backStackEntry) {
                    try {
                        navController.getBackStackEntry(Screen.Home.route)
                    } catch (_: Exception) {
                        backStackEntry
                    }
                }
                val homeViewModel: HomeViewModel = hiltViewModel(parentEntry)
                val homeState by homeViewModel.state.collectAsState()
                val resolvedVenue = remember(venueId, homeState) {
                    homeState.featuredVenues.find { it.id.toString() == venueId }
                        ?: homeState.recommendedVenues.find { it.id.toString() == venueId }
                        ?: homeState.nearbyVenues.find { it.id.toString() == venueId }
                }

                // Get current user from ProfileViewModel so BookingScreen can prefill name/phone
                val profileViewModel: ProfileViewModel = hiltViewModel()
                val profileState by profileViewModel.state.collectAsState()

                BookingScreen(
                    courtId = venueId,
                    court = resolvedVenue,
                    currentUser = profileState.currentUser,
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

            // Notifications
            composable(route = Screen.Notifications.route) {
                NotificationsScreen(
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // ...existing code...
