package apolo.tienda.tienda.presentation.inventory.list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apolo.tienda.tienda.data.remote.repository.InventoryRepositoryImpl
import apolo.tienda.tienda.data.remote.request.CloseInventoryRequest
import apolo.tienda.tienda.data.remote.response.CloseInventoryResponse
import apolo.tienda.tienda.data.remote.response.ListInventoryResponse
import apolo.tienda.tienda.presentation.state.UiState
import kotlinx.coroutines.launch

class ListInventoryViewModel : ViewModel() {

    private lateinit var repository: InventoryRepositoryImpl
    val listInventoryState = MutableLiveData<UiState<List<ListInventoryResponse>>>()
    val closeInventoryState = MutableLiveData<UiState<CloseInventoryResponse>>()

    fun setRepository(repository: InventoryRepositoryImpl) {
        this.repository = repository
    }

    fun getListInventory(cod_empresa: String) {
        listInventoryState.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.getListInventory(cod_empresa)
            result.fold(
                onSuccess = { list ->
                    listInventoryState.value = UiState.Success(list ?: emptyList())
                },
                onFailure = {
                    listInventoryState.value = UiState.Error(it.message ?: "Error desconocido")
                }
            )
        }
    }


    fun closeInventory(request: CloseInventoryRequest) {
        closeInventoryState.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.closeInventory(request)
            result.fold(
                onSuccess = { list ->
                    closeInventoryState.value = UiState.Success(list)
                },
                onFailure = {
                    closeInventoryState.value = UiState.Error(it.message ?: "Error desconocido")
                }
            )
        }
    }
}
