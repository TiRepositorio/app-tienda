package apolo.tienda.tienda

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import apolo.tienda.tienda.conexion.ConnectionDB
import apolo.tienda.tienda.conexion.Usuario
import apolo.tienda.tienda.utilidades.ConexionWS
import apolo.tienda.tienda.utilidades.Sincronizacion
import apolo.tienda.tienda.utilidades.TablasSincronizacion
import apolo.tienda.tienda.utilidades.UtilidadesBD
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inicializarVariables()
    }

    private fun inicializarVariables(){
        context = this
        etAccion   = findViewById(R.id.accion)
        etUsuario  = findViewById(R.id.etUsuario)
        etPassword = findViewById(R.id.etPassword)
        btIngresar = findViewById(R.id.btIngresar)
        btIngresar.setOnClickListener { AbrirSesion().execute() }
        etAccion.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() == "ingresar"){
                    startActivity(Intent(context,Inventario::class.java))
                }
                if (s.toString() == "noIngresar"){
                    Toast.makeText(context,"Datos de usuario incorrectos o usuario inactivo.",Toast.LENGTH_SHORT).show()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

        } )
        utilidadesBD = UtilidadesBD(this, null)
        bd = utilidadesBD.writableDatabase
        conexionWS = ConexionWS()
        tablasSincronizacion = TablasSincronizacion()
    }

    companion object{

        var sesion = false
        private lateinit var etAccion   : EditText
        private lateinit var etUsuario  : EditText
        private lateinit var etPassword : EditText
        private lateinit var btIngresar : Button

        lateinit var listaProducto : ArrayList<HashMap<String,String>>
        lateinit var listaProductoV : ArrayList<ContentValues>
        lateinit var lista : ArrayList<HashMap<String,String>>
        lateinit var context : Context
        lateinit var conn: Connection
        lateinit var stm2: Statement
        lateinit var rst2 : ResultSet

        lateinit var utilidadesBD: UtilidadesBD
        lateinit var bd : SQLiteDatabase
        lateinit var conexionWS: ConexionWS
        lateinit var tablasSincronizacion:TablasSincronizacion
        var errorConexion = ""

        private fun abrirConexion(usuario:String, clave:String): Boolean {
            var resu = false
            try {
                conn = ConnectionDB.getConnectionDB(usuario,clave)
                stm2 = conn.createStatement()
                resu = true
            } catch (e: SQLException) {
                errorConexion = e.message.toString()
            }
            return resu
        }

        fun cerrarConexion(): Boolean {
            return try {
                conn.close()
                true
            } catch (e: java.lang.Exception) {
                true
            }
        }

        fun abrirSesion(): Boolean {
            return try {
                conn = ConnectionDB.getConnectionDB(Usuario.usuario, Usuario.clave)
                stm2 = conn.createStatement()
                val select = "select ESTADO,COD_SUCURSAL,COD_EMPRESA from usuarios a where a.cod_usuario = '" + Usuario.usuario + "'"
                val rs: ResultSet = stm2.executeQuery(select)
                sesion = false
                if (rs.next()) {
                    val estado: String = rs.getString("ESTADO")
                    if (estado == "A") {
                        sesion = true
                        Usuario.sucursal = rs.getString("COD_SUCURSAL")
                        Usuario.empresa  = rs.getString("COD_EMPRESA")
                    }
                }
                conn.close()
                sesion
            } catch (e: Exception) {
                errorConexion = e.message!!
                false
            }
        }

        private class Conexion : AsyncTask<Void?, Void?, Void?>() {
            override fun onPreExecute() {}
            override fun doInBackground(vararg params: Void?): Void? {
                Usuario.usuario = etUsuario.text.toString().toUpperCase()
                Usuario.clave   = etPassword.text.toString().toUpperCase()
                try {
                    conn = ConnectionDB.getConnectionDB(Usuario.usuario, Usuario.clave)
                } catch(e : Exception) {
                    cerrarConexion()
                }
                return null
            }
            override fun onPostExecute(result: Void?) {}
        }

        private class AbrirSesion : AsyncTask<Void?, Void?, Void?>() {
            override fun onPreExecute() {}
            override fun doInBackground(vararg params: Void?): Void? {
                Usuario.usuario = etUsuario.text.toString().toUpperCase()
                Usuario.clave   = etPassword.text.toString().toUpperCase()
                abrirSesion()
                return null
            }
            override fun onPostExecute(result: Void?) {
                if (sesion){
//                if (true){
                    etAccion.setText("ingresar")
                } else {
                    etAccion.setText("noIngresar")
                }
            }
        }

        fun dato(rs:ResultSet,index:String):String{
            return try {
                rs.getString(index.trim())
            } catch (e:java.lang.Exception){
                ""
            }
        }

        private class Consultar(private val sql:String,private val etAccionRem : EditText) : AsyncTask<Void?, Void?, Void?>() {
            override fun onPreExecute() {}
            override fun doInBackground(vararg params: Void?): Void? {
                Usuario.usuario = etUsuario.text.toString().toUpperCase()
                Usuario.clave   = etPassword.text.toString().toUpperCase()
                try {
                    conn = ConnectionDB.getConnectionDB(Usuario.usuario, Usuario.clave)
                    stm2 = conn.createStatement()
                    rst2 = stm2.executeQuery(sql)
                    listaProducto = ArrayList()
                    while (rst2.next()){
                        val dato = HashMap<String,String>()
                        dato["DESCRIPCION"]     = dato(rst2,"DESCRIPCION")
                        dato["COD_UNIDAD_REL"]  = dato(rst2,"COD_UNIDAD_REL")
                        dato["COD_ARTICULO"]  = dato(rst2,"COD_ARTICULO")
                        dato["REFERENCIA"]  = dato(rst2,"REFERENCIA")
                        dato["MEN_UN_VTA"]  = dato(rst2,"MEN_UN_VTA")
                        dato["COD_BARRA_ART"]  = dato(rst2,"COD_BARRA_ART")
                        listaProducto.add(dato)
                    }
                    cerrarConexion()
                } catch (e : java.lang.Exception) {
                    cerrarConexion()
                }
                return null
            }
            override fun onPostExecute(result: Void?) {
                etAccionRem.setText("asignarResultset")
            }
        }

        fun consultar(sql:String,etAccionRem : EditText){
            Consultar(sql,etAccionRem).execute()
        }

        fun insertar(tabla:String,campos:String,valores:String,etAccionRem: EditText){
            val sql = "INSERT INTO $tabla ($campos) values ($valores)"
            Insertar(sql, etAccionRem).execute()
        }

        private class Insertar(private val sql:String,private val etAccionRem : EditText) : AsyncTask<Void?, Void?, Void?>() {
            override fun onPreExecute() {}
            override fun doInBackground(vararg params: Void?): Void? {
                Usuario.usuario = etUsuario.text.toString().toUpperCase()
                Usuario.clave   = etPassword.text.toString().toUpperCase()
                try {
                    conn = ConnectionDB.getConnectionDB(Usuario.usuario, Usuario.clave)
                    stm2 = conn.createStatement()
                    stm2.executeQuery(sql)
                    cerrarConexion()
                } catch (e : java.lang.Exception) {
                    e.message
                    cerrarConexion()
                }
                return null
            }
            override fun onPostExecute(result: Void?) {
                etAccionRem.setText("limpiar")
            }
        }

        fun verExistencia(tabla:String,
                        where:String,
                        etAccionRem:EditText,
                        accion:String)
        {
            VerExistencia(tabla,where,etAccionRem,accion).execute()
        }

        private class VerExistencia(private val tabla:String,
                                   private val where:String,
                                   private val etAccionRem : EditText,
                                   private val accion:String) : AsyncTask<Void?, Void?, Void?>()
        {
            var existe = false
            override fun onPreExecute() {}
            override fun doInBackground(vararg params: Void?): Void? {
                Usuario.usuario = etUsuario.text.toString().toUpperCase()
                Usuario.clave   = etPassword.text.toString().toUpperCase()
                val sql = "select 1 from $tabla $where "
                try {
                    conn = ConnectionDB.getConnectionDB(Usuario.usuario, Usuario.clave)
                    stm2 = conn.createStatement()
                    rst2 = stm2.executeQuery(sql)
                    while (rst2.next()){
                        existe = true
                    }
                    cerrarConexion()
                } catch (e : java.lang.Exception) {
                    e.message
                    cerrarConexion()
                }
                return null
            }
            override fun onPostExecute(result: Void?) {
                if (existe){
                    etAccionRem.setText(accion)
                } else {
                    etAccionRem.setText("noExiste")
                }
            }
        }


        fun consultaGen(campos:String,
                        tabla:String,
                        where:String,
                        group:String,
                        order:String,
                        etAccionRem:EditText,
                        accion:String)
        {
            ConsultarGen(campos,tabla,where,group,order,etAccionRem,accion).execute()
        }

        private class ConsultarGen(private val campos:String,
                                   private val tabla:String,
                                   private val where: String,
                                   private val group:String,
                                   private val order:String,
                                   private val etAccionRem: EditText,
                                   private val accion:String) : AsyncTask<Void?, Void?, Void?>()
        {
            override fun onPreExecute() {}
            override fun doInBackground(vararg params: Void?): Void? {
                Usuario.usuario = etUsuario.text.toString().toUpperCase()
                Usuario.clave   = etPassword.text.toString().toUpperCase()
                val sql = "select $campos from $tabla $where $group $order"
                try {
                    conn = ConnectionDB.getConnectionDB(Usuario.usuario, Usuario.clave)
                    stm2 = conn.createStatement()
                    rst2 = stm2.executeQuery(sql)
                    lista = ArrayList()
                    while (rst2.next()){
                        val dato = HashMap<String,String>()
                        for (i in campos.split(",").indices){
                            dato[campos.split(",")[i].trim()] = dato(rst2,campos.split(",")[i].trim())
                        }
                        lista.add(dato)
                    }
                    cerrarConexion()
                } catch (e : java.lang.Exception) {
                    e.message
                    cerrarConexion()
                }
                return null
            }
            override fun onPostExecute(result: Void?) {
                etAccionRem.setText(accion)
            }
        }

        fun ejecutarSQL(sql:String,
                        etAccionRem:EditText,
                        accion:String)
        {
            EjecutarSQL(sql,etAccionRem,accion).execute()
        }

        private class EjecutarSQL(private val sql:String,
                                   private val etAccionRem: EditText,
                                   private val accion:String) : AsyncTask<Void?, Void?, Void?>()
        {
            override fun onPreExecute() {}
            override fun doInBackground(vararg params: Void?): Void? {
                Usuario.usuario = etUsuario.text.toString().toUpperCase()
                Usuario.clave   = etPassword.text.toString().toUpperCase()
                try {
                    conn = ConnectionDB.getConnectionDB(Usuario.usuario, Usuario.clave)
                    stm2 = conn.createStatement()
                    rst2 = stm2.executeQuery(sql)
                    cerrarConexion()
                } catch (e : java.lang.Exception) {
                    e.message
                    cerrarConexion()
                }
                return null
            }
            override fun onPostExecute(result: Void?) {
                etAccionRem.setText(accion)
            }
        }

    }







}