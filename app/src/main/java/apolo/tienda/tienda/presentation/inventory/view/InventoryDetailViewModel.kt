package apolo.tienda.tienda.presentation.inventory.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apolo.tienda.tienda.data.remote.repository.InventoryRepositoryImpl
import apolo.tienda.tienda.data.remote.request.DeleteDetInventoryRequest
import apolo.tienda.tienda.data.remote.request.UpdateDetInventoryRequest
import apolo.tienda.tienda.data.remote.response.DetailInventoryResponse
import apolo.tienda.tienda.data.remote.response.UnidadMedidaResponse
import apolo.tienda.tienda.presentation.state.UiState
import kotlinx.coroutines.launch

class InventoryDetailViewModel : ViewModel() {

    private lateinit var repository: InventoryRepositoryImpl

    private val _detalleState = MutableLiveData<UiState<List<DetailInventoryResponse>>>()
    val detalleState: LiveData<UiState<List<DetailInventoryResponse>>> = _detalleState

    private val _deleteItemState = MutableLiveData<UiState<Unit>>()
    val deleteItemState: LiveData<UiState<Unit>> = _deleteItemState

    private val _updateItemState = MutableLiveData<UiState<Unit>>()
    val updateItemState: LiveData<UiState<Unit>> = _updateItemState

    private val _unidadesState = MutableLiveData<List<UnidadMedidaResponse>>()
    val unidadesState: LiveData<List<UnidadMedidaResponse>> = _unidadesState

    private val _errorState = MutableLiveData<String>()
    val errorState: LiveData<String> = _errorState

    fun setRepository(repositoryImpl: InventoryRepositoryImpl) {
        this.repository = repositoryImpl
    }

    fun getDetalleInventario(codEmpresa: String, nroRegistro: String) {
        _detalleState.value = UiState.Loading

        viewModelScope.launch {
            val result = repository.getDetailInventory(codEmpresa, nroRegistro)

            result.fold(
                onSuccess = { _detalleState.value = UiState.Success(it) },
                onFailure = { _detalleState.value = UiState.Error(it.message ?: "Error al obtener el detalle") }
            )
        }
    }

    fun deleteDetInventario(item: DetailInventoryResponse) {
        _deleteItemState.value = UiState.Loading

        var requestDelete = DeleteDetInventoryRequest(
            cod_empresa = item.cod_empresa,
            nro_registro = item.nro_registro,
            nro_orden = item.nro_orden
            )

        viewModelScope.launch {
            val result = repository.deleteDetInventory(requestDelete)
            result.fold(
                onSuccess = { _deleteItemState.value = UiState.Success(Unit) },
                onFailure = { _deleteItemState.value = UiState.Error(it.message ?: "Error al eliminar") }
            )
        }


    }


    fun updateDetInventario(item: DetailInventoryResponse) {
        _updateItemState.value = UiState.Loading

        var requestUpdate = UpdateDetInventoryRequest(
            cod_empresa = item.cod_empresa,
            nro_registro = item.nro_registro,
            nro_orden = item.nro_orden,
            cantidad = item.cantidad,
            cod_unidad_rel = item.cod_unidad_medida
        )

        viewModelScope.launch {
            val result = repository.updateDetInventory(requestUpdate)
            result.fold(
                onSuccess = { _updateItemState.value = UiState.Success(Unit) },
                onFailure = { _updateItemState.value = UiState.Error(it.message ?: "Error al modificar") }
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
}
