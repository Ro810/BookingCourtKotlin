package com.example.jatpackcompose.ui.theme

import android.app.Activity
// import android.os.Build // Không cần thiết nữa nếu không có dynamic color
import androidx.compose.foundation.isSystemInDarkTheme // Vẫn có thể giữ lại nếu bạn muốn tham chiếu ở đâu đó, nhưng không dùng trong hàm Theme này
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
// import androidx.compose.material3.darkColorScheme // Xóa bỏ
// import androidx.compose.material3.dynamicDarkColorScheme // Xóa bỏ
// import androidx.compose.material3.dynamicLightColorScheme // Xóa bỏ
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
// import androidx.compose.ui.platform.LocalContext // Không cần thiết nữa nếu không có dynamic color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = MidBlue,
    onPrimary = Color.White,
    secondary = DarkBlue,
    onSecondary = Color.White,
    tertiary = LightBlue,
    background = LightBlue,
    onBackground = Color.White,
    surface = Color(0xFFF5F5F5),
    onSurface = Color.Black,
    outline = DarkBlue,
)

@Composable
fun JatpackComposeTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
