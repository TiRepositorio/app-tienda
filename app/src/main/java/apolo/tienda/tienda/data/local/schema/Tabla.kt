package apolo.tienda.tienda.data.local.schema

data class Tabla(
    val nombre: String,
    val persistente: Boolean = false,
    val columnas: List<Columna>
)
