package apolo.tienda.tienda.data.remote.request

import apolo.tienda.tienda.data.local.response.ListaDetalleTomaInventarioResponse

data class EnviarTomaInventarioRequest(
    val idInventario: Long,
    val productos: List<ListaDetalleTomaInventarioResponse>
)