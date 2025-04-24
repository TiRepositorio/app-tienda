package apolo.tienda.tienda.data.remote.request

data class UpdateDetInventoryRequest(
    val cod_empresa: String,
    val nro_registro: Number,
    val nro_orden: Number,
    val cantidad: Number,
    val cod_unidad_rel: String
)