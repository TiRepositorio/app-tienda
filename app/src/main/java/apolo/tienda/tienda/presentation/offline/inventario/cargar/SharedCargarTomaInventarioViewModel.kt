package apolo.tienda.tienda.presentation.offline.inventario.cargar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedCargarTomaInventarioViewModel : ViewModel() {

    private val _actualizarDetalle = MutableLiveData<Boolean>()
    val actualizarDetalle: LiveData<Boolean> = _actualizarDetalle

    fun notificarActualizacion() {
        _actualizarDetalle.value = true
    }

    fun notificacionConsumida() {
        _actualizarDetalle.value = false
    }
}