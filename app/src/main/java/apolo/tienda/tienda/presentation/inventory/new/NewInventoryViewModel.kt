package apolo.tienda.tienda.presentation.inventory.new

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apolo.tienda.tienda.data.remote.request.NewInventoryRequest
import apolo.tienda.tienda.data.remote.response.EmpresaResponse
import apolo.tienda.tienda.data.remote.response.NewInventoryResponse
import apolo.tienda.tienda.data.remote.response.SucursalResponse
import apolo.tienda.tienda.domain.repository.InventoryRepository
import apolo.tienda.tienda.presentation.state.UiState
import kotlinx.coroutines.launch

class NewInventoryViewModel : ViewModel() {

    private lateinit var repository: InventoryRepository

    val empresasState = MutableLiveData<List<EmpresaResponse>>()
    val sucursalesState = MutableLiveData<List<SucursalResponse>>()
    val newInventoryState = MutableLiveData<UiState<NewInventoryResponse>>()
    val errorEmpresasState = MutableLiveData<String>()
    val errorSucursalesState = MutableLiveData<String>()
    val errorNewInventoryState = MutableLiveData<String>()

    fun setRepository(repository: InventoryRepository) {
        this.repository = repository
    }

    fun getEmpresas() {
        viewModelScope.launch {
            val result = repository.getEmpresas()
            result.onSuccess {
                empresasState.value = it
            }.onFailure {
                errorEmpresasState.value = it.message
            }
        }
    }

    fun getSucursales(cod_empresa: String) {
        viewModelScope.launch {
            val result = repository.getSucursales(cod_empresa)
            result.onSuccess {
                sucursalesState.value = it
            }.onFailure {
                errorSucursalesState.value = it.message
            }
        }
    }


    fun createInventory(request: NewInventoryRequest) {
        viewModelScope.launch {
            newInventoryState.value = UiState.Loading

            val result = repository.createInventory(request)

            result.fold(
                onSuccess = {
                    newInventoryState.value = UiState.Success(it)
                },
                onFailure = {
                    newInventoryState.value = UiState.Error(it.message ?: "Ocurrió un error")
                }
            )
        }
    }

}
