package apolo.tienda.tienda.data.remote.api

import apolo.tienda.tienda.data.remote.request.CloseInventoryRequest
import apolo.tienda.tienda.data.remote.request.DeleteDetInventoryRequest
import apolo.tienda.tienda.data.remote.request.LoadInventoryRequest
import apolo.tienda.tienda.data.remote.request.NewInventoryRequest
import apolo.tienda.tienda.data.remote.request.UpdateDetInventoryRequest
import apolo.tienda.tienda.data.remote.response.CloseInventoryResponse
import apolo.tienda.tienda.data.remote.response.DeleteDetInventoryResponse
import apolo.tienda.tienda.data.remote.response.EmpresaResponse
import apolo.tienda.tienda.data.remote.response.DetailInventoryResponse
import apolo.tienda.tienda.data.remote.response.ListInventoryResponse
import apolo.tienda.tienda.data.remote.response.LoadInventoryResponse
import apolo.tienda.tienda.data.remote.response.NewInventoryResponse
import apolo.tienda.tienda.data.remote.response.ProductoResponse
import apolo.tienda.tienda.data.remote.response.SucursalResponse
import apolo.tienda.tienda.data.remote.response.UnidadMedidaResponse
import apolo.tienda.tienda.data.remote.response.UpdateDetInventoryResponse
import apolo.tienda.tienda.data.remote.response.UserConfigResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface InventoryApi {
    @GET("empresas")
    suspend fun getEmpresas(): Response<List<EmpresaResponse>>

    @GET("sucursales")
    suspend fun getSucursales(@Query("COD_EMPRESA") cod_empresa: String): Response<List<SucursalResponse>>

    @POST("inventory/create-inventory")
    suspend fun createInventory(@Body request: NewInventoryRequest): Response<NewInventoryResponse>

    @POST("inventory/load-inventory")
    suspend fun loadInventory(@Body request: LoadInventoryRequest): Response<LoadInventoryResponse>

    @GET("inventory/list-inventory")
    suspend fun listInventory(@Query("COD_EMPRESA") cod_empresa: String): Response<List<ListInventoryResponse>>

    @GET("unidad-medida")
    suspend fun getUnidadMedida(@Query("COD_EMPRESA") cod_empresa: String, @Query("COD_ARTICULO") cod_articulo: String): Response<List<UnidadMedidaResponse>>

    @GET("productos")
    suspend fun getProducto(@Query("COD_EMPRESA") cod_empresa: String, @Query("COD_ARTICULO") cod_articulo: String, @Query("COD_BARRA") cod_barra: String): Response<List<ProductoResponse>>

    @GET("inventory/detail-inventory")
    suspend fun getDetailInventory(@Query("COD_EMPRESA") cod_empresa: String, @Query("NRO_REGISTRO") nro_registro: String): Response<List<DetailInventoryResponse>>

    @GET("user/get-config")
    suspend fun getUserConfig(): Response<UserConfigResponse>

    @POST("inventory/close-inventory")
    suspend fun closeInventory(@Body request: CloseInventoryRequest): Response<CloseInventoryResponse>

    @POST("inventory/delete-det-inventory")
    suspend fun deleteDetInventory(@Body request: DeleteDetInventoryRequest): Response<DeleteDetInventoryResponse>

    @POST("inventory/update-det-inventory")
    suspend fun updateDetInventory(@Body request: UpdateDetInventoryRequest): Response<UpdateDetInventoryResponse>

}