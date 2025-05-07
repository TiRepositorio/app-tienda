package apolo.tienda.tienda.domain.model

import java.io.Serializable

data class InventarioLocal(
    val id: Int,
    val nroAjuste: Int,
    val usuarioAlta: String,
    val fechaInventario: String,
    val comentario: String,
    val estado: String
): Serializable

