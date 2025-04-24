package apolo.tienda.tienda.data.remote.api

import apolo.tienda.tienda.data.remote.request.LoginRequest
import apolo.tienda.tienda.data.remote.response.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
