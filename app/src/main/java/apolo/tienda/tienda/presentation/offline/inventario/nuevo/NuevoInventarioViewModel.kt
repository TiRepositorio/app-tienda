package apolo.tienda.tienda.presentation.offline.inventario.nuevo

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apolo.tienda.tienda.data.local.api.InventarioLocalRepository
import apolo.tienda.tienda.data.local.request.NuevoInventarioCabeceraLocalRequest
import apolo.tienda.tienda.data.local.response.ListaInventarioResponse
import apolo.tienda.tienda.data.local.response.NuevoInventarioCabeceraLocalResponse
import apolo.tienda.tienda.data.remote.request.NewInventoryRequest
import apolo.tienda.tienda.data.remote.response.EmpresaResponse
import apolo.tienda.tienda.data.remote.response.NewInventoryResponse
import apolo.tienda.tienda.presentation.state.UiState
import kotlinx.coroutines.launch

class NuevoInventarioViewModel  : ViewModel() {


    private lateinit var repository: InventarioLocalRepository

    val listaInventarioState = MutableLiveData<List<ListaInventarioResponse>>()
    val nuevoInventarioState = MutableLiveData<UiState<NuevoInventarioCabeceraLocalResponse>>()
    val errorListaInventarioState = MutableLiveData<String>()
    val errorNewInventoryState = MutableLiveData<String>()

    fun setRepository(repository: InventarioLocalRepository) {
        this.repository = repository
    }

    fun getListaInventario(context: Context) {
        viewModelScope.launch {
            val result = repository.getListaInventario(context)
            result.onSuccess {
                listaInventarioState.value = it
            }.onFailure {
                errorListaInventarioState.value = it.message
            }
        }
    }




    fun crearInventarioLocal(context: Context, request: NuevoInventarioCabeceraLocalRequest) {
        viewModelScope.launch {
            nuevoInventarioState.value = UiState.Loading

            val result = repository.crearCabecera(context, request)

            result.fold(
                onSuccess = {
                    nuevoInventarioState.value = UiState.Success(it)
                },
                onFailure = {
                    nuevoInventarioState.value = UiState.Error(it.message ?: "Ocurrió un error")
                }
            )
        }
    }
}