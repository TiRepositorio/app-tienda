package apolo.tienda.tienda.data.local.response

data class ListaDetalleTomaInventarioResponse(
    val id: Int,
    val idInventarioCabecera: Int,
    val nroAjuste: Int,
    val codProducto: String,
    val descProducto: String,
    val cantidad: Double,
    val codBarra: String,
    val stockActual: Double,
    val codDeposito: String,
    val sector: String,
    val movil: String,
    val enviado: String,
)

