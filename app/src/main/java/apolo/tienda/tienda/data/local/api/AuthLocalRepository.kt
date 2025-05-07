package apolo.tienda.tienda.data.local.api

import android.content.Context
import apolo.tienda.tienda.data.local.request.LoginLocalRequest
import apolo.tienda.tienda.data.local.response.LoginLocalResponse

interface AuthLocalRepository {
    fun login(context: Context, request: LoginLocalRequest): LoginLocalResponse?
}
