package apolo.tienda.tienda.utils


import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object DataParser {

    /**
     * Parsea un archivo JSON de datos y devuelve una lista de registros.
     * Cada registro es un Map<columna, valor>.
     *
     * @param file Archivo JSON que contiene un array de objetos.
     * @return Lista de registros.
     */
    fun parseJsonFile(file: File): List<Map<String, String>> {
        val registros = mutableListOf<Map<String, String>>()

        if (!file.exists()) return registros

        try {
            val contenido = file.readText()
            val type = object : TypeToken<List<Map<String, String>>>() {}.type
            registros.addAll(Gson().fromJson(contenido, type))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return registros
    }
}
