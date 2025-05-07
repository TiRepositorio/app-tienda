package apolo.tienda.tienda.data.local.api

import android.content.Context
import apolo.tienda.tienda.data.local.database.DatabaseHelper
import apolo.tienda.tienda.data.local.request.LoginLocalRequest
import apolo.tienda.tienda.data.local.response.LoginLocalResponse
import apolo.tienda.tienda.data.local.schema.SchemaParser
import java.io.File

class AuthLocalApiImpl : AuthLocalRepository {
    override fun login(context: Context, request: LoginLocalRequest): LoginLocalResponse? {
        val estructuraFile = File(context.filesDir, "estructura.json")
        val schema = SchemaParser.parse(context, estructuraFile)
            ?: return null

        val dbHelper = DatabaseHelper(context, schema)
        val db = dbHelper.readableDatabase


        val cursor = db.rawQuery(
            """
                SELECT * FROM BS_USUARIOS 
                WHERE COD_USUARIO = ? COLLATE NOCASE 
                  AND CLAVE = ? COLLATE NOCASE
            """.trimIndent(),
            arrayOf(request.username, request.password)
        )

        val response = if (cursor.moveToFirst()) {
            LoginLocalResponse(
                codUsuario = cursor.getString(cursor.getColumnIndexOrThrow("COD_USUARIO")),
                codEmpresa = cursor.getString(cursor.getColumnIndexOrThrow("COD_EMPRESA")),
                codSucursal = cursor.getString(cursor.getColumnIndexOrThrow("COD_SUCURSAL"))
            )
        } else null

        cursor.close()
        db.close()
        return response
    }
}
