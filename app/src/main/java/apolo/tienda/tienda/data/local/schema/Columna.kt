package apolo.tienda.tienda.data.local.schema

data class Columna(
    val nombre: String,
    val tipo: String,
    val primaryKey: Boolean = false,
    val nullable: Boolean = true
)
