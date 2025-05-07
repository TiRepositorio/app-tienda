package apolo.tienda.tienda.data.local.database

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import apolo.tienda.tienda.data.local.schema.Columna
import apolo.tienda.tienda.data.local.schema.Schema
import apolo.tienda.tienda.data.local.schema.Tabla

class DatabaseHelper(
    context: Context,
    private val schema: Schema
) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    schema.version
) {

    override fun onCreate(db: SQLiteDatabase) {
        schema.tablas.forEach { tabla ->
            val sql = buildCreateTableSQL(tabla)
            db.execSQL(sql)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        schema.tablas.forEach { tabla ->
            if (tabla.persistente) {
                actualizarEstructuraTabla(db, tabla)
            } else {
                db.execSQL("DROP TABLE IF EXISTS ${tabla.nombre}")
                val sql = buildCreateTableSQL(tabla)
                db.execSQL(sql)
            }
        }
    }

    private fun actualizarEstructuraTabla(db: SQLiteDatabase, tabla: Tabla) {
        val columnasExistentes = obtenerColumnasExistentes(db, tabla.nombre)

        tabla.columnas.forEach { columna ->
            if (!columnasExistentes.contains(columna.nombre)) {
                val alterSql = "ALTER TABLE ${tabla.nombre} ADD COLUMN ${buildColumnDefinition(columna)}"
                db.execSQL(alterSql)
            }
        }
    }

    private fun obtenerColumnasExistentes(db: SQLiteDatabase, nombreTabla: String): Set<String> {
        val columnas = mutableSetOf<String>()
        val cursor: Cursor = db.rawQuery("PRAGMA table_info($nombreTabla)", null)
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex("name")
            do {
                val columnName = cursor.getString(nameIndex)
                columnas.add(columnName)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return columnas
    }

    private fun buildCreateTableSQL(tabla: Tabla): String {
        val columnsSql = tabla.columnas.joinToString(", ") { columna ->
            buildColumnDefinition(columna)
        }
        return "CREATE TABLE IF NOT EXISTS ${tabla.nombre} ($columnsSql);"
    }

    private fun buildColumnDefinition(columna: Columna): String {
        val builder = StringBuilder()
        builder.append("${columna.nombre} ${columna.tipo}")

        if (columna.primaryKey) {
            builder.append(" PRIMARY KEY")
        }
        if (!columna.nullable) {
            builder.append(" NOT NULL")
        }
        // Si querés agregar default en el futuro, podés hacerlo acá.

        return builder.toString()
    }

    companion object {
        private const val DATABASE_NAME = "inventario.db"
    }
}
