package apolo.tienda.tienda.data.remote.interceptor

import apolo.tienda.tienda.utils.PreferencesHelper
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(
    private val preferencesHelper: PreferencesHelper
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()

        val token = preferencesHelper.getToken()
        if (!token.isNullOrEmpty()) {
            request.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(request.build())
    }
}
