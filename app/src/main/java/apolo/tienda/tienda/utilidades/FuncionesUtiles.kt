package apolo.tienda.tienda.utilidades

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.View
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import apolo.tienda.tienda.MainActivity
import apolo.tienda.tienda.R
import com.google.android.material.navigation.NavigationView
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FuncionesUtiles {

    //CONSTRUCTORES
    constructor()

    constructor(context: Context){
        this.context = context
    }

    //Variables
    companion object{
        lateinit var cursor: Cursor
        val usuario : HashMap<String, String> = HashMap()
        var posicionCabecera: Int = 0
        var posicionDetalle : Int = 0
        var posicionDetalle2: Int = 0
        var posicionGenerico: Int = 0
        var listaCabecera: ArrayList<HashMap<String, String>> = ArrayList()
        var listaDetalle: ArrayList<HashMap<String, String>> = ArrayList()
        var listaDetalle2: ArrayList<HashMap<String, String>> = ArrayList()
        var subListaDetalle: ArrayList<ArrayList<HashMap<String, String>>> = ArrayList()
        var subListaDetalle2: ArrayList<ArrayList<ArrayList<HashMap<String, String>>>> =  ArrayList()
        val formatoNumeroEntero: DecimalFormat = DecimalFormat("###,###,###.##")
        val formatoNumeroDecimal: DecimalFormat = DecimalFormat("###,###,##0.00")
        var formatoGenerico: NumberFormat = NumberFormat.getInstance()
        var ultimaVenta : Int = -1
    }

    //adaptador
    var llBuscar: LinearLayout? = null
    var spBuscar: Spinner?  = null
    var etBuscar: EditText? = null
    var btBuscar: Button?   = null
    var imgTitulo: ImageView? = null
    var tvTitulo: TextView? = null
    private var imgAnterior: ImageView? = null
    private var imgSiguiente: ImageView? = null
    var tvVendedor : TextView? = null
    private var tvSupervisor : TextView? = null
    private var tvGerentes : TextView? = null
    var contMenu: DrawerLayout? = null
    var barraMenu: NavigationView? = null
    var context : Context? = null
    private var spinnerAdapter : ArrayAdapter<String>? = null
    private var valoresSpinner: ArrayList<HashMap<String, String>> = ArrayList()
    private var parametros : Array<String> = arrayOf()
    lateinit var vistas : IntArray
    lateinit var vistasCabecera : IntArray
    lateinit var valores: Array<String>
    lateinit var subVistas : IntArray
    lateinit var subValores: Array<String>
    lateinit var subVistas2 : IntArray
    lateinit var subValores2: Array<String>
    lateinit var listaVendedores: ArrayList<HashMap<String, String>>
    private lateinit var listaSupervisores: ArrayList<HashMap<String, String>>
    private lateinit var listaGerentes: ArrayList<HashMap<String, String>>
    var posVend : Int = 0
    private var posSup : Int = 0
    private var posGer : Int = 0

    //FUNCIONES DE BD
    fun dato(cursor: Cursor, index: String): String {
        return try {
            cursor.getString(cursor.getColumnIndex(index))
        } catch (e: java.lang.Exception){
            ""
        }
    }
    fun datoEntero(cursor: Cursor, index: String): Int{
        return cursor.getInt(cursor.getColumnIndex(index))
    }
    fun datoDecimal(cursor: Cursor, index: String): Double{
        return cursor.getDouble(cursor.getColumnIndex(index))
    }
    fun datoPorcentaje(cursor: Cursor, totalS: String, valorS: String):Double{

        val total: Double =
            cursor.getString(cursor.getColumnIndex(totalS))
                .replace(".", "")
                .replace(",", ".")
                .replace("%", ""
        ).toDouble()
        val valor: Double =
            cursor.getString(cursor.getColumnIndex(valorS)).replace(".", "").replace(",", ".").replace(
            "%",
            ""
        ).toDouble()

        return (valor*100)/total
    }
    @SuppressLint("Recycle")
    fun consultar(sql: String): Cursor{
        return try {
            cursor = MainActivity.bd!!.rawQuery(sql, null)
            cursor.moveToFirst()
            cursor
        } catch (e: Exception){
            e.message
            cursor
        }
    }
    fun cargarLista(lista: ArrayList<HashMap<String, String>>, cursor: Cursor){
        for (i in 0 until cursor.count){
            val datos : HashMap<String, String> = HashMap()
            for(j in 0 until cursor.columnCount){
                datos[cursor.getColumnName(j)] = dato(cursor, cursor.getColumnName(j))
            }
            lista.add(datos)
            cursor.moveToNext()
        }
    }
    fun ejecutar(sql: String, context: Context): Boolean {
        return try {
            MainActivity.bd!!.execSQL(sql)
            true
        } catch (e: Exception) {
            val dialogo:AlertDialog.Builder = AlertDialog.Builder(context)
            dialogo.setMessage(e.message)
            dialogo.setTitle("ERROR")
            dialogo.show()
    //            Toast.makeText(context,"Error al ejecutar " + sql,Toast.LENGTH_LONG).show()
            false
        }
    }
    fun insertar(tabla: String, valores: ContentValues){
        try {
            MainActivity.bd!!.insert(tabla, null, valores)
//            mensaje("Correcto","Insertado correctamente")
        } catch (e: Exception){
            mensaje("Error", e.message.toString())
        }
    }
    fun insertar(tabla: String, campos: String, valores: ContentValues){
        try {
            MainActivity.bd!!.insert(tabla, campos, valores)
//            mensaje("Correcto","Insertado correctamente")
        } catch (e: Exception){
            mensaje("Error", e.message.toString())
        }
    }
    fun actualizar(tabla: String, valores: ContentValues, where: String){
        try {
            MainActivity.bd!!.update(tabla, valores, where, null)
            mensaje("Correcto", "Acualizado correctamente")
        } catch (e: Exception){
            mensaje("Error", e.message.toString())
        }
    }
    @SuppressLint("SetTextI18n")
    fun buscar(tabla: String) : Cursor{
        val sql: String
        if (spBuscar!!.selectedItemPosition != 0){
            sql = "SELECT * FROM " + tabla +
                    " WHERE " + valoresSpinner[spBuscar!!.selectedItemPosition][spBuscar!!.selectedItem] +
                    "  LIKE '%"  + etBuscar!!.text.toString() + "%' "
            tvVendedor!!.text = "Todos"
            return consultar(sql)
        }
        sql = "SELECT * FROM " + tabla +
              " WHERE " + valoresSpinner[spBuscar!!.selectedItemPosition][spBuscar!!.selectedItem] +
              "  LIKE '%"  + etBuscar!!.text.toString() + "%' "
        tvVendedor!!.text = "Todos"
        return consultar(sql)
    }
    @SuppressLint("SetTextI18n")
    fun buscar(tabla: String, campos: String?, groupBy: String?, orderBy: String?) : Cursor{
        var sql: String
        if (spBuscar!!.selectedItemPosition != 0){
            sql = "SELECT " + campos + " FROM " + tabla +
                    " WHERE " + valoresSpinner[spBuscar!!.selectedItemPosition][spBuscar!!.selectedItem]!!.split(",")[0].toUpperCase(
                Locale.ROOT) +
                    "  LIKE '%"  + etBuscar!!.text.toString() + "%' "
            if (valoresSpinner[spBuscar!!.selectedItemPosition][spBuscar!!.selectedItem]!!.split(",").size>1){
                for(i in 1 until valoresSpinner[spBuscar!!.selectedItemPosition][spBuscar!!.selectedItem]!!.split(
                    ","
                ).size){
                    sql = "$sql OR " + valoresSpinner[spBuscar!!.selectedItemPosition][spBuscar!!.selectedItem]!!.split(
                        ","
                    )[i].toUpperCase(Locale.ROOT) +
                            " LIKE '%" + etBuscar!!.text.toString() + "%' "
                }
            }
        } else {
            sql = "SELECT " + campos + " FROM " + tabla +
                    " WHERE " + valoresSpinner[spBuscar!!.selectedItemPosition][spBuscar!!.selectedItem] +
                    "  LIKE '%"  + etBuscar!!.text.toString() + "%' "
        }
        if (!groupBy.equals("")){ sql = "$sql GROUP BY $groupBy" }
        if (!orderBy.equals("")){ sql = "$sql ORDER BY $orderBy" }
        tvVendedor!!.text = "Todos"
        return consultar(sql)
    }
    fun buscar(tabla: String, campos: String?, groupBy: String?, orderBy: String?, where: String) : Cursor{
        var sql: String
        if (spBuscar!!.selectedItemPosition != 0){
            sql = "SELECT " + campos + " FROM " + tabla +
                    " WHERE " + valoresSpinner[spBuscar!!.selectedItemPosition][spBuscar!!.selectedItem]!!.split(",")[0].toUpperCase(
                Locale.ROOT
            ) +
                    "  LIKE '%"  + etBuscar!!.text.toString() + "%' "
            if (valoresSpinner[spBuscar!!.selectedItemPosition][spBuscar!!.selectedItem]!!.split(",").size>1){
                for(i in 1 until valoresSpinner[spBuscar!!.selectedItemPosition][spBuscar!!.selectedItem]!!.split(",").size){
                    sql = "$sql OR " + valoresSpinner[spBuscar!!.selectedItemPosition][spBuscar!!.selectedItem]!!.split(",")[i].toUpperCase(
                        Locale.ROOT
                    ) +
                            " LIKE '%" + etBuscar!!.text.toString() + "%' "
                }
            }
        } else {
            sql = "SELECT " + campos + " FROM " + tabla +
                    " WHERE " + valoresSpinner[spBuscar!!.selectedItemPosition][spBuscar!!.selectedItem].toString().split(",")[0] +
                    "  LIKE '%"  + etBuscar!!.text.toString() + "%' "
        }
        sql += where
        if (!groupBy.equals("")){ sql = "$sql GROUP BY $groupBy" }
        if (!orderBy.equals("")){ sql = "$sql ORDER BY $orderBy" }
//        if (tvVendedor!! != null){
//            tvVendedor!!.setText("Todos")
//        }
        return consultar(sql)
    }
    fun ultPedidoVenta(codVendedor: String):Int{
        val sql : String = ("SELECT NUMERO MAXIMO FROM svm_vendedor_pedido  WHERE COD_VENDEDOR = '${codVendedor}'")
        return datoEntero(consultar(sql), "MAXIMO")
    }
    fun cargarDatos(cursor: Cursor):ArrayList<HashMap<String, String>>{
        val lista = ArrayList<HashMap<String, String>>()
        for (i in 0 until cursor.count){
            val dato : HashMap<String, String> = HashMap()
            for (j in 0 until cursor.columnCount){
                try {
                    dato[cursor.getColumnName(j)] = dato(cursor, cursor.getColumnName(j))
                } catch (e: Exception){}
            }
            lista.add(dato)
            cursor.moveToNext()
        }
        return lista
    }

    //FUNCIONES DE FORMATO DE NUMEROS
    fun entero(entero: String):String{
        if (entero.trim() == ""){
            return "0"
        }
        return formatoNumeroEntero.format(
            entero.replace(".", "").replace("null", "0").trim().toInt()
        )
    }
    fun entero(entero: Int):String{
        return formatoNumeroEntero.format(entero)
    }
    fun aEntero(numero: String):Int{
        return numero.replace(".", "").toInt()
    }
    fun enteroCliente(entero: String):String{
        if (entero.trim() == ""){
            return "0"
        }
        if (entero.trim().indexOf(".")>-1){
            return "0"
        }
        return formatoNumeroEntero.format(
            entero.replace(".", "").replace("null", "0").trim().toInt()
        )
    }
    fun enteroLargo(entero: String):String{
        if (entero.trim() == ""){
            return "0"
        }
        if (entero.trim().indexOf(".")>-1){
            return "0"
        }
        return formatoNumeroEntero.format(entero.replace(".","").replace("null","0").trim().toInt())
    }
    fun decimal(decimal: String):String{
        if (decimal.trim() == "" || decimal.trim() == "null"){
            return "0.0"
        }
        if (decimal.indexOf(",")>-1){
            return formatoNumeroDecimal.format(
                decimal.replace(".", "")
                    .replace(",", ".")
                    .replace("%", "").trim().toDouble()
            ).toString()
        }
        return formatoNumeroDecimal.format(
            decimal.replace(",", ".")
                .replace("%", "")
                .replace("null", "")
                .trim().toDouble()
        ).toString()
    }
    fun decimal(decimal: String, cantidad: Int):String{
        formatoGenerico.minimumFractionDigits = cantidad
        formatoGenerico.maximumFractionDigits = cantidad
        if (decimal.trim() == "" || decimal.trim() == "null"){
            return "0.0"
        }
        if (decimal.indexOf(",")>-1){
            return formatoGenerico.format(
                decimal.replace(".", "")
                    .replace(",", ".")
                    .replace("%", "").trim().toDouble()
            ).toString()
        }
        return formatoGenerico.format(
            decimal.replace(",", ".")
                .replace("%", "")
                .replace("null", "")
                .trim().toDouble()
        ).toString()
    }
    fun decimal(decimal: Double):String{
        return formatoNumeroDecimal.format(decimal)
    }
    fun decimalPunto(decimal: String):String{
        return decimal(decimal).replace(".", "").replace(",", ".")
    }
    fun numero(decimales: String, numero: String):String{
        formatoGenerico.minimumFractionDigits = decimales.toInt()
        formatoGenerico.maximumFractionDigits = decimales.toInt()
        return formatoGenerico.format(numero.replace(",", ".").toDouble())
    }
    fun porcentaje(decimal: String):String{
        return decimal(decimal) + "%"
    }
    fun porcentaje(decimal: Double):String{
        return formatoNumeroDecimal.format(decimal) + "%"
    }
    fun inicializaContadores(){
        posicionCabecera = 0
        posicionDetalle = 0
        posicionGenerico = 0
    }
    fun fechaHora(fecha: String): Date {
        val hourFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        return hourFormat.parse(fecha)
    }
    fun fecha(fecha: String): Date {
        val hourFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy")
        return hourFormat.parse(fecha)
    }
    fun fechaF(fecha: String): Date {
        val hourFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy")
        return hourFormat.parse(hourFormat.format(fecha))
    }
    fun tiempoTranscurrido(fecha1: String, fecha2: String):Int{
        var diferencia : Long = (fechaHora(fecha2).time/60000) - (fechaHora(fecha1).time / 60000)
        if (diferencia<0){
            diferencia *= (-1)
        }
        return diferencia.toInt()
    }
    fun convertirFechatoSQLFormat(fechas: String): String? {
        var fecha = fechas
        var res = ""
        var dia = ""
        var mes = ""
        var anho = ""
        fecha = fecha.replace("/", "")
        fecha = fecha.replace(" ", "")
        for (i in fecha.indices) {
            if (i < 2) {
                dia += fecha[i]
            } else {
                if (i < 4) {
                    mes += fecha[i]
                } else {
                    anho += fecha[i]
                }
            }
        }
        res = "$anho-$mes-$dia"
        return res
    }

    //FECHAS
    fun getDiaDeLaSemana(str_fecha: String?): String? {
        val formatoDelTexto = SimpleDateFormat("dd/MM/yyyy")
        val fecha: Date?
        try {
            fecha = formatoDelTexto.parse(str_fecha)
            val cal: Calendar = GregorianCalendar.getInstance()
            cal.time = fecha
            return getDia(cal.get(Calendar.DAY_OF_WEEK))
        } catch (ex: java.lang.Exception) {
        }
        return "Lunes"
    }
    fun getDia(dia: Int):String{
        return when(dia){
            1 -> "Domingo"
            2 -> "Lunes"
            3 -> "Martes"
            4 -> "Miercoles"
            5 -> "Jueves"
            6 -> "Viernes"
            7 -> "Sabado"
            else -> "No corresponde"
        }
    }
    fun getDiaDeLaSemana(): String {
        val cal = GregorianCalendar.getInstance()
        return getDia(cal[Calendar.DAY_OF_WEEK])
    }
    fun getMes():Int{
        return getFechaActual().split("/")[1].toInt()
    }
    fun getMes(mes: String):String{
        return when (mes){
            "01" -> "Enero"
            "02" -> "Febrero"
            "03" -> "Marzo"
            "04" -> "Abril"
            "05" -> "Mayo"
            "06" -> "Junio"
            "07" -> "Julio"
            "08" -> "Agosto"
            "09" -> "Setiembre"
            "10" -> "Octubre"
            "11" -> "Noviembre"
            "12" -> "Diciembre"
            else -> "Valor no corresponde"
        }
    }
    fun getMes(mes: Int):String{
        return when (mes){
            1 -> "Enero"
            2 -> "Febrero"
            3 -> "Marzo"
            4 -> "Abril"
            5 -> "Mayo"
            6 -> "Junio"
            7 -> "Julio"
            8 -> "Agosto"
            9 -> "Setiembre"
            10 -> "Octubre"
            11 -> "Noviembre"
            12 -> "Diciembre"
            else -> "Valor no corresponde"
        }
    }
    fun getHoraActual(): String? {
        val calendario = Calendar.getInstance()
        val hora: Int
        val minutos: Int
        val segundos: Int
        hora = calendario[Calendar.HOUR_OF_DAY]
        minutos = calendario[Calendar.MINUTE]
        segundos = calendario[Calendar.SECOND]

        // GUARDAR LA HORA
        var _hora = ""
        _hora += if (hora < 10) {
            "0$hora:"
        } else {
            "$hora:"
        }
        _hora += if (minutos < 10) {
            "0$minutos:"
        } else {
            "$minutos:"
        }
        if (segundos < 10) {
            _hora += "0$segundos"
        } else {
            _hora += segundos
        }
        return _hora
    }
    fun getFechaActual():String{
        val dfDate = SimpleDateFormat("dd/MM/yyyy")
        val cal = Calendar.getInstance()
        return dfDate.format(cal.getTime()) + ""
    }
    fun getFechaHoraActual():String{
        return getFechaActual() + " " + getHoraActual()
    }
    fun fechaRuteo(context: Context):Boolean{
        var d: Date? = null
        var d1: Date? = null
        val cal = Calendar.getInstance()
        val sql : String = ("SELECT numero MAXIMO, ind_palm, ultima_sincro, RANGO, TIPO_promotor, MIN_FOTO, MAX_FOTO, IND_FOTO, FEC_CARGA_RUTEO, MAX_DESC "
                + ",version, MAX_DESC_VAR, PER_VENDER, INT_MARCACION  from svm_vendedor_pedido_venta  where COD_promotor ="
                + "'" + usuario["LOGIN"] + "'")
        val cursor : Cursor = consultar(sql)
        var fecha = ""
        if (cursor.count>0){
            fecha = try {
                dato(cursor, "FEC_CARGA_RUTEO").trim()
            } catch (e: java.lang.Exception){
                ""
            }
        }
        if (fecha == ""){
            mensaje(context, "Atención!", "No se ha habilitado este dia para enviar ruteo.")
            return false
        }
        try {
            d = fecha(fecha)
            d1 = fechaF(cal.time.toString())
        } catch (e: java.text.ParseException) {
            e.printStackTrace();
        }

        val diffInDays : Int = ((d1!!.time - d!!.time) / (1000 * 60 * 60 * 24)).toInt()

        if (diffInDays != 0) {
            mensaje(context, "Atención!", "No se ha habilitado este dia para enviar ruteo.")
            return false
        }
        return true
    }


    //MENU
    fun mostrarMenu(){
        if (contMenu!!.isDrawerOpen(GravityCompat.START)) {
            contMenu!!.closeDrawer(GravityCompat.START)
        } else {
            contMenu!!.openDrawer(GravityCompat.START)
        }
    }
    fun cargarTitulo(icon: Int, titulo: String){
        imgTitulo!!.setImageResource(icon)
        tvTitulo!!.text = titulo
    }
    @SuppressLint("SetTextI18n")
    fun actualizaVendedor(context: Context){
        if (posVend == listaVendedores.size){
            Toast.makeText(context, "Es el ultimo registro", Toast.LENGTH_SHORT).show()
            posVend--
        } else {
            if (posVend == -1){
                Toast.makeText(context, "Es el primer registro", Toast.LENGTH_SHORT).show()
                posVend++
            } else {
                tvVendedor!!.text = listaVendedores[posVend]["codigo"] + "-" +
                        listaVendedores[posVend]["descripcion"]
                inicializaContadores()
            }
        }
    }
    @SuppressLint("SetTextI18n")
    fun actualizaSupervisor(context: Context){
        if (posSup == listaSupervisores.size){
            Toast.makeText(context, "Es el ultimo registro", Toast.LENGTH_SHORT).show()
            posSup--
        } else {
            if (posSup == -1){
                Toast.makeText(context, "Es el primer registro", Toast.LENGTH_SHORT).show()
                posSup++
            } else {
                tvSupervisor!!.text = listaSupervisores[posSup]["codigo"] + "-" +
                        listaSupervisores[posSup]["descripcion"]
                inicializaContadores()
            }
        }
    }

    //DIALOGOS
    fun mensaje(context: Context, titulo: String, mensaje: String){
        val dialogo : AlertDialog.Builder = AlertDialog.Builder(context)
        dialogo.setTitle(titulo)
        dialogo.setMessage(mensaje)
        dialogo.setPositiveButton("OK", null)
        dialogo.show()
    }
    fun mensaje(titulo: String, mensaje: String){
        val dialogo : AlertDialog.Builder = AlertDialog.Builder(context)
        dialogo.setTitle(titulo)
        dialogo.setMessage(mensaje)
        dialogo.show()
    }
    fun getIntervaloMarcacion(): Int {
        val sql : String =
            ("SELECT numero MAXIMO, ind_palm, ultima_sincro, RANGO, TIPO_promotor, MIN_FOTO, MAX_FOTO, IND_FOTO, FEC_CARGA_RUTEO, MAX_DESC "
                    + ",version, MAX_DESC_VAR, PER_VENDER, INT_MARCACION  from svm_vendedor_pedido  where COD_promotor ="
                    + "'" + usuario["LOGIN"] + "'")
        return if (consultar(sql).count>0){
            datoEntero(consultar(sql), "INT_MARCACION")
        } else {
            0
        }
    }
    fun getIndPalm(codVendedor: String): String {
        val sql = "SELECT IND_PALM from svm_vendedor_pedido  WHERE COD_VENDEDOR = '${codVendedor}'"
        return if (consultar(sql).count>0){
            dato(consultar(sql), "IND_PALM")
        } else {
            "N"
        }
    }
    fun dialogoEntrada(et: EditText, context: Context){
        val dialogo : AlertDialog.Builder = AlertDialog.Builder(context)
        val entrada = EditText(context)
        dialogo.setTitle(et.hint)
        entrada.inputType = InputType.TYPE_CLASS_TEXT
        dialogo.setView(entrada)
        entrada.text = et.text
        dialogo.setPositiveButton(
            "OK"
        ) { _: DialogInterface, _: Int ->
            et.text = entrada.text
        }
        dialogo.setCancelable(false)
        dialogo.show()
    }
    fun dialogoEntrada(et: EditText, context: Context, etAccion: EditText, accion: String){
        val dialogo : AlertDialog.Builder = AlertDialog.Builder(context)
        val entrada = EditText(context)
        dialogo.setTitle(et.hint)
        entrada.inputType = InputType.TYPE_CLASS_TEXT
        dialogo.setView(entrada)
        entrada.text = et.text
        dialogo.setPositiveButton(
            "OK"
        ) { _: DialogInterface, i: Int ->
            et.text = entrada.text
            etAccion.setText(accion)
        }
        dialogo.setCancelable(false)
        dialogo.show()
    }
    //DATOS
    fun toast(context: Context, mensaje: String){
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }
}