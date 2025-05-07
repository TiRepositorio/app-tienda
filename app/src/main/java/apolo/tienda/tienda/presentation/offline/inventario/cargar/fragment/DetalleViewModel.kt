package apolo.tienda.tienda.presentation.offline.inventario.cargar.fragment

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apolo.tienda.tienda.data.local.api.InventarioLocalApiImpl
import apolo.tienda.tienda.data.local.response.ListaDetalleTomaInventarioResponse
import apolo.tienda.tienda.presentation.state.UiState
import kotlinx.coroutines.launch

class DetalleViewModel : ViewModel() {

    private lateinit var repository: InventarioLocalApiImpl

    private val _detalleState = MutableLiveData<UiState<List<ListaDetalleTomaInventarioResponse>>>()
    val detalleState: LiveData<UiState<List<ListaDetalleTomaInventarioResponse>>> = _detalleState

    private val _errorState = MutableLiveData<String>()
    val errorState: LiveData<String> = _errorState

    fun setRepository(repositoryImpl: InventarioLocalApiImpl) {
        this.repository = repositoryImpl
    }

    fun getDetalleTomaInventario(context: Context, idCabecera: Int) {
        _detalleState.value = UiState.Loading

        viewModelScope.launch {
            val result = repository.getDetalleTomaInventario(context, idCabecera)

            result.fold(
                onSuccess = { _detalleState.value = UiState.Success(it) },
                onFailure = { _detalleState.value = UiState.Error(it.message ?: "Error al obtener el detalle") }
            )
        }
    }






}
