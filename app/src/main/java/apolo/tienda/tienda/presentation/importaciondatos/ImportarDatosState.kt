package apolo.tienda.tienda.presentation.importaciondatos


sealed class ImportarDatosState {
    object Idle : ImportarDatosState()
    object Loading : ImportarDatosState()
    data class Progress(val current: Int, val total: Int, val tabla: String) : ImportarDatosState()
    object Success : ImportarDatosState()
    data class Error(val message: String) : ImportarDatosState()
}
