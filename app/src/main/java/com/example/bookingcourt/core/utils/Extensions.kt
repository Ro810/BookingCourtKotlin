package com.example.bookingcourt.core.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.NumberFormat
import java.util.Locale

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

@Composable
fun <T> Flow<T>.collectAsEffect(
    key: Any? = null,
    block: suspend (T) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val blockState by rememberUpdatedState(block)

    LaunchedEffect(key ?: this, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            collect { blockState(it) }
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> Flow<T>.asResult(): Flow<Result<T>> = map { Result.success(it) }
    .onStart { emit(Result.success(null as T)) }
    .catch { emit(Result.failure(it)) }

fun Long.toFormattedPrice(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return format.format(this)
}

fun Instant.toLocalDateTime(): LocalDateTime {
    return this.toLocalDateTime(TimeZone.currentSystemDefault())
}

fun getCurrentTimestamp(): Long = Clock.System.now().toEpochMilliseconds()

inline fun <T> Result<T>.onSuccess(action: (value: T) -> Unit): Result<T> {
    if (isSuccess) {
        action(getOrThrow())
    }
    return this
}

inline fun <T> Result<T>.onFailure(action: (exception: Throwable) -> Unit): Result<T> {
    exceptionOrNull()?.let { action(it) }
    return this
}
