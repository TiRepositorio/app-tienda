package apolo.tienda.tienda.presentation.config

import androidx.lifecycle.ViewModel
import apolo.tienda.tienda.utils.PreferencesHelper

class ConfigViewModel(
    private val preferencesHelper: PreferencesHelper
) : ViewModel() {

    fun getSavedIp(): String {
        return preferencesHelper.getBackendIp()
    }

    fun isValidIp(ip: String): Boolean {
        return android.util.Patterns.IP_ADDRESS.matcher(ip).matches()
    }


    fun getSavedPort(): String {
        return preferencesHelper.getBackendPort()
    }

    fun getSavedIdDispositivo(): String {
        return preferencesHelper.getIdDispositivo()
    }

    fun getSavedUUID(): String {
        return preferencesHelper.getUUID()
    }

    fun saveIp(ip: String) {
        preferencesHelper.saveBackendIp(ip)
    }

    fun savePort(port: String) {
        preferencesHelper.saveBackendPort(port)
    }

    fun saveIdDispotivio(idDispositivo: String) {
        preferencesHelper.saveIdDispositivo(idDispositivo)
    }
}
