package apolo.tienda.tienda.data.remote.request

data class LoadInventoryRequest(
    val cod_empresa: String,
    val cod_sucursal: String,
    val nro_registro: String,
    val cod_barra: String,
    val cod_articulo: String,
    val cod_unidad_rel: String,
    val cantidad: Double
)