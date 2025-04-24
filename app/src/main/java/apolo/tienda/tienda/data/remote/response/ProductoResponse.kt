package apolo.tienda.tienda.data.remote.response

import com.google.gson.annotations.SerializedName

data class ProductoResponse(
    @SerializedName("COD_EMPRESA")
    val cod_empresa: String,

    @SerializedName("COD_ARTICULO")
    val cod_articulo: String,

    @SerializedName("DESCRIPCION")
    val desc_articulo: String,

    @SerializedName("COD_BARRA")
    val cod_barra: String,

    @SerializedName("COD_UNIDAD_REL")
    val cod_unidad_rel: String,

    @SerializedName("REFERENCIA")
    val referencia: String
)
