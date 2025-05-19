package apolo.tienda.tienda.data.remote.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import apolo.tienda.tienda.data.remote.api.ActualizacionApi
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ActualizacionRepositoryImpl(
    private val api: ActualizacionApi
) {
    suspend fun descargarApk(context: Context): Result<Uri> {
        return try {
            withContext(Dispatchers.IO) {
                val response = api.descargarApk()
                if (response.isSuccessful && response.body() != null) {
                    val apkFile = File(context.getExternalFilesDir(null), "actualizacion.apk")
                    response.body()!!.byteStream().use { input ->
                        FileOutputStream(apkFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider", // debe estar definido en AndroidManifest.xml
                        apkFile
                    )
                    Result.success(uri)
                } else {
                    Result.failure(Exception("No se pudo descargar el instalador"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
