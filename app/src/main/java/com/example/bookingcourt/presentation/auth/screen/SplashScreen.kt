package com.example.bookingcourt.presentation.auth.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bookingcourt.core.utils.collectAsEffect
import com.example.bookingcourt.presentation.auth.viewmodel.SplashViewModel
import com.example.bookingcourt.presentation.theme.BookingCourtTheme
import com.example.bookingcourt.presentation.theme.Dimensions
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    var startAnimation by remember { mutableFloatStateOf(0f) }

    val alphaAnim by animateFloatAsState(
        targetValue = startAnimation,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha",
    )

    val scaleAnim by animateFloatAsState(
        targetValue = startAnimation,
        animationSpec = tween(durationMillis = 1000),
        label = "scale",
    )

    LaunchedEffect(key1 = true) {
        startAnimation = 1f
        delay(1500)
        viewModel.checkAuthStatus()
    }

    viewModel.navigationEvent.collectAsEffect { event ->
        when (event) {
            SplashViewModel.NavigationEvent.NavigateToHome -> onNavigateToHome()
            SplashViewModel.NavigationEvent.NavigateToLogin -> onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.SportsTennis,
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .scale(scaleAnim)
                    .alpha(alphaAnim),
                tint = MaterialTheme.colorScheme.onPrimary,
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

            Text(
                text = "Booking Court",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.alpha(alphaAnim),
            )

            Text(
                text = "Đặt sân thể thao dễ dàng",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                modifier = Modifier.alpha(alphaAnim),
            )
        }
    }
}
