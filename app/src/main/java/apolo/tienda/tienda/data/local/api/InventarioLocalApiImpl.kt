package apolo.tienda.tienda.data.local.api

import android.content.Context
import apolo.tienda.tienda.data.local.database.DatabaseHelper
import apolo.tienda.tienda.data.local.request.NuevoInventarioCabeceraLocalRequest
import apolo.tienda.tienda.data.local.request.NuevoInventarioDetalleLocalRequest
import apolo.tienda.tienda.data.local.response.ListaDetalleTomaInventarioResponse
import apolo.tienda.tienda.data.local.response.ListaInventarioResponse
import apolo.tienda.tienda.data.local.response.ListaProductoResponse
import apolo.tienda.tienda.data.local.response.ListaTomaInventarioResponse
import apolo.tienda.tienda.data.local.response.NuevoInventarioCabeceraLocalResponse
import apolo.tienda.tienda.data.local.response.NuevoInventarioDetalleLocalResponse
import apolo.tienda.tienda.data.local.schema.SchemaParser
import java.io.File

class InventarioLocalApiImpl : InventarioLocalRepository {
    override fun getListaInventario(context: Context): Result<List<ListaInventarioResponse>> {
        return try {
            val estructuraFile = File(context.filesDir, "estructura.json")
            val schema = SchemaParser.parse(context, estructuraFile)
                ?: return Result.failure(Exception("No se pudo cargar la estructura"))

            val dbHelper = DatabaseHelper(context, schema)
            val db = dbHelper.readableDatabase
            val lista = mutableListOf<ListaInventarioResponse>()

            val query = """
                SELECT 
                    COD_EMPRESA,
                    COD_SUCURSAL,
                    COD_DEPOSITO,
                    NRO_REGISTRO,
                    DESCRIPCION_AJUSTE
                FROM INVENTARIO_ORIGEN
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM INVENTARIO_CABECERA cab
                    WHERE cab.nro_ajuste = INVENTARIO_ORIGEN.NRO_REGISTRO
                      AND cab.estado = 'P'
                )
                GROUP BY COD_EMPRESA, COD_SUCURSAL, COD_DEPOSITO, NRO_REGISTRO, DESCRIPCION_AJUSTE
            """.trimIndent()

            val cursor = db.rawQuery(query, null)
            if (cursor.moveToFirst()) {
                do {
                    lista.add(
                        ListaInventarioResponse(
                            codEmpresa = cursor.getString(cursor.getColumnIndexOrThrow("COD_EMPRESA")),
                            codSucursal = cursor.getString(cursor.getColumnIndexOrThrow("COD_SUCURSAL")),
                            codDeposito = cursor.getString(cursor.getColumnIndexOrThrow("COD_DEPOSITO")),
                            nroRegistro = cursor.getString(cursor.getColumnIndexOrThrow("NRO_REGISTRO")),
                            descAjuste = cursor.getString(cursor.getColumnIndexOrThrow("DESCRIPCION_AJUSTE")) ?: ""
                        )
                    )
                } while (cursor.moveToNext())
            }

            cursor.close()
            db.close()
            Result.success(lista)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override fun crearCabecera (
        context: Context,
        request: NuevoInventarioCabeceraLocalRequest
    ): Result<NuevoInventarioCabeceraLocalResponse> {
        return try {
            val estructuraFile = File(context.filesDir, "estructura.json")
            val schema = SchemaParser.parse(context, estructuraFile)
                ?: return Result.failure(Exception("No se pudo cargar la estructura"))

            val dbHelper = DatabaseHelper(context, schema)
            val db = dbHelper.writableDatabase

            val sql = """
            INSERT INTO INVENTARIO_CABECERA (nro_ajuste, usuario_alta, fecha_inventario, comentario, estado)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

            val statement = db.compileStatement(sql)
            statement.bindLong(1, request.nroAjuste.toLong())
            statement.bindString(2, request.usuarioAlta)
            statement.bindString(3, request.fechaInventario)
            statement.bindString(4, request.comentario)
            statement.bindString(5, request.estado)

            val id = statement.executeInsert()

            statement.close()
            db.close()
            dbHelper.close()

            Result.success(NuevoInventarioCabeceraLocalResponse(id = id.toInt()))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getListaTomaInventario(context: Context): Result<List<ListaTomaInventarioResponse>> {
        return try {
            val estructuraFile = File(context.filesDir, "estructura.json")
            val schema = SchemaParser.parse(context, estructuraFile)
                ?: return Result.failure(Exception("No se pudo cargar la estructura"))

            val dbHelper = DatabaseHelper(context, schema)
            val db = dbHelper.readableDatabase
            val lista = mutableListOf<ListaTomaInventarioResponse>()

            val query = """
                SELECT 
                    cab.id,
                    cab.nro_ajuste,
                    cab.fecha_inventario,
                    cab.comentario,
                    cab.usuario_alta,
                    cab.estado,
                    COUNT(det.id) as total_registros,
                    SUM(CASE WHEN det.enviado = 'S' THEN 1 ELSE 0 END) as total_enviados
                FROM INVENTARIO_CABECERA cab
                LEFT JOIN INVENTARIO_DETALLE det ON cab.id = det.ID_INVENTARIO_CABECERA
                WHERE cab.estado = 'P'
                GROUP BY cab.id
            """.trimIndent()

            val cursor = db.rawQuery(query, null)
            if (cursor.moveToFirst()) {
                do {
                    lista.add(
                        ListaTomaInventarioResponse(
                            nroAjuste = cursor.getString(cursor.getColumnIndexOrThrow("nro_ajuste")).toInt(),
                            fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha_inventario")),
                            comentario = cursor.getString(cursor.getColumnIndexOrThrow("comentario")),
                            usuario = cursor.getString(cursor.getColumnIndexOrThrow("usuario_alta")),
                            id = cursor.getString(cursor.getColumnIndexOrThrow("id")).toInt(),
                            estado = cursor.getString(cursor.getColumnIndexOrThrow("estado")),
                            totalRegistros = cursor.getInt(cursor.getColumnIndexOrThrow("total_registros")),
                            totalRegistrosEnviados = cursor.getInt(cursor.getColumnIndexOrThrow("total_enviados"))
                        )
                    )
                } while (cursor.moveToNext())
            }


            cursor.close()
            db.close()
            Result.success(lista)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getTomaInventario(context: Context, id: Long): Result<ListaTomaInventarioResponse> {
        return try {
            val estructuraFile = File(context.filesDir, "estructura.json")
            val schema = SchemaParser.parse(context, estructuraFile)
                ?: return Result.failure(Exception("No se pudo cargar la estructura"))

            val dbHelper = DatabaseHelper(context, schema)
            val db = dbHelper.readableDatabase

            val query = """
                SELECT 
                    id,
                    nro_ajuste,
                    fecha_inventario,
                    comentario,
                    usuario_alta,
                    estado 
                FROM INVENTARIO_CABECERA
                WHERE id = ?
            """.trimIndent()

            val cursor = db.rawQuery(query, arrayOf(id.toString()))

            val result = if (cursor.moveToFirst()) {
                ListaTomaInventarioResponse(
                    nroAjuste = cursor.getInt(cursor.getColumnIndexOrThrow("nro_ajuste")),
                    fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha_inventario")),
                    comentario = cursor.getString(cursor.getColumnIndexOrThrow("comentario")),
                    usuario = cursor.getString(cursor.getColumnIndexOrThrow("usuario_alta")),
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")).toInt(),
                    estado = cursor.getString(cursor.getColumnIndexOrThrow("estado")),
                    totalRegistros = 0,
                    totalRegistrosEnviados = 0
                )
            } else {
                null
            }

            cursor.close()
            db.close()
            dbHelper.close()

            if (result != null) {
                Result.success(result)
            } else {
                Result.failure(Exception("No se encontró el inventario con ID $id"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getProductos(
        context: Context,
        filtro: String,
        nroAjuste: String
    ): Result<List<ListaProductoResponse>> {
        return try {
            val estructuraFile = File(context.filesDir, "estructura.json")
            val schema = SchemaParser.parse(context, estructuraFile)
                ?: return Result.failure(Exception("No se pudo cargar la estructura"))

            val dbHelper = DatabaseHelper(context, schema)
            val db = dbHelper.readableDatabase
            val lista = mutableListOf<ListaProductoResponse>()

            val query = """
                SELECT 
                    id,
                    COD_EMPRESA,
                    COD_SUCURSAL,
                    COD_DEPOSITO,
                    COD_PRODUCTO,
                    COD_BARRA,
                    DESCRIPCION_PRODUCTO,
                    COD_UBICACION,
                    CANTIDAD,
                    NRO_REGISTRO,
                    DESCRIPCION_AJUSTE 
                    
                FROM INVENTARIO_ORIGEN
                WHERE NRO_REGISTRO = ?
                  AND (COD_PRODUCTO = ? OR COD_BARRA = ?)
            """.trimIndent()

            val cursor = db.rawQuery(query, arrayOf(nroAjuste, filtro, filtro))
            if (cursor.moveToFirst()) {
                do {
                    lista.add(
                        ListaProductoResponse(
                            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            codEmpresa = cursor.getString(cursor.getColumnIndexOrThrow("COD_EMPRESA")),
                            codSucursal = cursor.getString(cursor.getColumnIndexOrThrow("COD_SUCURSAL")),
                            codDeposito = cursor.getString(cursor.getColumnIndexOrThrow("COD_DEPOSITO")),
                            codProducto = cursor.getString(cursor.getColumnIndexOrThrow("COD_PRODUCTO")),
                            codBarra = cursor.getString(cursor.getColumnIndexOrThrow("COD_BARRA")),
                            descProducto = cursor.getString(cursor.getColumnIndexOrThrow("DESCRIPCION_PRODUCTO")),
                            codUbicacion = cursor.getString(cursor.getColumnIndexOrThrow("COD_UBICACION")),
                            cantidad = cursor.getString(cursor.getColumnIndexOrThrow("CANTIDAD")),
                            nroRegistro = cursor.getString(cursor.getColumnIndexOrThrow("NRO_REGISTRO")),
                            descAjuste = cursor.getString(cursor.getColumnIndexOrThrow("DESCRIPCION_AJUSTE"))
                        )
                    )
                } while (cursor.moveToNext())
            }


            cursor.close()
            db.close()
            Result.success(lista)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getProducto(context: Context, id: Int): Result<ListaProductoResponse> {
        return try {
            val estructuraFile = File(context.filesDir, "estructura.json")
            val schema = SchemaParser.parse(context, estructuraFile)
                ?: return Result.failure(Exception("No se pudo cargar la estructura"))

            val dbHelper = DatabaseHelper(context, schema)
            val db = dbHelper.readableDatabase

            val query = """
                SELECT 
                    id,
                    COD_EMPRESA,
                    COD_SUCURSAL,
                    COD_DEPOSITO,
                    COD_PRODUCTO,
                    COD_BARRA,
                    DESCRIPCION_PRODUCTO,
                    COD_UBICACION,
                    CANTIDAD,
                    NRO_REGISTRO,
                    DESCRIPCION_AJUSTE 
                    
                FROM INVENTARIO_ORIGEN
                WHERE id = ?
            """.trimIndent()

            val cursor = db.rawQuery(query, arrayOf(id.toString()))

            val result = if (cursor.moveToFirst()) {
                ListaProductoResponse(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    codEmpresa = cursor.getString(cursor.getColumnIndexOrThrow("COD_EMPRESA")),
                    codSucursal = cursor.getString(cursor.getColumnIndexOrThrow("COD_SUCURSAL")),
                    codDeposito = cursor.getString(cursor.getColumnIndexOrThrow("COD_DEPOSITO")),
                    codProducto = cursor.getString(cursor.getColumnIndexOrThrow("COD_PRODUCTO")),
                    codBarra = cursor.getString(cursor.getColumnIndexOrThrow("COD_BARRA")),
                    descProducto = cursor.getString(cursor.getColumnIndexOrThrow("DESCRIPCION_PRODUCTO")),
                    codUbicacion = cursor.getString(cursor.getColumnIndexOrThrow("COD_UBICACION")),
                    cantidad = cursor.getString(cursor.getColumnIndexOrThrow("CANTIDAD")),
                    nroRegistro = cursor.getString(cursor.getColumnIndexOrThrow("NRO_REGISTRO")),
                    descAjuste = cursor.getString(cursor.getColumnIndexOrThrow("DESCRIPCION_AJUSTE"))
                )
            } else {
                null
            }

            cursor.close()
            db.close()
            dbHelper.close()

            if (result != null) {
                Result.success(result)
            } else {
                Result.failure(Exception("No se encontró el inventario con ID $id"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun guardarProducto(
        context: Context,
        request: NuevoInventarioDetalleLocalRequest
    ): Result<NuevoInventarioDetalleLocalResponse> {
        return try {
            val estructuraFile = File(context.filesDir, "estructura.json")
            val schema = SchemaParser.parse(context, estructuraFile)
                ?: return Result.failure(Exception("No se pudo cargar la estructura"))

            val dbHelper = DatabaseHelper(context, schema)
            val db = dbHelper.writableDatabase

            val sql = """
                INSERT INTO INVENTARIO_DETALLE (ID_INVENTARIO_CABECERA, nro_ajuste, cod_producto, desc_producto, cantidad, cod_barra, stock_actual, cod_deposito, sector, movil, enviado)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()


            val statement = db.compileStatement(sql)
            statement.bindString(1, request.idInventarioCabecera.toString())
            statement.bindString(2, request.nroAjuste.toString())
            statement.bindString(3, request.codProducto)
            statement.bindString(4, request.descProducto)
            statement.bindDouble(5, request.cantidad)
            statement.bindString(6, request.codBarra)
            statement.bindDouble(7, request.stockActual)
            statement.bindString(8, request.codDeposito)
            statement.bindString(9, request.sector)
            statement.bindString(10, request.movil)
            statement.bindString(11, request.enviado)

            val id = statement.executeInsert()

            statement.close()
            db.close()
            dbHelper.close()

            Result.success(NuevoInventarioDetalleLocalResponse(id = id.toInt()))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDetalleTomaInventario(
        context: Context,
        idCabecera: Int
    ): Result<List<ListaDetalleTomaInventarioResponse>> {

        return try {
            val estructuraFile = File(context.filesDir, "estructura.json")
            val schema = SchemaParser.parse(context, estructuraFile)
                ?: return Result.failure(Exception("No se pudo cargar la estructura"))

            val dbHelper = DatabaseHelper(context, schema)
            val db = dbHelper.readableDatabase
            val lista = mutableListOf<ListaDetalleTomaInventarioResponse>()

            val query = """
                SELECT 
                    id,
                    ID_INVENTARIO_CABECERA, 
                    nro_ajuste, 
                    cod_producto, 
                    desc_producto, 
                    cantidad, 
                    cod_barra, 
                    stock_actual, 
                    cod_deposito, 
                    sector, 
                    movil, 
                    enviado
                     
                FROM INVENTARIO_DETALLE
                WHERE ID_INVENTARIO_CABECERA = ?
            """.trimIndent()

            val cursor = db.rawQuery(query, arrayOf(idCabecera.toString()))
            if (cursor.moveToFirst()) {
                do {
                    lista.add(
                        ListaDetalleTomaInventarioResponse(
                            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            idInventarioCabecera = cursor.getInt(cursor.getColumnIndexOrThrow("ID_INVENTARIO_CABECERA")),
                            nroAjuste = cursor.getInt(cursor.getColumnIndexOrThrow("nro_ajuste")),
                            codProducto = cursor.getString(cursor.getColumnIndexOrThrow("cod_producto")),
                            cantidad = cursor.getDouble(cursor.getColumnIndexOrThrow("cantidad")),
                            codBarra = cursor.getString(cursor.getColumnIndexOrThrow("cod_barra")),
                            stockActual = cursor.getDouble(cursor.getColumnIndexOrThrow("stock_actual")),
                            codDeposito = cursor.getString(cursor.getColumnIndexOrThrow("cod_deposito")),
                            sector = cursor.getString(cursor.getColumnIndexOrThrow("sector")),
                            movil = cursor.getString(cursor.getColumnIndexOrThrow("movil")),
                            enviado = cursor.getString(cursor.getColumnIndexOrThrow("enviado")),
                            descProducto = cursor.getString(cursor.getColumnIndexOrThrow("desc_producto")),
                        )
                    )
                } while (cursor.moveToNext())
            }


            cursor.close()
            db.close()
            Result.success(lista)

        } catch (e: Exception) {
            Result.failure(e)
        }

    }


    override fun finalizarTomaInventario(
        context: Context,
        idCabecera: Int
    ): Result<Unit> {
        return try {
            val estructuraFile = File(context.filesDir, "estructura.json")
            val schema = SchemaParser.parse(context, estructuraFile)
                ?: return Result.failure(Exception("No se pudo cargar la estructura"))

            val dbHelper = DatabaseHelper(context, schema)
            val db = dbHelper.writableDatabase

            val sql = """
                UPDATE INVENTARIO_CABECERA 
                SET estado = 'F'
                WHERE id = ?
            """.trimIndent()

            val statement = db.compileStatement(sql)
            statement.bindLong(1, idCabecera.toLong())

            statement.executeUpdateDelete()

            statement.close()
            db.close()
            dbHelper.close()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDetallePendienteEnvio(
        context: Context,
        idCabecera: Int
    ): Result<List<ListaDetalleTomaInventarioResponse>> {
        return try {
            val estructuraFile = File(context.filesDir, "estructura.json")
            val schema = SchemaParser.parse(context, estructuraFile)
                ?: return Result.failure(Exception("No se pudo cargar la estructura"))

            val dbHelper = DatabaseHelper(context, schema)
            val db = dbHelper.readableDatabase

            val lista = mutableListOf<ListaDetalleTomaInventarioResponse>()

            val query = """
                SELECT 
                    id,
                    ID_INVENTARIO_CABECERA,
                    nro_ajuste,
                    cod_producto, 
                    desc_producto, 
                    cantidad, 
                    cod_barra, 
                    stock_actual, 
                    cod_deposito, 
                    sector, 
                    movil,
                    enviado
                FROM INVENTARIO_DETALLE
                WHERE ID_INVENTARIO_CABECERA = ? AND enviado = 'N'
            """.trimIndent()

            val cursor = db.rawQuery(query, arrayOf(idCabecera.toString()))

            if (cursor.moveToFirst()) {
                do {
                    lista.add(
                        ListaDetalleTomaInventarioResponse(
                            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            idInventarioCabecera = cursor.getInt(cursor.getColumnIndexOrThrow("ID_INVENTARIO_CABECERA")),
                            nroAjuste = cursor.getInt(cursor.getColumnIndexOrThrow("nro_ajuste")),
                            codProducto = cursor.getString(cursor.getColumnIndexOrThrow("cod_producto")),
                            cantidad = cursor.getDouble(cursor.getColumnIndexOrThrow("cantidad")),
                            codBarra = cursor.getString(cursor.getColumnIndexOrThrow("cod_barra")),
                            stockActual = cursor.getDouble(cursor.getColumnIndexOrThrow("stock_actual")),
                            codDeposito = cursor.getString(cursor.getColumnIndexOrThrow("cod_deposito")),
                            sector = cursor.getString(cursor.getColumnIndexOrThrow("sector")),
                            movil = cursor.getString(cursor.getColumnIndexOrThrow("movil")),
                            enviado = cursor.getString(cursor.getColumnIndexOrThrow("enviado")),
                            descProducto = cursor.getString(cursor.getColumnIndexOrThrow("desc_producto")),
                        )
                    )
                } while (cursor.moveToNext())
            }

            cursor.close()
            db.close()
            dbHelper.close()

            Result.success(lista)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun marcarTomaInventarioEnviado(context: Context, idCabecera: Int): Result<Unit> {
        return try {
            val estructuraFile = File(context.filesDir, "estructura.json")
            val schema = SchemaParser.parse(context, estructuraFile)
                ?: return Result.failure(Exception("No se pudo cargar la estructura"))

            val dbHelper = DatabaseHelper(context, schema)
            val db = dbHelper.writableDatabase

            val sql = """
            UPDATE INVENTARIO_DETALLE
            SET enviado = 'S'
            WHERE ID_INVENTARIO_CABECERA = ? AND enviado = 'N'
        """.trimIndent()

            val statement = db.compileStatement(sql)
            statement.bindLong(1, idCabecera.toLong())
            statement.executeUpdateDelete()

            statement.close()
            db.close()
            dbHelper.close()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
