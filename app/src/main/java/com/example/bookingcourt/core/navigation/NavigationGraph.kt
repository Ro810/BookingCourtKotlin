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
import com.example.bookingcourt.presentation.filter.SearchScreen // Của User (ThuyTien)
import com.example.bookingcourt.presentation.filter.FilterScreen // Của User (ThuyTien)
import com.example.bookingcourt.presentation.payment.screen.PaymentScreen
import com.example.bookingcourt.presentation.profile.screen.EditProfileScreen
import com.example.bookingcourt.presentation.profile.screen.ProfileScreen
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
                    onCourtClick = { court -> // Thay thế onNavigateToCourtDetail
                        navController.navigate(
                            Screen.CourtDetail.createRoute(court.id),
                        )
                    },
                    onSearchClick = { // Tính năng mới của ThuyTien
                        navController.navigate(Screen.Search.route)
                    },
                    onFilterClick = { // Tính năng mới của ThuyTien
                        navController.navigate(Screen.Filter.route)
                    },
                    onProfileClick = { // Tính năng mới của ThuyTien
                        navController.navigate(Screen.Profile.route)
                    },
                )
            }

            // 2. SearchScreen (USER - Code của ThuyTien)
            composable(route = Screen.Search.route) {
                SearchScreen(
                    onBackClick = {
                        navController.navigateUp()
                    },
                    onCourtClick = { court ->
                        navController.navigate(
                            Screen.CourtDetail.createRoute(court.id),
                        )
                    },
                )
            }

            // 3. FilterScreen (USER - Code của ThuyTien)
            composable(route = Screen.Filter.route) {
                FilterScreen(
                    onBackClick = {
                        navController.navigateUp()
                    },
                    onApplyFilter = {
                        navController.navigateUp()
                    },
                )
            }

            // 4. OwnerHomeScreen (OWNER - Màn hình quản lý sân của chủ sân)
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

            // 4.1. OwnerCourtDetail (OWNER - Màn hình chi tiết sân cho chủ sân)
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
            composable(
                route = Screen.CourtDetail.route,
                arguments = listOf(
                    navArgument("courtId") {
                        type = NavType.StringType
                    },
                ),
            ) { backStackEntry ->
                val courtId = backStackEntry.arguments?.getString("courtId") ?: ""

                // Logic sử dụng ViewModel và DetailScreen (Code của ThuyTien)
                val parentEntry = remember(backStackEntry) {
                    try {
                        navController.getBackStackEntry(Screen.Search.route)
                    } catch (_: Exception) {
                        try {
                            navController.getBackStackEntry(Screen.Home.route)
                        } catch (_: Exception) {
                            backStackEntry
                        }
                    }
                }
                val homeViewModel: HomeViewModel = hiltViewModel(parentEntry)
                val state by homeViewModel.state.collectAsState()
                val court = remember(courtId, state) {
                    state.featuredCourts.find { it.id == courtId }
                        ?: state.recommendedCourts.find { it.id == courtId }
                        ?: state.nearbyCourts.find { it.id == courtId }
                }
                court?.let {
                    DetailScreen(
                        court = it,
                        onBackClick = { navController.navigateUp() },
                        onBookClick = { selectedCourt ->
                            navController.navigate(
                                Screen.Booking.createRoute(selectedCourt.id),
                            )
                        }
                    )
                }
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

            // ...
            composable(route = Screen.Profile.route) {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                val profileState by profileViewModel.state.collectAsState()

                var showMessage by remember { mutableStateOf<String?>(null) }

                // Lắng nghe events từ ViewModel
                LaunchedEffect(Unit) {
                    profileViewModel.event.collect { event ->
                        when (event) {
                            is com.example.bookingcourt.presentation.profile.viewmodel.ProfileEvent.NavigateToLogin -> {
                                // Navigate to login and clear all back stack
                                navController.navigate(Route.AUTH) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                            is com.example.bookingcourt.presentation.profile.viewmodel.ProfileEvent.NavigateToHomeScreen -> {
                                // Chuyển sang màn hình HomeScreen (khách đặt sân)
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Profile.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            is com.example.bookingcourt.presentation.profile.viewmodel.ProfileEvent.NavigateToOwnerHomeScreen -> {
                                // Chuyển sang màn hình OwnerHomeScreen (quản lý sân)
                                navController.navigate(Screen.OwnerHome.route) {
                                    popUpTo(Screen.Profile.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            is com.example.bookingcourt.presentation.profile.viewmodel.ProfileEvent.ShowMessage -> {
                                showMessage = event.message
                            }
                        }
                    }
                }

                ProfileScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onNavigateToEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onNavigateToChangePassword = {
                        // TODO: Navigate to change password screen when implemented
                    },
                    onNavigateToBecomeOwner = {
                        // Kiểm tra xem user đã có bank info chưa
                        val user = profileState.currentUser
                        val hasBankInfo = user?.bankName != null &&
                                         user?.bankAccountNumber != null &&
                                         user?.bankAccountName != null

                        if (hasBankInfo) {
                            // Đã có bank info -> chỉ cần switch mode
                            profileViewModel.switchToOwnerMode()
                        } else {
                            // Chưa có bank info -> navigate đến form đăng ký
                            navController.navigate(Screen.BecomeOwner.route)
                        }
                    },
                    onNavigateToBecomeCustomer = {
                        // Chuyển từ OWNER về USER mode
                        profileViewModel.switchToUserMode()
                    },
                    onLogout = {
                        navController.navigate(Route.AUTH) {
                            popUpTo(Route.MAIN) { inclusive = true }
                        }
                    },
                    viewModel = profileViewModel,
                )

                // Hiển thị message nếu có
                showMessage?.let { message ->
                    AlertDialog(
                        onDismissRequest = { showMessage = null },
                        title = { Text("Thông báo") },
                        text = { Text(message) },
                        confirmButton = {
                            TextButton(onClick = { showMessage = null }) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
            // Become Owner Screen - Form to fill bank information
            composable(route = Screen.BecomeOwner.route) {
                // Get BecomeOwnerViewModel at composable level
                val becomeOwnerViewModel: BecomeOwnerViewModel = hiltViewModel()
                val state by becomeOwnerViewModel.state.collectAsState()

                // Check if we're still determining bank info status
                when (state.hasBankInfo) {
                    null -> {
                        // Still checking, show loading
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    true -> {
                        // User already has bank info, directly request owner role
                        LaunchedEffect(Unit) {
                            becomeOwnerViewModel.requestOwnerRoleDirectly()
                        }

                        // Show loading while requesting
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator()
                                Text("Đang yêu cầu nâng cấp lên chủ sân...")
                            }
                        }
                    }
                    false -> {
                        // User doesn't have bank info, show the form
                        BecomeOwnerScreen(
                            onNavigateBack = { navController.navigateUp() },
                            onNavigateToLogin = {
                                // Navigate to login screen and clear all back stack
                                navController.navigate(Route.AUTH) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
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
