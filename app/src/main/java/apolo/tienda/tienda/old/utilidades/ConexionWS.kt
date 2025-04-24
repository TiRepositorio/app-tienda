package apolo.tienda.tienda.old.utilidades

import android.content.ContentValues
import android.content.Context
import android.os.AsyncTask
import apolo.tienda.tienda.old.MainActivity
import apolo.tienda.tienda.old.conexion.ConnectionDB
import apolo.tienda.tienda.old.conexion.Usuario


class ConexionWS {

    companion object{
        lateinit var context : Context
        var resultado : String = ""
        private const val NAMESPACE: String = "http://edsystem/servidor"
        private var METHOD_NAME = ""
        private const val URL = "http://sistmov.apolo.com.py:8280/edsystemWS/edsystemWS/edsystem"
        private var SOAP_ACTION = "$NAMESPACE/$METHOD_NAME"
    }

    fun consultarArticulo(){
        Consultar(SentenciasSQL.sqlArticulo()).execute()
    }

    private class Consultar(private val sql:String) : AsyncTask<Void?, Void?, Void?>() {
        override fun onPreExecute() {
        }
        override fun doInBackground(vararg params: Void?): Void? {
            try {
                MainActivity.conn = ConnectionDB.getConnectionDB(Usuario.usuario, Usuario.clave)
                MainActivity.stm2 = MainActivity.conn.createStatement()
                MainActivity.rst2 = MainActivity.stm2.executeQuery(sql)
                MainActivity.listaProductoV = ArrayList()
                while (MainActivity.rst2.next()){
                    val dato = ContentValues()
                    dato.put("DESCRIPCION", MainActivity.rst2.getString("DESCRIPCION"))
                    dato.put("COD_UNIDAD_REL", MainActivity.rst2.getString("COD_UNIDAD_REL"))
                    dato.put("COD_ARTICULO", MainActivity.rst2.getString("COD_ARTICULO"))
                    dato.put("REFERENCIA", MainActivity.rst2.getString("REFERENCIA"))
                    dato.put("MEN_UN_VTA", MainActivity.rst2.getString("MEN_UN_VTA"))
                    dato.put("COD_BARRA_ART", MainActivity.rst2.getString("COD_BARRA_ART"))
                    MainActivity.listaProductoV.add(dato)
                }
                MainActivity.cerrarConexion()
            } catch (e : java.lang.Exception) {
                e.message
                MainActivity.cerrarConexion()
            }
            return null
        }
        override fun onPostExecute(result: Void?) {
            return
        }
    }

    fun consultarArticulos(){
        try {
            MainActivity.conn = ConnectionDB.getConnectionDB(Usuario.usuario, Usuario.clave)
            MainActivity.stm2 = MainActivity.conn.createStatement()
            MainActivity.rst2 = MainActivity.stm2.executeQuery(SentenciasSQL.sqlArticulo())
            MainActivity.listaProductoV = ArrayList()
            while (MainActivity.rst2.next()){
                val dato = ContentValues()
                dato.put("DESCRIPCION", MainActivity.rst2.getString("DESCRIPCION"))
                dato.put("COD_UNIDAD_REL", MainActivity.rst2.getString("COD_UNIDAD_REL"))
                dato.put("COD_ARTICULO", MainActivity.rst2.getString("COD_ARTICULO"))
                dato.put("REFERENCIA", MainActivity.rst2.getString("REFERENCIA"))
                dato.put("MEN_UN_VTA", MainActivity.rst2.getString("MEN_UN_VTA"))
                dato.put("COD_BARRA_ART", MainActivity.rst2.getString("COD_BARRA_ART"))
                MainActivity.listaProductoV.add(dato)
            }
            MainActivity.cerrarConexion()
        } catch (e : java.lang.Exception) {
            e.message
            MainActivity.cerrarConexion()
        }
    }


}