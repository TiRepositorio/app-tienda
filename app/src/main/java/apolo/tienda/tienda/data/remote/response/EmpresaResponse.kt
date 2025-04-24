package apolo.tienda.tienda.data.remote.response

import com.google.gson.annotations.SerializedName

data class EmpresaResponse(
    @SerializedName("COD_EMPRESA")
    val cod_empresa: String,

    @SerializedName("DESCRIPCION")
    val desc_empresa: String
)