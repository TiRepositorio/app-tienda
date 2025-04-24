package apolo.tienda.tienda.data.remote.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class  ListInventoryResponse (

    @SerializedName("COD_EMPRESA")
    val cod_empresa: String,

    @SerializedName("DESC_EMPRESA")
    val desc_empresa: String,

    @SerializedName("COD_SUCURSAL")
    val cod_sucursal: String,

    @SerializedName("DESC_SUCURSAL")
    val desc_sucursal: String,

    @SerializedName("NRO_REGISTRO")
    val nro_registro: String,

    @SerializedName("FECHA")
    val fecha: String,

    @SerializedName("COD_USUARIO")
    val cod_usuario: String,

    @SerializedName("ESTADO")
    val estado: String,

    @SerializedName("DESCRIPCION")
    val descripcion: String

): Serializable