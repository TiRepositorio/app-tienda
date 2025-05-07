package apolo.tienda.tienda.presentation.offline.inventario.listar

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apolo.tienda.tienda.data.local.api.InventarioLocalApiImpl
import apolo.tienda.tienda.data.local.response.ListaTomaInventarioResponse
import apolo.tienda.tienda.data.remote.request.EnviarTomaInventarioRequest
import apolo.tienda.tienda.domain.repository.InventoryRepository
import apolo.tienda.tienda.presentation.state.UiState
import apolo.tienda.tienda.utils.PreferencesHelper
import kotlinx.coroutines.launch

class ListarTomaInventariosViewModel : ViewModel() {

    private lateinit var repository: InventarioLocalApiImpl
    private lateinit var repositoryRemote: InventoryRepository
    val listaTomaInventarioState = MutableLiveData<UiState<List<ListaTomaInventarioResponse>>>()
    val finalizarTomaInventarioState = MutableLiveData<UiState<Unit>>()
    val enviarTomaInventarioState = MutableLiveData<UiState<Unit>>()

    fun setRepository(repository: InventarioLocalApiImpl) {
        this.repository = repository
    }

    fun setRepositoryRemote(repository: InventoryRepository) {
        this.repositoryRemote = repository
    }

    fun getListTomaInventario(context: Context) {
        listaTomaInventarioState.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.getListaTomaInventario(context)
            result.fold(
                onSuccess = { list ->
                    listaTomaInventarioState.value = UiState.Success(list ?: emptyList())
                },
                onFailure = {
                    listaTomaInventarioState.value = UiState.Error(it.message ?: "Error desconocido")
                }
            )
        }
    }


    fun finalizarTomaInventario(context: Context, idCabecera: Int) {
        finalizarTomaInventarioState.value = UiState.Loading

        viewModelScope.launch {
            val result = repository.finalizarTomaInventario(context, idCabecera)

            result.fold(
                onSuccess = {
                    finalizarTomaInventarioState.value = UiState.Success(Unit)
                    getListTomaInventario(context) // refrescar lista si querés
        },
                onFailure = {
                    finalizarTomaInventarioState.value = UiState.Error(it.message ?: "No se pudo finalizar el inventario")
                }
            )
        }
    }


    fun enviarTomaInventario(
        context: Context,
        idInventarioCabecera: Int
    ) {
        viewModelScope.launch {
            enviarTomaInventarioState.value = UiState.Loading

            // Paso 1: Obtener productos pendientes desde base local
            val productosResult = repository.getDetallePendienteEnvio(context, idInventarioCabecera)

            productosResult.fold(
                onSuccess = { listaProductos ->
                    if (listaProductos.isEmpty()) {
                        enviarTomaInventarioState.value = UiState.Error("No hay productos pendientes de envío.")
                        return@launch
                    }

                    // Paso 2: Enviar a backend
                    val request = EnviarTomaInventarioRequest(
                        idInventario = idInventarioCabecera.toLong(),
                        productos = listaProductos
                    )

                    val envioResult = repositoryRemote.enviarTomaInventario(request,
                                                                            PreferencesHelper.getInstance().getIdDispositivo(),
                                                                            PreferencesHelper.getInstance().getUUID())

                    envioResult.fold(
                        onSuccess = {
                            // Paso 3: Marcar como enviados
                            val marcarResult = repository.marcarTomaInventarioEnviado(context, idInventarioCabecera)
                            marcarResult.fold(
                                onSuccess = {
                                    enviarTomaInventarioState.value = UiState.Success(Unit)
                                },
                                onFailure = {
                                    enviarTomaInventarioState.value = UiState.Error("Enviado, pero no se pudo marcar como enviado localmente.")
                                }
                            )
                        },
                        onFailure = {
                            enviarTomaInventarioState.value = UiState.Error("Error al enviar al servidor: ${it.message}")
                        }
                    )
                },
                onFailure = {
                    enviarTomaInventarioState.value = UiState.Error("Error al obtener productos locales: ${it.message}")
                }
            )
        }
    }

}