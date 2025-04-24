package apolo.tienda.tienda.domain.repository

import apolo.tienda.tienda.data.remote.request.LoginRequest
import apolo.tienda.tienda.data.remote.response.LoginResponse


interface AuthRepository {
    suspend fun login(username: String, password: String): Result<LoginResponse>
}
