package apolo.tienda.tienda.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferencesHelper private constructor(context: Context) {

    private val PREF_NAME = "app_preferences"
    private val KEY_IP = "backend_ip"
    private val KEY_PORT = "backend_port"
    private val KEY_TOKEN = "token"
    private val KEY_MSG_ERROR = "msg_error"
    private val KEY_COD_EMPRESA = "cod_empresa"
    private val KEY_COD_SUCURSAL = "cod_sucursal"
    private val KEY_DESC_EMPRESA = "desc_empresa"
    private val KEY_DESC_SUCURSAL = "desc_sucursal"
    private val KEY_USER_PERMISOS = "user_permisos"
    private val KEY_USER = "user"
    private val KEY_ID_DISPOSITIVO = "id_dispositivo"
    private val KEY_OFFLINE = "offline"
    private val KEY_UUID = "uuid"


    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var instance: PreferencesHelper? = null

        fun init(context: Context) {
            if (instance == null) {
                instance = PreferencesHelper(context.applicationContext)
            }
        }

        fun getInstance(): PreferencesHelper {
            return instance ?: throw IllegalStateException("PreferencesHelper must be initialized")
        }
    }

    fun saveBackendIp(ip: String) {
        prefs.edit { putString(KEY_IP, ip) }
    }

    fun getBackendIp(): String {
        return prefs.getString(KEY_IP, "").orEmpty()
    }

    fun saveBackendPort(port: String) {
        prefs.edit { putString(KEY_PORT, port) }
    }

    fun getBackendPort(): String {
        return prefs.getString(KEY_PORT, "80").orEmpty()
    }

    fun saveToken(token: String) {
        prefs.edit { putString(KEY_TOKEN, token) }
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun clearToken() {
        prefs.edit { remove(KEY_TOKEN) }
    }


    fun saveMsgError(msg: String) {
        prefs.edit { putString(KEY_MSG_ERROR, msg) }
    }

    fun getMsgError(): String? {
        return prefs.getString(KEY_MSG_ERROR, null)
    }

    fun clearMsgError() {
        prefs.edit { remove(KEY_MSG_ERROR) }
    }


    fun saveCodEmpresa(cod: String) {
        prefs.edit() { putString(KEY_COD_EMPRESA, cod) }
    }

    fun getCodEmpresa(): String? {
        return prefs.getString(KEY_COD_EMPRESA, null)
    }

    fun saveCodSucursal(cod: String) {
        prefs.edit() { putString(KEY_COD_SUCURSAL, cod) }
    }

    fun getCodSucursal(): String? {
        return prefs.getString(KEY_COD_SUCURSAL, null)
    }

    fun saveDescEmpresa(desc: String) {
        prefs.edit() { putString(KEY_DESC_EMPRESA, desc) }
    }

    fun getDescEmpresa(): String? {
        return prefs.getString(KEY_DESC_EMPRESA, null)
    }

    fun saveDescSucursal(desc: String) {
        prefs.edit() { putString(KEY_DESC_SUCURSAL, desc) }
    }

    fun getDescSucursal(): String? {
        return prefs.getString(KEY_DESC_SUCURSAL, null)
    }


    fun saveUsuario(usuario: String) {
        prefs.edit { putString(KEY_USER, usuario) }
    }

    fun getUsuario(): String? {
        return prefs.getString(KEY_USER, null)
    }

    fun saveIdDispositivo(idDispositivo: String) {
        prefs.edit { putString(KEY_ID_DISPOSITIVO, idDispositivo) }
    }

    fun getIdDispositivo(): String {
        return prefs.getString(KEY_ID_DISPOSITIVO, "").orEmpty()
    }


    fun saveOffline(offline: Boolean) {
        prefs.edit { putBoolean(KEY_OFFLINE, offline) }
    }

    fun getOffline(): Boolean {
        return prefs.getBoolean(KEY_OFFLINE, false)
    }

    fun saveUUID(ip: String) {
        prefs.edit { putString(KEY_UUID, ip) }
    }

    fun getUUID(): String {
        return prefs.getString(KEY_UUID, "").orEmpty()
    }

}