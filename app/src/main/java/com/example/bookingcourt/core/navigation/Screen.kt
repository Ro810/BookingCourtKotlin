package com.example.bookingcourt.core.navigation

sealed class Screen(val route: String) {
    // Auth Screens
    data object Splash : Screen("splash")
    data object SelectRole : Screen("select_role")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot_password")
    data object ResetPassword : Screen("reset_password/{email}") {
        fun createRoute(email: String) = "reset_password/$email"
    }

    // Main Screens
    data object Home : Screen("home")
    data object OwnerHome : Screen("owner_home")
    data object Search : Screen("search")
    data object Filter : Screen("filter")

    // Profile Screens
    data object Profile : Screen("profile")
    data object EditProfile : Screen("edit_profile")
    data object Settings : Screen("settings")
    data object BecomeOwner : Screen("become_owner")

    // Court Screens
    data object CourtList : Screen("court_list?sportType={sportType}") {
        fun createRoute(sportType: String? = null) = if (sportType != null) {
            "court_list?sportType=$sportType"
        } else {
            "court_list"
        }
    }

    data object CourtDetail : Screen("court_detail/{courtId}") {
        fun createRoute(courtId: String) = "court_detail/$courtId"
    }

    data object OwnerCourtDetail : Screen("owner_court_detail/{courtId}") {
        fun createRoute(courtId: String) = "owner_court_detail/$courtId"
    }

    data object AddCourt : Screen("add_court")
    data object CreateVenue : Screen("create_venue")
    data object EditCourt : Screen("edit_court/{courtId}") {
        fun createRoute(courtId: String) = "edit_court/$courtId"
    }

    // Booking Screens
    data object Booking : Screen("booking/{courtId}") {
        fun createRoute(courtId: String) = "booking/$courtId"
    }

    data object BookingHistory : Screen("booking_history")

    data object BookingDetail : Screen("booking_detail/{bookingId}") {
        fun createRoute(bookingId: String) = "booking_detail/$bookingId"
    }

    // Payment Screens
    data object Payment : Screen("payment/{bookingId}") {
        fun createRoute(bookingId: String) = "payment/$bookingId"
    }

    data object PaymentSuccess : Screen("payment_success/{bookingId}") {
        fun createRoute(bookingId: String) = "payment_success/$bookingId"
    }
}

object Route {
    const val AUTH = "auth"
    const val MAIN = "main"
}
