package apolo.tienda.tienda.data.remote.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming

interface ImportarDatosApi {
    @GET("importar-datos/")
    @Streaming
    suspend fun descargarZip(): Response<ResponseBody>
}