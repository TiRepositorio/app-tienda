package apolo.tienda.tienda.data.local.api

import android.content.Context
import apolo.tienda.tienda.data.local.request.NuevoInventarioCabeceraLocalRequest
import apolo.tienda.tienda.data.local.request.NuevoInventarioDetalleLocalRequest
import apolo.tienda.tienda.data.local.response.ListaDetalleTomaInventarioResponse
import apolo.tienda.tienda.data.local.response.ListaInventarioResponse
import apolo.tienda.tienda.data.local.response.ListaProductoResponse
import apolo.tienda.tienda.data.local.response.ListaTomaInventarioResponse
import apolo.tienda.tienda.data.local.response.NuevoInventarioCabeceraLocalResponse
import apolo.tienda.tienda.data.local.response.NuevoInventarioDetalleLocalResponse

interface InventarioLocalRepository {
    fun getListaInventario(context: Context): Result<List<ListaInventarioResponse>>
    fun crearCabecera(context: Context, request: NuevoInventarioCabeceraLocalRequest): Result<NuevoInventarioCabeceraLocalResponse>
    fun getListaTomaInventario(context: Context): Result<List<ListaTomaInventarioResponse>>
    fun getTomaInventario(context: Context, id: Long): Result<ListaTomaInventarioResponse>
    fun getProductos(context: Context, filtro: String, nroAjuste: String): Result<List<ListaProductoResponse>>
    fun getProducto(context: Context, id: Int): Result<ListaProductoResponse>
    fun guardarProducto(context: Context, request: NuevoInventarioDetalleLocalRequest): Result<NuevoInventarioDetalleLocalResponse>
    fun getDetalleTomaInventario(context: Context, idCabecera: Int): Result<List<ListaDetalleTomaInventarioResponse>>
    fun finalizarTomaInventario(context: Context, idCabecera: Int): Result<Unit>
    fun getDetallePendienteEnvio(context: Context, idCabecera: Int): Result<List<ListaDetalleTomaInventarioResponse>>
    fun marcarTomaInventarioEnviado(context: Context, idCabecera: Int): Result<Unit>
}