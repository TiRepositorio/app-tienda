package apolo.tienda.tienda.data.remote.interceptor

import android.content.Context
import android.content.Intent
import android.widget.Toast
import apolo.tienda.tienda.presentation.login.LoginActivity
import apolo.tienda.tienda.utils.AppContextHolder
import apolo.tienda.tienda.utils.PreferencesHelper
import apolo.tienda.tienda.utils.showToast
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject

class AuthInterceptor(
    private val preferencesHelper: PreferencesHelper
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        if (response.code == 401 || response.code == 403) {
            preferencesHelper.clearToken()

            val errorBody = response.peekBody(Long.MAX_VALUE).string() // Leemos el body sin consumirlo

            val mensajeError = obtenerMensajeDeError(errorBody)

            preferencesHelper.saveMsgError(mensajeError)

            // Redirigir al Login con el ApplicationContext
            val intent = Intent(AppContextHolder.getContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            AppContextHolder.getContext().startActivity(intent)
        }

        return response
    }

    private fun obtenerMensajeDeError(errorBody: String): String {
        return try {
            val json = JSONObject(errorBody)
            json.getString("error")
        } catch (e: Exception) {
            "Token inválido"
        }
    }
}

