package apolo.tienda.tienda.data.remote.response


data class NewInventoryResponse (

    val success: Boolean,  // Depende de lo que te devuelve tu API
    val nro_registro: Int, // Depende de lo que te devuelve tu API
    val cod_empresa: String, // Depende de lo que te devuelve tu API
    val cod_sucursal: String, // Depende de lo que te devuelve tu API
    val error: String, // Depende de lo que te devuelve tu API

)