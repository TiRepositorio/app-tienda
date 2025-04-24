package apolo.tienda.tienda.presentation.inventory.load

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apolo.tienda.tienda.data.remote.repository.InventoryRepositoryImpl
import apolo.tienda.tienda.data.remote.request.LoadInventoryRequest
import apolo.tienda.tienda.data.remote.response.ProductoResponse
import apolo.tienda.tienda.data.remote.response.UnidadMedidaResponse
import apolo.tienda.tienda.presentation.state.UiState
import kotlinx.coroutines.launch

class InventoryLoadViewModel : ViewModel() {

    private var repository: InventoryRepositoryImpl? = null

    private val _productosState = MutableLiveData<List<ProductoResponse>>()
    val productosState: LiveData<List<ProductoResponse>> = _productosState

    private val _unidadesState = MutableLiveData<List<UnidadMedidaResponse>>()
    val unidadesState: LiveData<List<UnidadMedidaResponse>> = _unidadesState

    private val _errorState = MutableLiveData<String>()
    val errorState: LiveData<String> = _errorState

    private val _saveItemState = MutableLiveData<UiState<Unit>>()
    val saveItemState: LiveData<UiState<Unit>> = _saveItemState

    fun setRepository(repository: InventoryRepositoryImpl) {
        this.repository = repository
    }

    fun getProducto(cod_empresa: String, cod_articulo: String, cod_barra: String) {
        viewModelScope.launch {
            repository?.getProducto(cod_empresa, cod_articulo, cod_barra )?.fold(
                onSuccess = {
                    _productosState.value = it
                },
                onFailure = {
                    _errorState.value = it.message ?: "Error al buscar producto"
                }
            )
        }
    }

    fun getUnidadesMedida(cod_empresa: String, cod_articulo: String) {
        viewModelScope.launch {
            repository?.getUnidadMedida(cod_empresa, cod_articulo)?.fold(
                onSuccess = {
                    _unidadesState.value = it
                },
                onFailure = {
                    _errorState.value = it.message ?: "Error al obtener unidades de medida"
                }
            )
        }
    }


    fun guardarProducto(request: LoadInventoryRequest) {
        _saveItemState.value = UiState.Loading
        viewModelScope.launch {
            val result = repository?.loadInventory(request)
            result?.fold(
                onSuccess = { _saveItemState.value = UiState.Success(Unit) },
                onFailure = { _saveItemState.value = UiState.Error(it.message ?: "Error al guardar") }
            )
        }
    }
}
