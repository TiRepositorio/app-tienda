package apolo.tienda.tienda

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import apolo.tienda.tienda.conexion.Usuario
import apolo.tienda.tienda.utilidades.Adapter
import apolo.tienda.tienda.utilidades.FuncionesUtiles
import kotlinx.android.synthetic.main.activity_inventario.*

class Inventario : AppCompatActivity() {
    lateinit var producto : ArrayList<HashMap<String,String>>
    lateinit var adapter : Adapter.Generico
    val campos = " COD_BARRA, DESCRIPCION, CANTIDAD, ID, COD_ARTICULO "
    val tabla = "ST_INVENTARIO_TIENDA"
    val where = "where trunc(fecha_inventario) = trunc(sysdate) AND COD_USUARIO = '${Usuario.usuario}'"
    val group = ""
    val order = " order by ID desc "
    lateinit var funcion : FuncionesUtiles
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario)

        inicializarVariables()
    }

    private fun sqlArticulo(codArticulo:String) : String{
        return "select a.COD_ARTICULO, a.COD_BARRA_ART, a.DESCRIPCION, b.COD_UNIDAD_REL, b.REFERENCIA, b.MEN_UN_VTA from st_articulos a, st_relaciones b " +
                "WHERE (trim(a.COD_ARTICULO) = '${codArticulo.trim()}' OR trim(a.COD_BARRA_ART) = '${codArticulo.trim()}')" +
                "  AND a.COD_EMPRESA = b.COD_EMPRESA " +
                "  AND a.COD_ARTICULO = b.COD_ARTICULO" +
                "  AND a.COD_EMPRESA  = '2'"
    }

    fun cargar(){
        MainActivity.listaProducto = ArrayList()
//        funcion.cargarLista(MainActivity.listaProducto,funcion.consultar(sqlArticulo(etCodBarra.text.toString().trim())))
        MainActivity.consultar(sqlArticulo(etCodBarra.text.toString()),etAccionInv)
        etAccionInv.setText("asignarResultset")
    }

    private fun inicializarVariables(){
        funcion = FuncionesUtiles(this)
        etCodBarra.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().indexOf("\n")>-1){
                    MainActivity.consultar(sqlArticulo(s.toString().replace("\n","")),etAccionInv)
//                    cargar()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

        })
        etCodBarra.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus){
                MainActivity.consultar(sqlArticulo(etCodBarra.text.toString().replace("\n","")),etAccionInv)
//                cargar()
            }
        }
        etAccionInv.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() == "asignarResultset"){
                    producto = MainActivity.listaProducto
                    if (producto.size>0){
                        tvDescArticulo.text = producto[0]["DESCRIPCION"]
                        tvUnidadMedida.text    = producto[0]["COD_UNIDAD_REL"] + "-" + producto[0]["REFERENCIA"]
                    }
                    if (!cbCantidad.isChecked){
                        MainActivity.verExistencia("ST_INVENTARIO_TIENDA"," where cod_usuario = '${Usuario.usuario}' " +
                                " and trunc(fecha_inventario) = trunc(sysdate) " +
                                " and (cod_articulo = '${etCodBarra.text}' or cod_barra = '${etCodBarra.text}') ", etAccionInv,"agregar")
                    }
                }
                if (s.toString() == "limpiar"){
                    limpiar()
                    MainActivity.consultaGen(campos,tabla,where,group,order,etAccionInv,"actualizar")
                }
                if (s.toString() == "actualizar"){
                    Adapter.posicion = 0
                    adapter = Adapter.Generico(this@Inventario,
                        MainActivity.lista,
                        R.layout.inv_lista_registros,
                        intArrayOf(R.id.tv1,R.id.tv2,R.id.tv3),
                        arrayOf("COD_BARRA","DESCRIPCION","CANTIDAD"))
                    lvRegistros.adapter = adapter
                    lvRegistros.setOnItemClickListener { _, _, position, _ ->
                        Adapter.posicion = position
                        lvRegistros.invalidateViews()
                    }
                    etCodBarra.requestFocus()
                }
                if (s.toString() == "agregar"){
                    val sql = "update st_inventario_tienda set cantidad = cantidad + ${etCantidad.text} " +
                            " where cod_usuario = '${Usuario.usuario}' and trunc(fecha_inventario) = trunc(sysdate) " +
                            "   and (cod_articulo = '${etCodBarra.text}' or cod_barra = '${etCodBarra.text}') "
                    MainActivity.ejecutarSQL(sql,etAccionInv,"limpiar")
                }
                if (s.toString() == "noExiste"){
                    MainActivity.insertar("ST_INVENTARIO_TIENDA",
                        "ID,COD_USUARIO,COD_ARTICULO,COD_BARRA,CANTIDAD,COD_UNIDAD_REL,DESCRIPCION",
                        "(SELECT NVL(MAX(ID),0)+1 FROM ST_INVENTARIO_TIENDA)," +
                                "'${Usuario.usuario}','${producto[0]["COD_ARTICULO"]}'," +
                                "'${producto[0]["COD_BARRA_ART"]}','${etCantidad.text}'," +
                                "'${producto[0]["COD_UNIDAD_REL"]}','${tvDescArticulo.text}'",
                        etAccionInv)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

        })
        btConfirmar.setOnClickListener {
            if (cbCantidad.isChecked){
                if (etCantidad.text.toString().trim() == "" || etCantidad.text.toString().toDouble() == 0.0){
                    Toast.makeText(this,"Cantidad inválida",Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                MainActivity.verExistencia("ST_INVENTARIO_TIENDA"," where cod_usuario = '${Usuario.usuario}' " +
                        " and trunc(fecha_inventario) = trunc(sysdate) " +
                        " and (cod_articulo = '${etCodBarra.text}' or cod_barra = '${etCodBarra.text}') ", etAccionInv,"agregar")
//                MainActivity.insertar("ST_INVENTARIO_TIENDA",
//                    "ID,COD_USUARIO,COD_ARTICULO,COD_BARRA,CANTIDAD,COD_UNIDAD_REL,DESCRIPCION",
//                    "(SELECT NVL(MAX(ID),0)+1 FROM ST_INVENTARIO_TIENDA)," +
//                            "'${Usuario.usuario}','${producto[0]["COD_ARTICULO"]}'," +
//                            "'${producto[0]["COD_BARRA_ART"]}','${etCantidad.text}'," +
//                            "'${producto[0]["COD_UNIDAD_REL"]}','${tvDescArticulo.text}'",
//                    etAccionInv)
            } else {
                MainActivity.verExistencia("ST_INVENTARIO_TIENDA"," where cod_usuario = '${Usuario.usuario}' " +
                        " and trunc(fecha_inventario) = trunc(sysdate) " +
                        " and (cod_articulo = '${etCodBarra.text}' or cod_barra = '${etCodBarra.text}' ", etAccionInv,"agregar")
            }
        }
        btCancelar.setOnClickListener { limpiar() }
        cbCantidad.setOnCheckedChangeListener { _, isChecked ->
            etCantidad.isEnabled = isChecked
            if (!isChecked){
                etCantidad.setText("1")
            }
        }
        cbCantidad.isChecked = true
        ibtEliminar.setOnClickListener { eliminar() }
        ibtEliminar.isEnabled = false
    }

    fun limpiar(){
        etCodBarra.setText("")
        etCantidad.setText(if (cbCantidad.isChecked){""} else {"1"})
        etAccionInv.setText("")
        tvDescArticulo.text = ""
        tvUnidadMedida.text = ""
        producto = ArrayList()
        MainActivity.listaProducto = ArrayList()
        ibtEliminar.isEnabled = true
    }

    private fun eliminar(){
        if (MainActivity.lista.size == 0){
            return
        }
        if (Adapter.posicion > MainActivity.lista.size-1){
            return
        }
        val sql = "delete from st_inventario_tienda " +
                " where trunc(fecha_inventario) = trunc(sysdate) " +
                "   and cod_usuario  = '${Usuario.usuario}' " +
                "   and cod_articulo = '${MainActivity.lista[Adapter.posicion]["COD_ARTICULO"]}'"
        MainActivity.ejecutarSQL(sql,etAccionInv,"limpiar")
    }



}