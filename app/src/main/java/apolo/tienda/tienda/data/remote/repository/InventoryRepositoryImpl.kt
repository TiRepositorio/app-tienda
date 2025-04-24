package apolo.tienda.tienda.data.remote.repository

import apolo.tienda.tienda.data.remote.api.InventoryApi
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
import apolo.tienda.tienda.domain.repository.InventoryRepository

class InventoryRepositoryImpl(
    private val api: InventoryApi
) : InventoryRepository {

    override suspend fun getEmpresas(): Result<List<EmpresaResponse>> {
        return try {
            val response = api.getEmpresas()
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Datos vacíos"))
            } else {
                Result.failure(Exception("Error al obtener empresas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSucursales(cod_empresa: String): Result<List<SucursalResponse>> {
        return try {
            val response = api.getSucursales(cod_empresa)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Datos vacíos"))
            } else {
                Result.failure(Exception("Error al obtener sucursales"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun createInventory(request: NewInventoryRequest): Result<NewInventoryResponse> {
        return try {
            val response = api.createInventory(request)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Error de datos"))
            } else {
                Result.failure(Exception("Error al crear inventario"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun loadInventory(request: LoadInventoryRequest): Result<LoadInventoryResponse> {
        return try {
            val response = api.loadInventory(request)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Error de datos"))
            } else {
                Result.failure(Exception("Error al cargar inventario"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun closeInventory(request: CloseInventoryRequest): Result<CloseInventoryResponse> {
        return try {
            val response = api.closeInventory(request)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Error de datos"))
            } else {
                Result.failure(Exception("Error al cerrar el inventario"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getListInventory(cod_empresa: String): Result<List<ListInventoryResponse>> {
        return try {
            val response = api.listInventory(cod_empresa)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Datos vacíos"))
            } else {
                Result.failure(Exception("Error al obtener listado de inventarios"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    override suspend fun getUnidadMedida(cod_empresa: String, cod_articulo: String): Result<List<UnidadMedidaResponse>> {
        return try {
            val response = api.getUnidadMedida(cod_empresa, cod_articulo)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Datos vacíos"))
            } else {
                Result.failure(Exception("Error al obtener unidades de medida"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProducto(cod_empresa: String, cod_articulo: String, cod_barra: String): Result<List<ProductoResponse>> {
        return try {
            val response = api.getProducto(cod_empresa, cod_articulo, cod_barra)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Datos vacíos"))
            } else {
                Result.failure(Exception("Error al obtener producto"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    override suspend fun getDetailInventory(cod_empresa: String, nro_registro: String): Result<List<DetailInventoryResponse>> {
        return try {
            val response = api.getDetailInventory(cod_empresa, nro_registro)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Datos vacíos"))
            } else {
                Result.failure(Exception("Error al obtener detalle de inventario"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getUserConfig(): Result<UserConfigResponse> {
        return try {
            val response = api.getUserConfig()
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Datos vacíos"))
            } else {
                Result.failure(Exception("Error al obtener config del usuario"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun deleteDetInventory(request: DeleteDetInventoryRequest): Result<DeleteDetInventoryResponse> {
        return try {
            val response = api.deleteDetInventory(request)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Error de datos"))
            } else {
                Result.failure(Exception("Error al eliminar detalle"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun updateDetInventory(request: UpdateDetInventoryRequest): Result<UpdateDetInventoryResponse> {
        return try {
            val response = api.updateDetInventory(request)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Error de datos"))
            } else {
                Result.failure(Exception("Error al actualizar detalle"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}
