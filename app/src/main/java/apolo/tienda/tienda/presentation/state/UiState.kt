package apolo.tienda.tienda.presentation.state

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T? = null) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
