package apolo.tienda.tienda.domain.repository

import apolo.tienda.tienda.data.remote.request.CloseInventoryRequest
import apolo.tienda.tienda.data.remote.request.DeleteDetInventoryRequest
import apolo.tienda.tienda.data.remote.request.EnviarTomaInventarioRequest
import apolo.tienda.tienda.data.remote.request.LoadInventoryRequest
import apolo.tienda.tienda.data.remote.request.NewInventoryRequest
import apolo.tienda.tienda.data.remote.request.UpdateDetInventoryRequest
import apolo.tienda.tienda.data.remote.response.CloseInventoryResponse
import apolo.tienda.tienda.data.remote.response.DeleteDetInventoryResponse
import apolo.tienda.tienda.data.remote.response.EmpresaResponse
import apolo.tienda.tienda.data.remote.response.DetailInventoryResponse
import apolo.tienda.tienda.data.remote.response.EnviarTomaInventarioResponse
import apolo.tienda.tienda.data.remote.response.ListInventoryResponse
import apolo.tienda.tienda.data.remote.response.LoadInventoryResponse
import apolo.tienda.tienda.data.remote.response.NewInventoryResponse
import apolo.tienda.tienda.data.remote.response.ProductoResponse
import apolo.tienda.tienda.data.remote.response.SucursalResponse
import apolo.tienda.tienda.data.remote.response.UnidadMedidaResponse
import apolo.tienda.tienda.data.remote.response.UpdateDetInventoryResponse
import apolo.tienda.tienda.data.remote.response.UserConfigResponse

interface InventoryRepository {
    suspend fun getEmpresas(): Result<List<EmpresaResponse>>
    suspend fun getSucursales(cod_empresa: String): Result<List<SucursalResponse>>
    suspend fun createInventory(request: NewInventoryRequest): Result<NewInventoryResponse>
    suspend fun loadInventory(request: LoadInventoryRequest): Result<LoadInventoryResponse>
    suspend fun getListInventory(cod_empresa: String): Result<List<ListInventoryResponse>>
    suspend fun getUnidadMedida(cod_empresa: String, cod_articulo: String): Result<List<UnidadMedidaResponse>>
    suspend fun getProducto(cod_empresa: String, cod_articulo: String, cod_barra: String): Result<List<ProductoResponse>>
    suspend fun getDetailInventory(cod_empresa: String, nro_registro: String): Result<List<DetailInventoryResponse>>
    suspend fun getUserConfig(): Result<UserConfigResponse>
    suspend fun closeInventory(request: CloseInventoryRequest): Result<CloseInventoryResponse>
    suspend fun deleteDetInventory(request: DeleteDetInventoryRequest): Result<DeleteDetInventoryResponse>
    suspend fun updateDetInventory(request: UpdateDetInventoryRequest): Result<UpdateDetInventoryResponse>


    //los jardinesv
    suspend fun enviarTomaInventario(request: EnviarTomaInventarioRequest, idEquipo: String, uuid: String): Result<EnviarTomaInventarioResponse>
}

