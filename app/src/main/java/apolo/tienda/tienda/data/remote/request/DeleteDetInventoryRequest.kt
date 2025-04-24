package apolo.tienda.tienda.data.remote.request

data class DeleteDetInventoryRequest(
    val cod_empresa: String,
    val nro_registro: Number,
    val nro_orden: Number
)