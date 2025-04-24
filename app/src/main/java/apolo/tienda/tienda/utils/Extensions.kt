package apolo.tienda.tienda.utils

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import apolo.tienda.tienda.data.remote.response.ListInventoryResponse
import apolo.tienda.tienda.domain.model.Inventory

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.getBackendUrl(): String {
    val preferencesHelper = PreferencesHelper.getInstance()
    val ip = preferencesHelper.getBackendIp()
    val port = preferencesHelper.getBackendPort()

    return "http://$ip:$port/api/"
}


fun ListInventoryResponse.toDomain(): Inventory {
    return Inventory(
        nroRegistro = nro_registro.toIntOrNull() ?: 0,
        codEmpresa = cod_empresa,
        descEmpresa = desc_empresa,
        codSucursal = cod_sucursal,
        descSucursal = desc_sucursal,
        fecha = fecha,
        descripcion = descripcion
    )
}


fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
    val wrapper = object : Observer<T> {
        override fun onChanged(value: T) {
            removeObserver(this)
            observer(value)
        }
    }
    observe(owner, wrapper)
}