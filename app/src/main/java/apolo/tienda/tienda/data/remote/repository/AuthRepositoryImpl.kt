package apolo.tienda.tienda.data.remote.repository

import apolo.tienda.tienda.data.remote.api.AuthApi
import apolo.tienda.tienda.data.remote.request.LoginRequest
import apolo.tienda.tienda.data.remote.response.LoginResponse
import apolo.tienda.tienda.domain.repository.AuthRepository
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class AuthRepositoryImpl(
    private val api: AuthApi
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(username, password))
            Result.success(response)
        } catch (e: HttpException) {
            val errorMessage = parseErrorMessage(e)
            Result.failure(Exception(errorMessage))

        } catch (e: IOException) {
            Result.failure(Exception("Error de red"))

        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado"))
        }
    }


    private fun parseErrorMessage(e: HttpException): String {
        return try {
            val errorJson = e.response()?.errorBody()?.string()
            val jsonObject = JSONObject(errorJson)
            jsonObject.getString("error") // o el campo que devuelva tu API
        } catch (ex: Exception) {
            "Error desconocido"
        }
    }

}
