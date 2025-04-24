package apolo.tienda.tienda.domain.model

import java.io.Serializable

data class Inventory(
    val nroRegistro: Int,
    val codEmpresa: String,
    val descEmpresa: String,
    val codSucursal: String,
    val descSucursal: String,
    val fecha: String,
    val descripcion: String
): Serializable
