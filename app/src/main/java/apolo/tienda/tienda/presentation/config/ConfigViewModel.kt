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

    fun saveIp(ip: String) {
        preferencesHelper.saveBackendIp(ip)
    }

    fun getSavedPort(): String {
        return preferencesHelper.getBackendPort()
    }

    fun savePort(port: String) {
        preferencesHelper.saveBackendPort(port)
    }
}
