package apolo.tienda.tienda.old.utilidades

import apolo.tienda.tienda.old.conexion.Usuario

class SentenciasSQL {
    companion object{
        var sql: String = ""

        fun createTableUsuarios(): String{
            return "CREATE TABLE IF NOT EXISTS usuarios " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT  , NOMBRE TEXT       , " +
                    " LOGIN TEXT                            , TIPO TEXT         , " +
                    " ACTIVO TEXT                           , COD_EMPRESA TEXT  , " +
                    " VERSION TEXT                          , MIN_FOTOS   TEXT  , " +
                    " MAX_FOTOS TEXT                        , COD_PERSONA TEXT);"
        }

        fun createTableStArticulos():String{
            return "CREATE TABLE IF NOT EXISTS st_articulos " +
                    "(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "COD_ARTICULO TEXT," +
                    "COD_UNIDAD_REL TEXT," +
                    "DESCRIPCION TEXT," +
                    "REFERENCIA TEXT," +
                    "MEN_UN_VTA TEXT," +
                    "COD_BARRA_ART TEXT" +
                    ");" +
                    " CREATE INDEX IF NOT EXISTS IND_COD_ARTICULO on st_articulos (COD_ARTICULO);"
        }
        fun dropTableStArticulos():String{
            return "DROP TABLE IF EXISTS st_articulos "
        }
        fun sqlArticulo() : String{
            return "select A.COD_ARTICULO, A.COD_BARRA_ART, A.DESCRIPCION, B.COD_UNIDAD_REL, B.REFERENCIA, B.MEN_UN_VTA from st_articulos A,ST_RELACIONES B " +
                    "WHERE A.COD_EMPRESA = B.COD_EMPRESA" +
                    "  AND A.COD_ARTICULO = B.COD_ARTICULO" +
                    "  AND NVL(B.MEN_UN_VTA,'N') = 'S'" +
                    "  AND trim(A.COD_EMPRESA) = '${Usuario.empresa.trim()}'" +
                    "  "
        }
    }
}