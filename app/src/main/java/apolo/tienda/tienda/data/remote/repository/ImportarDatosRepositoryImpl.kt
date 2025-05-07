package apolo.tienda.tienda.data.remote.repository


import android.content.Context
import apolo.tienda.tienda.data.remote.api.ImportarDatosApi
import apolo.tienda.tienda.utils.importarDesdeZip
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImportarDatosRepositoryImpl(
    private val api: ImportarDatosApi
) {
    suspend fun importarDesdeZip(
        context: Context,
        onProgress: (current: Int, total: Int, tabla: String) -> Unit
    ): Result<Unit> {
        /*return try {
            val response = api.descargarZip()
            if (response.isSuccessful && response.body() != null) {
                // 1. Guardar el archivo ZIP en filesDir
                val zipFile = File(context.filesDir, "datos.zip")
                response.body()!!.byteStream().use { input ->
                    FileOutputStream(zipFile).use { output ->
                        input.copyTo(output)
                    }
                }

                // 2. Procesar la importación desde ese archivo físico
                apolo.tienda.tienda.utils.importarDesdeZip(context, onProgress)

                Result.success(Unit)
            } else {
                Result.failure(Exception("No se pudo descargar el archivo ZIP"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }*/
        return try {
            withContext(Dispatchers.IO) {
                val response = api.descargarZip()
                if (response.isSuccessful && response.body() != null) {
                    val zipFile = File(context.filesDir, "datos.zip")
                    response.body()!!.byteStream().use { input ->
                        FileOutputStream(zipFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    apolo.tienda.tienda.utils.importarDesdeZip(context, onProgress)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("No se pudo descargar el archivo ZIP"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}