package apolo.tienda.tienda.presentation.inventory.load

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import apolo.tienda.tienda.R
import apolo.tienda.tienda.data.remote.repository.InventoryRepositoryImpl
import apolo.tienda.tienda.data.remote.request.LoadInventoryRequest
import apolo.tienda.tienda.data.remote.response.ProductoResponse
import apolo.tienda.tienda.databinding.ActivityInventoryLoadBinding
import apolo.tienda.tienda.di.NetworkModule
import apolo.tienda.tienda.domain.model.Inventory
import apolo.tienda.tienda.presentation.state.UiState
import apolo.tienda.tienda.utils.*

class InventoryLoadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryLoadBinding
    private val viewModel: InventoryLoadViewModel by viewModels()
    private lateinit var inventario: Inventory
    private lateinit var loadingDialog: LoadingDialog
    private var unidadesMap: Map<String, String> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryLoadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDependencies()
        obtenerDatosIntent()
        setupUI()
        setupObservers()
        setupGuardarButton()

        loadingDialog = LoadingDialog(this)
    }

    private fun setupDependencies() {
        val retrofit = NetworkModule.provideRetrofit(
            baseUrl = getBackendUrl(),
            preferencesHelper = PreferencesHelper.getInstance()
        )
        val api = NetworkModule.provideInventoryApi(retrofit)
        val repository = InventoryRepositoryImpl(api)
        viewModel.setRepository(repository)
    }

    private fun obtenerDatosIntent() {
        val nroRegistro = intent.getStringExtra("NRO_REGISTRO")
        val codEmpresa = intent.getStringExtra("COD_EMPRESA")
        val codSucursal = intent.getStringExtra("COD_SUCURSAL")

        if (nroRegistro == null || codEmpresa == null || codSucursal == null) {
            showToast("Datos del inventario inválidos")
            finish()
            return
        }

        inventario = Inventory(
            nroRegistro = nroRegistro.toIntOrNull() ?: 0,
            codEmpresa = codEmpresa,
            codSucursal = codSucursal,
            fecha = "",
            descripcion = "",
            descSucursal = "",
            descEmpresa = ""
        )

        binding.tvNroInventario.text = "Inventario Nro.: $nroRegistro"
    }

    private fun setupUI() {
        binding.etCodigoBarra.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val codigo = binding.etCodigoBarra.text.toString()
                if (codigo.isNotEmpty()) {
                    loadingDialog.show()
                    viewModel.getProducto(inventario.codEmpresa, "", codigo)
                }
                true
            } else false
        }


        binding.etCodigoBarra.setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_ENTER && event.action == android.view.KeyEvent.ACTION_UP) {
                val codigo = binding.etCodigoBarra.text.toString()
                if (codigo.isNotEmpty()) {
                    loadingDialog.show()
                    viewModel.getProducto(inventario.codEmpresa, "", codigo)
                }
                true
            } else {
                false
            }
        }

        binding.etCodigoArticulo.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val codigo = binding.etCodigoArticulo.text.toString()
                if (codigo.isNotEmpty()) {
                    loadingDialog.show()
                    viewModel.getProducto(inventario.codEmpresa, codigo, "")
                }
                true
            } else false
        }

        binding.etCodigoArticulo.setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_ENTER && event.action == android.view.KeyEvent.ACTION_UP) {
                val codigo = binding.etCodigoArticulo.text.toString()
                if (codigo.isNotEmpty()) {
                    loadingDialog.show()
                    viewModel.getProducto(inventario.codEmpresa, codigo, "")
                }
                true
            } else {
                false
            }
        }

        binding.spUnidad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val codUnidad = viewModel.unidadesState.value?.get(position)?.cod_unidad_rel
                codUnidad?.let {
                    val codigoBarra = unidadesMap[it]
                    binding.etCodigoBarra.setText(codigoBarra ?: "")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupObservers() {
        viewModel.productosState.observe(this) { productos ->
            if (productos.size == 1) {
                val producto = productos.first()
                binding.etCodigoArticulo.setText(producto.cod_articulo)
                binding.tvDescripcion.text = producto.desc_articulo
                viewModel.getUnidadesMedida(inventario.codEmpresa, producto.cod_articulo)
            } else if (productos.size > 1) {
                mostrarDialogoSeleccion(productos)
                limpiarCamposProducto()
            } else {
                showToast("Producto no encontrado.")
                limpiarCamposProducto()
            }
            loadingDialog.hide()
        }

        viewModel.unidadesState.observe(this) { unidades ->
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                unidades.map { it.referencia }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spUnidad.adapter = adapter

            unidadesMap = unidades.associate { it.cod_unidad_rel to it.cod_barra }

            loadingDialog.hide()
            binding.etCantidad.requestFocus()
        }

        viewModel.errorState.observe(this) {
            showToast(it)
            loadingDialog.hide()
        }

        viewModel.saveItemState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> loadingDialog.show()
                is UiState.Success -> {
                    loadingDialog.hide()
                    showToast("Producto guardado correctamente")
                    limpiarCamposProducto()
                    binding.etCodigoBarra.requestFocus()
                }
                is UiState.Error -> {
                    loadingDialog.hide()
                    showToast("Error: ${state.message}")
                }
            }
        }
    }

    private fun setupGuardarButton() {
        binding.btnGuardar.setOnClickListener {
            guardarProducto()
        }
    }

    private fun guardarProducto() {
        val codArticulo = binding.etCodigoArticulo.text.toString()
        val codUnidad = viewModel.unidadesState.value?.get(binding.spUnidad.selectedItemPosition)?.cod_unidad_rel
        val cantidadText = binding.etCantidad.text.toString()
        val codBarra = binding.etCodigoBarra.text.toString()

        if (codArticulo.isEmpty()) {
            showToast("Debe ingresar un código de artículo")
            return
        }

        if (codUnidad.isNullOrEmpty()) {
            showToast("Debe seleccionar una unidad de medida")
            return
        }

        val cantidad = cantidadText.toDoubleOrNull()
        if (cantidad == null || cantidad <= 0) {
            showToast("La cantidad debe ser mayor a 0")
            return
        }

        val request = LoadInventoryRequest(
            nro_registro = inventario.nroRegistro.toString(),
            cod_barra = codBarra,
            cod_empresa = inventario.codEmpresa,
            cod_sucursal = inventario.codSucursal,
            cod_articulo = codArticulo,
            cod_unidad_rel = codUnidad,
            cantidad = cantidad
        )

        viewModel.guardarProducto(request)
    }

    private fun limpiarCamposProducto() {
        binding.etCodigoArticulo.setText("")
        binding.etCodigoBarra.setText("")
        binding.tvDescripcion.text = "(sin descripción aún)"
        binding.etCantidad.setText("")
        binding.spUnidad.adapter = null
        unidadesMap = emptyMap()
    }

    private fun mostrarDialogoSeleccion(productos: List<ProductoResponse>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_product_list, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rvProductos)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        alertDialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val adapter = ProductSelectAdapter(productos) { producto ->
            binding.etCodigoArticulo.setText(producto.cod_articulo)
            binding.tvDescripcion.text = producto.desc_articulo
            viewModel.getUnidadesMedida(inventario.codEmpresa, producto.cod_articulo)
            alertDialog.dismiss()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        alertDialog.show()

        // Ajustar el tamaño del diálogo al ancho total
        alertDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
