package apolo.tienda.tienda.data.local.schema


import android.content.Context
import com.google.gson.Gson
import java.io.File
import java.io.InputStreamReader

object SchemaParser {

    /**
     * Lee un archivo JSON desde almacenamiento interno y devuelve el objeto Schema.
     * @param context Contexto de la aplicación.
     * @param jsonFile File que apunta al archivo estructura.json descomprimido.
     * @return Schema parseado o null si falla.
     */
    fun parse(context: Context, jsonFile: File): Schema? {
        return try {
            val reader = InputStreamReader(jsonFile.inputStream())
            val schema = Gson().fromJson(reader, Schema::class.java)
            reader.close()
            schema
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
