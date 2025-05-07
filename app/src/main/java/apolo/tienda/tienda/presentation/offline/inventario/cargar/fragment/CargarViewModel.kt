package apolo.tienda.tienda.presentation.offline.inventario.cargar.fragment

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apolo.tienda.tienda.data.local.api.InventarioLocalApiImpl
import apolo.tienda.tienda.data.local.request.NuevoInventarioDetalleLocalRequest
import apolo.tienda.tienda.data.local.response.ListaProductoResponse
import apolo.tienda.tienda.data.remote.repository.InventoryRepositoryImpl
import apolo.tienda.tienda.data.remote.request.LoadInventoryRequest
import apolo.tienda.tienda.data.remote.response.ProductoResponse
import apolo.tienda.tienda.data.remote.response.UnidadMedidaResponse
import apolo.tienda.tienda.presentation.state.UiState
import kotlinx.coroutines.launch

class CargarViewModel : ViewModel() {

    private lateinit var repository: InventarioLocalApiImpl

    private val _productosState = MutableLiveData<List<ListaProductoResponse>>()
    val productosState: LiveData<List<ListaProductoResponse>> = _productosState

    private val _errorState = MutableLiveData<String>()
    val errorState: LiveData<String> = _errorState

    private val _saveItemState = MutableLiveData<UiState<Unit>>()
    val saveItemState: LiveData<UiState<Unit>> = _saveItemState

    fun setRepository(repository: InventarioLocalApiImpl) {
        this.repository = repository
    }



    fun getProductos(context: Context, filtro: String, nroAjuste: String ) {
        viewModelScope.launch {
            repository.getProductos(context, filtro, nroAjuste ).fold(
                onSuccess = {
                    _productosState.value = it
                },
                onFailure = {
                    _errorState.value = it.message ?: "Error al buscar producto"
                }
            )
        }
    }

    fun limpiarProductos() {
        _productosState.value = emptyList()
    }

    fun guardarProducto(context: Context, request: NuevoInventarioDetalleLocalRequest) {
        _saveItemState.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.guardarProducto(context, request)
            result.fold(
                onSuccess = { _saveItemState.value = UiState.Success(Unit) },
                onFailure = { _saveItemState.value = UiState.Error(it.message ?: "Error al guardar") }
            )
        }

    }


}
