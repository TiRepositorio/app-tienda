package apolo.tienda.tienda.data.local.repository

import android.content.Context
import apolo.tienda.tienda.data.local.api.AuthLocalRepository
import apolo.tienda.tienda.data.local.request.LoginLocalRequest
import apolo.tienda.tienda.data.local.response.LoginLocalResponse

class AuthLocalRepositoryImpl(
    private val api: AuthLocalRepository,
    private val context: Context
) {
    fun login(request: LoginLocalRequest): Result<LoginLocalResponse> {
        return try {
            val response = api.login(context, request)
            if (response != null) {
                Result.success(response)
            } else {
                Result.failure(Exception("Usuario o clave incorrectos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
