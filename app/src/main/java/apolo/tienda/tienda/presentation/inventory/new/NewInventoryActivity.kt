package apolo.tienda.tienda.presentation.inventory.new

import android.R
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import apolo.tienda.tienda.data.remote.repository.InventoryRepositoryImpl
import apolo.tienda.tienda.data.remote.request.NewInventoryRequest
import apolo.tienda.tienda.data.remote.response.ListInventoryResponse
import apolo.tienda.tienda.data.remote.response.NewInventoryResponse
import apolo.tienda.tienda.databinding.ActivityNewInventoryBinding
import apolo.tienda.tienda.di.NetworkModule
import apolo.tienda.tienda.presentation.inventory.load.InventoryLoadActivity
import apolo.tienda.tienda.presentation.inventory.main.InventoryMainActivity
import apolo.tienda.tienda.presentation.state.UiState
import apolo.tienda.tienda.utils.*
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class NewInventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewInventoryBinding
    private val viewModel: NewInventoryViewModel by viewModels()
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDependencies()
        setupUi()
        setupObservers()

        viewModel.getEmpresas()
    }

    // ---------- INICIALIZACIÓN Y DEPENDENCIAS ----------
    private fun setupDependencies() {
        val retrofit = NetworkModule.provideRetrofit(getBackendUrl(), PreferencesHelper.getInstance())
        val repository = InventoryRepositoryImpl(NetworkModule.provideInventoryApi(retrofit))
        viewModel.setRepository(repository)
        loadingDialog = LoadingDialog(this)
    }

    private fun setupUi() {
        setupEmpresaSpinnerListener()
        setupDatePicker()
        setupGuardarListener()
    }

    // ---------- OBSERVERS ----------
    private fun setupObservers() {
        viewModel.empresasState.observe(this) { empresas ->
            val empresaAdapter = ArrayAdapter(this, R.layout.simple_list_item_1, empresas.map { it.desc_empresa })
            (binding.spEmpresa as AutoCompleteTextView).setAdapter(empresaAdapter)
            binding.spEmpresa.setOnClickListener {
                binding.spEmpresa.showDropDown()
            }

            // Obtener la lista de empresas del estado actual
            val empresas = viewModel.empresasState.value ?: emptyList()
            // Obtener el código de empresa desde las preferencias
            val defaultCodEmpresa = PreferencesHelper.getInstance().getCodEmpresa()
            // Buscar el índice de la empresa predeterminada
            val index = empresas.indexOfFirst { it.cod_empresa == defaultCodEmpresa }
            if (index != -1) {
                val empresaNombre = empresas[index].desc_empresa
                binding.spEmpresa.setText(empresaNombre, false)
                viewModel.getSucursales(empresas[index].cod_empresa)
            }
        }

        viewModel.sucursalesState.observe(this) { sucursales ->

            val sucursalAdapter = ArrayAdapter(this, R.layout.simple_list_item_1, sucursales.map { it.desc_sucursal })
            (binding.spSucursal as AutoCompleteTextView).setAdapter(sucursalAdapter)
            binding.spSucursal.setOnClickListener {
                binding.spSucursal.showDropDown()
            }

            // Seleccionar sucursal predeterminada
            val sucursales = viewModel.sucursalesState.value ?: emptyList()
            val defaultCodSucursal = PreferencesHelper.getInstance().getCodSucursal()
            val index = sucursales.indexOfFirst { it.cod_sucursal == defaultCodSucursal }
            if (index != -1) {
                val sucursalNombre = sucursales[index].desc_sucursal
                binding.spSucursal.setText(sucursalNombre, false)
            } else {
                val primeraSucursal = sucursales.first().desc_sucursal
                binding.spSucursal.setText(primeraSucursal, false)
            }

        }

        viewModel.errorEmpresasState.observe(this) { showToast(it) }
        viewModel.errorSucursalesState.observe(this) { showToast(it) }

        viewModel.newInventoryState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> loadingDialog.show()
                is UiState.Success -> {
                    loadingDialog.hide()

                    if (!PermissionManager.hasPermission(Permiso.CARGAR_INV.value)
                        && !PermissionManager.hasPermission(Permiso.VER_INV.value)) {
                        showToast("Inventario creado correctamente")
                        finish()
                    } else {
                        state.data?.let { showPostCreationDialog(it) }

                    }

                }
                is UiState.Error -> {
                    loadingDialog.hide()
                    showToast(state.message)
                }
            }
        }
    }

    // ---------- UI: EVENTOS ----------
    private fun setupEmpresaSpinnerListener() {
        binding.spEmpresa.setOnItemClickListener { _, _, position, _ ->
            viewModel.empresasState.value?.get(position)?.let {
                viewModel.getSucursales(it.cod_empresa)
            }
        }
    }

    private fun setupDatePicker() {
        binding.etFecha.setOnClickListener {
            val now = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val horaActual = LocalTime.now()
                    val fechaCompleta = LocalDateTime.of(year, month + 1, dayOfMonth, horaActual.hour, horaActual.minute, horaActual.second)
                    binding.etFecha.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year))
                    binding.etFecha.tag = fechaCompleta
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
    }

    private fun setupGuardarListener() {
        binding.btnGuardar.setOnClickListener {
            val empresaNombre = binding.spEmpresa.text.toString()
            val empresa = viewModel.empresasState.value?.find { it.desc_empresa == empresaNombre }
            //val empresa = viewModel.empresasState.value?.get(binding.spEmpresa.selectedItemPosition)
            val sucursalNombre = binding.spSucursal.text.toString()
            val sucursal = viewModel.sucursalesState.value?.find { it.desc_sucursal == sucursalNombre }
            //val sucursal = viewModel.sucursalesState.value?.get(binding.spSucursal.selectedItemPosition)
            val fecha = binding.etFecha.tag as? LocalDateTime
            val comentario = binding.etDescripcion.text.toString()

            if (empresa == null || sucursal == null || fecha == null) {
                showToast("Debe completar todos los campos")
                return@setOnClickListener
            }

            val request = NewInventoryRequest(
                cod_empresa = empresa.cod_empresa,
                cod_sucursal = sucursal.cod_sucursal,
                fecha = fecha.toString(),
                descripcion = comentario
            )

            viewModel.createInventory(request)
        }
    }

    // ---------- DIÁLOGO POST CREACIÓN ----------
    private fun showPostCreationDialog(inventario: NewInventoryResponse) {
        if (inventario == null) {
            showToast("Inventario creado, pero no se obtuvo información del registro")
            finish()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Inventario creado")
            .setMessage("¿Deseas cargar productos a este inventario ahora?")
            .setPositiveButton("Sí") { _, _ ->

                val empresa = viewModel.empresasState.value?.find { it.cod_empresa == inventario.cod_empresa }
                val sucursal = viewModel.sucursalesState.value?.find { it.cod_sucursal == inventario.cod_sucursal }

                val inv = ListInventoryResponse(
                    cod_empresa = inventario.cod_empresa,
                    desc_empresa = empresa?.desc_empresa ?: "-",
                    cod_sucursal = inventario.cod_sucursal,
                    desc_sucursal = sucursal?.desc_sucursal ?: "-",
                    nro_registro = inventario.nro_registro.toString(),
                    fecha = binding.etFecha.text?.toString() ?: "",
                    cod_usuario = (PreferencesHelper.getInstance().getUsuario().toString()),
                    estado = "P",
                    descripcion = binding.etDescripcion.text.toString()
                )
 
                val intent = Intent(this, InventoryMainActivity::class.java).apply {
                    putExtra("INVENTARIO", inv)
                }
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                showToast("Inventario creado correctamente")
                finish()
            }
            .show()
    }
}
