package apolo.tienda.tienda.conexion

import android.app.ProgressDialog
import android.content.ContentValues
import android.os.AsyncTask
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class ConnectionDB {
    companion object {
        @Throws(Exception::class)
        fun getConnectionDB(usuario: String, clave: String): Connection {
            Propiedades.loadProperties(usuario, clave)
            val driver: String = Propiedades.getDbDriver()
            val url: String = Propiedades.getUrl()
            Locale.setDefault(Locale.US)
            Class.forName(driver)
            val conn: Connection
            conn = if (driver == "oracle.jdbc.driver.OracleDriver" || driver == "com.sybase.jdbc4.jdbc.SybDriver") {
                try {
                    DriverManager.getConnection(url.trim(), usuario, clave)
                } catch (e : Exception) {
                    e.message
                    DriverManager.getConnection(url)
                }
            }
            else {
                DriverManager.getConnection(url)
            }
            return conn
        }
    }

}