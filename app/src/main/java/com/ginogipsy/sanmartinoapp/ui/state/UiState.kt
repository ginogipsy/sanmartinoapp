package com.ginogipsy.sanmartinoapp.ui.state

/**
 * Stato tipico di una schermata che fetcha dati da rete: Loading iniziale, Success con
 * payload, Error con messaggio. Usato dai ViewModel come `StateFlow<UiState<T>>`.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
