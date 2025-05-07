package apolo.tienda.tienda.data.local.request

data class NuevoInventarioCabeceraLocalRequest(
    val nroAjuste: Int,
    val usuarioAlta: String,
    val fechaInventario: String, // formato ISO o yyyy-MM-dd HH:mm:ss
    val comentario: String,
    val estado: String = "P"
)