package apolo.tienda.tienda.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import apolo.tienda.tienda.data.local.database.DatabaseHelper
import apolo.tienda.tienda.data.local.schema.SchemaParser
import apolo.tienda.tienda.data.local.schema.Tabla
import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream


suspend fun importarDesdeZip(
    context: Context,
    onProgress: (current: Int, total: Int, tabla: String) -> Unit
) {
    val zipFile = File(context.filesDir, "datos.zip")
    val outputDir = context.filesDir

    outputDir.listFiles()?.forEach { file ->
        if (file.extension == "json") file.delete()
    }

    ZipInputStream(FileInputStream(zipFile)).use { zipInputStream ->
        var entry = zipInputStream.nextEntry
        while (entry != null) {
            val outputFile = File(outputDir, entry.name)
            if (entry.isDirectory) {
                outputFile.mkdirs()
            } else {
                outputFile.outputStream().use { output ->
                    zipInputStream.copyTo(output)
                }
            }
            zipInputStream.closeEntry()
            entry = zipInputStream.nextEntry
        }
    }

    zipFile.delete()

    val estructuraFile = File(outputDir, "estructura.json")
    val schema = SchemaParser.parse(context, estructuraFile)
        ?: throw Exception("Error al leer estructura.json")

    val dbHelper = DatabaseHelper(context, schema)
    val db = dbHelper.writableDatabase

    val archivos = outputDir.listFiles()?.filter {
        it.extension == "json" && it.name != "estructura.json"
    } ?: emptyList()

    archivos.forEachIndexed { index, file ->
        val nombreArchivo = file.nameWithoutExtension.uppercase()
        val tabla = schema.tablas.find { it.nombre.equals(nombreArchivo, ignoreCase = true) }

        if (tabla == null) {
            println("⚠️ Archivo ${file.name} no tiene tabla asociada en estructura.json")
            return@forEachIndexed
        }

        val registros = Gson().fromJson(file.reader(), List::class.java) as List<Map<String, String>>
        insertarDatosEnTabla(db, tabla.nombre, registros, tabla)
        onProgress(index + 1, archivos.size, tabla.nombre)
    }

    db.close()
    dbHelper.close()
}

fun insertarDatosEnTabla(
    db: SQLiteDatabase,
    tableName: String,
    registros: List<Map<String, Any?>>,
    tabla: Tabla
) {
    db.beginTransaction()

    try {
        // 🔥 BORRAMOS SIEMPRE antes de insertar
        db.delete(tableName, null, null)

        registros.forEach { registro ->
            val valores = registro.filterKeys { it != "id" }
            val keys = valores.keys.joinToString(", ")
            val placeholders = valores.keys.joinToString(", ") { "?" }
            val sql = "INSERT INTO $tableName ($keys) VALUES ($placeholders)"
            val stmt = db.compileStatement(sql)
            /*valores.values.forEachIndexed { index, value ->
                stmt.bindString(index + 1, value ?: "")
            }*/


            valores.entries.forEachIndexed { index, entry ->
                val columna = tabla.columnas.find { it.nombre.equals(entry.key, ignoreCase = true) }
                val valor = entry.value
                // index + 1 porque SQLiteStatement usa 1-based index
                bindValorSQLite(stmt, index + 1, valor, columna?.tipo)
            }
            stmt.executeInsert()
            stmt.close()
        }

        db.setTransactionSuccessful()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        db.endTransaction()
    }


}

fun bindValorSQLite(stmt: SQLiteStatement, index: Int, valor: Any?, tipo: String?) {
    when (tipo?.uppercase()) {
        "INTEGER" -> stmt.bindLong(index, valor?.toString()?.toLongOrNull() ?: 0L)
        "REAL", "DOUBLE", "FLOAT", "NUMERIC" -> stmt.bindDouble(index, valor?.toString()?.toDoubleOrNull() ?: 0.0)
        else -> stmt.bindString(index, valor?.toString() ?: "")
    }
}
