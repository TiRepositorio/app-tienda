package apolo.tienda.tienda.data.local.response


data class ListaTomaInventarioResponse(
    val nroAjuste: Int,
    val fecha: String,
    val comentario: String,
    val estado: String,
    val usuario: String,
    val id: Int,
    val totalRegistros: Int,
    val totalRegistrosEnviados: Int
)


