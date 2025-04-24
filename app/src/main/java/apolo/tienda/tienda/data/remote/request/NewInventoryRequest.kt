package apolo.tienda.tienda.data.remote.request

data class NewInventoryRequest(
    val cod_empresa: String,
    val cod_sucursal: String,
    val fecha: String,  // formato yyyy-MM-dd
    val descripcion: String
)