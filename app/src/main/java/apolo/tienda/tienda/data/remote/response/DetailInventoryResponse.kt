package apolo.tienda.tienda.data.remote.response

import com.google.gson.annotations.SerializedName

data class  DetailInventoryResponse (

    @SerializedName("COD_EMPRESA")
    val cod_empresa: String,

    @SerializedName("DESC_EMPRESA")
    val desc_empresa: String,

    @SerializedName("COD_SUCURSAL")
    val cod_sucursal: String,

    @SerializedName("DESC_SUCURSAL")
    val desc_sucursal: String,

    @SerializedName("NRO_REGISTRO")
    val nro_registro: Number,

    @SerializedName("NRO_ORDEN")
    val nro_orden: Number,

    @SerializedName("COD_BARRA")
    val cod_barra: String,

    @SerializedName("COD_ARTICULO")
    val cod_articulo: String,

    @SerializedName("DESC_ARTICULO")
    val desc_articulo: String,

    @SerializedName("COD_UNIDAD_MEDIDA")
    val cod_unidad_medida: String,

    @SerializedName("DESC_UNIDAD_MEDIDA")
    val desc_unidad_medida: String,

    @SerializedName("CANTIDAD")
    val cantidad: Number

)