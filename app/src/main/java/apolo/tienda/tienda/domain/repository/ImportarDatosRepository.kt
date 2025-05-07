package apolo.tienda.tienda.domain.repository

interface ImportarDatosRepository {
    suspend fun descargarYGuardarDatos(): Result<Unit>
}