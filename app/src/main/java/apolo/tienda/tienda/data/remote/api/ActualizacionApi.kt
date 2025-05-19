package apolo.tienda.tienda.data.remote.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming

interface ActualizacionApi {
    @GET("update/descargar-instalador")
    @Streaming
    suspend fun descargarApk(): Response<ResponseBody>
}
