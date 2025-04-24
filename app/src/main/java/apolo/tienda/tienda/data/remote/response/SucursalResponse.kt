package apolo.tienda.tienda.data.remote.response

import com.google.gson.annotations.SerializedName

data class SucursalResponse(
    @SerializedName("COD_SUCURSAL")
    val cod_sucursal: String,

    @SerializedName("DESCRIPCION")
    val desc_sucursal: String
)