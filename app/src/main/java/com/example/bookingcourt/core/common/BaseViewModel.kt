package com.example.bookingcourt.core.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<State, Event, Effect> : ViewModel() {

    private val initialState: State by lazy { createInitialState() }

    private val _uiState: MutableStateFlow<State> = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    private val _effect: MutableSharedFlow<Effect> = MutableSharedFlow()
    val effect: SharedFlow<Effect> = _effect.asSharedFlow()

    val currentState: State
        get() = _uiState.value

    abstract fun createInitialState(): State

    abstract fun handleEvent(event: Event)

    protected fun setState(reduce: State.() -> State) {
        _uiState.value = currentState.reduce()
    }

    protected fun setEffect(builder: () -> Effect) {
        viewModelScope.launch {
            _effect.emit(builder())
        }
    }

    protected fun launchCatching(
        onError: (Throwable) -> Unit = {},
        block: suspend CoroutineScope.() -> Unit,
    ) {
        val errorHandler = CoroutineExceptionHandler { _, throwable ->
            onError(throwable)
        }
        viewModelScope.launch(errorHandler, block = block)
    }
}
