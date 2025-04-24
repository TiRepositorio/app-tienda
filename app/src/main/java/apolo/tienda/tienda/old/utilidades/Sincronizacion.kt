package apolo.tienda.tienda.old.utilidades

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import apolo.tienda.tienda.old.Inventario
import apolo.tienda.tienda.old.MainActivity
import apolo.tienda.tienda.R
import apolo.tienda.tienda.databinding.ActivitySincronizacionBinding
import java.text.DecimalFormat
import java.util.*

@Suppress("DEPRECATION", "ClassName")
class Sincronizacion : AppCompatActivity() {

    private lateinit var imeiBD: String

    private lateinit var binding: ActivitySincronizacionBinding

    companion object{
        var tipoSinc: String = "T"
        lateinit var context: Context
        var primeraVez = false
        var contador = 0
        var nf = DecimalFormat("000")
    }
//    lateinit var enviarMarcacion : EnviarMarcacion

    @Suppress("ClassName")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySincronizacionBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setContentView(R.layout.activity_sincronizacion)
        context = this
        imeiBD = ""
        binding.tvImei.text = "Espere"

        try {
            preparaSincornizacion().execute()
        } catch(e: Exception){
            Log.println(Log.WARN, "Error",e.message!!)
        }
    }

    @Suppress("ClassName")
    @SuppressLint("StaticFieldLeak")
    private inner class preparaSincornizacion: AsyncTask<Void, Void, Void>(){
        lateinit var progressDialog: ProgressDialog
        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog(context)
            progressDialog.setMessage("Consultando disponibilidad")
            progressDialog.setCancelable(false)
            progressDialog.show()
        }

        @SuppressLint("WrongThread", "SetTextI18n")
        override fun doInBackground(vararg p0: Void?): Void? {
            try {
                MainActivity.conexionWS.consultarArticulos()
            } catch (e:java.lang.Exception){
                runOnUiThread {
                    binding.tvImei.text = e.message
                }
            }
            if (MainActivity.listaProductoV.size<1){
                progressDialog.dismiss()
                runOnUiThread {
                    Toast.makeText(context,"Verifique su conexion intentarlo",Toast.LENGTH_SHORT).show()
                }
                finish()
                return null
            }

            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
            runOnUiThread {
                if (binding.tvImei.text.toString().indexOf("Espere")<0){
                    binding.btFinalizar.visibility = View.VISIBLE
                } else {
                    cargarRegistros()
                }
            }
        }
    }

    fun cargarRegistros(){
        if (tipoSinc == "T"){
            sincronizarTodo()
        }
    }

    private fun borrarTablasTodo(listaTablas: Vector<String>){
        for (i in 0 until listaTablas.size){
            val sql: String = "DROP TABLE IF EXISTS " + listaTablas[i].split(" ")[5]
            try {
                MainActivity.bd!!.execSQL(sql)
            } catch (e : Exception) {
                return
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun sincronizarTodo(){
        val th = Thread {
            runOnUiThread {
                binding.tvImei.text = binding.tvImei.text.toString() + "\n\nSincronizando"
            }
            borrarTablasTodo(MainActivity.tablasSincronizacion.listaSQLCreateTables())
            obtenerArchivosTodo(
                MainActivity.tablasSincronizacion.listaSQLCreateTables(),
                MainActivity.tablasSincronizacion.listaDatos()
            )
        }
        th.start()
    }

    @SuppressLint("SetTextI18n", "SdCardPath")
    private fun obtenerArchivosTodo(listaSQLCreateTable: Vector<String>,listaDatos:ArrayList<ArrayList<ContentValues>>):Boolean{
        runOnUiThread {
            binding.pbTabla.progress = 0
            binding.pbProgresoTotal.progress = 0
        }
        for (i in 0 until listaSQLCreateTable.size){
                MainActivity.bd.beginTransaction()
                try {
                    val sql: String = listaSQLCreateTable[i]

                    try {
                        MainActivity.bd.execSQL(sql)
                    } catch (e: Exception) {
                        return false
                    }

                    var cont = 0
                    runOnUiThread {
                        binding.tvImei.text = binding.tvImei.text.toString() + "\n${nf.format(i)} - " + listaSQLCreateTable[i].split(" ")[5]
                    }
                    for (j in 0 until listaDatos[i].size){

                        try {
                            MainActivity.bd.insert(listaSQLCreateTable[i].split(" ")[5],null,listaDatos[i][j])
                        } catch (e: Exception) {
                            runOnUiThread {
                                binding.tvImei.text = binding.tvImei.text.toString() + "\n\n" + e.message
                            }
                            return false
                        }
                        runOnUiThread {
                            cont++
                            var progreso : Int = (100/listaDatos[i].size)*(cont)
                            if (cont == listaDatos[i].size-1){
                                progreso = 100
                            }
                            binding.pbTabla.progress = progreso
                        }
                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        binding.tvImei.text = binding.tvImei.text.toString() + "\n\n" + e.message
                    }
                    return false
                }
                runOnUiThread {
                    binding.pbProgresoTotal.progress = (100/listaSQLCreateTable.size)*(i+1)
                }
            MainActivity.bd.setTransactionSuccessful()
            MainActivity.bd.endTransaction()
        }
        runOnUiThread {
            binding.pbProgresoTotal.progress = 100
            binding.btFinalizar.visibility = View.VISIBLE
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        return
    }

    fun cerrar(view: View) {
        startActivity(Intent(this, Inventario::class.java))
        primeraVez = false
        finish()
    }

}
