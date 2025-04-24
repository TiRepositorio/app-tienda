package apolo.tienda.tienda.data.remote.response

import com.google.gson.annotations.SerializedName

data class UserConfigResponse (

    @SerializedName("cod_empresa")
    val cod_empresa: String,

    @SerializedName("desc_empresa")
    val desc_empresa: String,

    @SerializedName("cod_sucursal")
    val cod_sucursal: String,

    @SerializedName("desc_sucursal")
    val desc_sucursal: String,

    @SerializedName("permisos")
    val permisos: List<String>

)
