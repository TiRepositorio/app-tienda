package apolo.tienda.tienda.presentation.offline.inventario.nuevo

import android.R
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import apolo.tienda.tienda.data.local.api.InventarioLocalApiImpl
import apolo.tienda.tienda.data.local.request.NuevoInventarioCabeceraLocalRequest
import apolo.tienda.tienda.databinding.ActivityNuevoInventarioBinding
import apolo.tienda.tienda.presentation.offline.inventario.cargar.CargarTomaInventarioActivity
import apolo.tienda.tienda.presentation.state.UiState
import apolo.tienda.tienda.utils.LoadingDialog
import apolo.tienda.tienda.utils.PreferencesHelper
import apolo.tienda.tienda.utils.showToast
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar

class NuevoInventarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNuevoInventarioBinding
    private val viewModel: NuevoInventarioViewModel by viewModels()
    private lateinit var loadingDialog: LoadingDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevoInventarioBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setupDependencies()
        setupUi()
        setupObservers()

        viewModel.getListaInventario(applicationContext)

    }


    // ---------- INICIALIZACIÓN Y DEPENDENCIAS ----------
    private fun setupDependencies() {
        val repository = InventarioLocalApiImpl()
        viewModel.setRepository(repository)
        loadingDialog = LoadingDialog(this)
    }

    private fun setupUi() {
        setupListaInventarioSpinnerListener()
        setupDatePicker()
        setFechaActual()
        setupGuardarListener()
    }

    // ---------- UI: EVENTOS ----------
    private fun setupListaInventarioSpinnerListener() {
        binding.spListaInventario.setOnItemClickListener { _, _, position, _ ->
            //viewModel.listaInventarioState.value?.get(position)?.let {
                //it.nroRegistro
            //}
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


    private fun setFechaActual() {
        val now = LocalDateTime.now()
        val fechaTexto = String.format("%02d/%02d/%04d", now.dayOfMonth, now.monthValue, now.year)

        binding.etFecha.setText(fechaTexto)
        binding.etFecha.tag = now
    }

    private fun setupGuardarListener() {
        binding.btnGuardar.setOnClickListener {
            val ajuste = binding.spListaInventario.text.toString().replace("Nro. Ajuste: ", "")
            val fecha = binding.etFecha.tag as? LocalDateTime
            val comentario = binding.etDescripcion.text.toString()



            if (ajuste == "" || fecha == null) {
                showToast("Debe completar todos los campos")
                return@setOnClickListener
            }

            val request = NuevoInventarioCabeceraLocalRequest(
                nroAjuste = ajuste.toInt(),
                usuarioAlta = PreferencesHelper.getInstance().getUsuario() ?: "",
                fechaInventario = fecha.toString(),
                comentario = comentario,
                estado = "P"
            )

            viewModel.crearInventarioLocal(applicationContext, request)
        }
    }


    // ---------- OBSERVERS ----------
    private fun setupObservers() {
        viewModel.listaInventarioState.observe(this) { inventario ->
            val empresaAdapter = ArrayAdapter(this, R.layout.simple_list_item_1, inventario.map { "Nro. Ajuste: ${it.nroRegistro}" })
            (binding.spListaInventario as AutoCompleteTextView).setAdapter(empresaAdapter)
            binding.spListaInventario.setOnClickListener {
                binding.spListaInventario.showDropDown()
            }

        }



        viewModel.errorListaInventarioState.observe(this) { showToast(it) }

        viewModel.nuevoInventarioState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> loadingDialog.show()
                is UiState.Success -> {
                    loadingDialog.hide()


                    state.data?.let {

                        val intent = Intent(this, CargarTomaInventarioActivity::class.java).apply {
                            putExtra("ID_INVENTARIO_CABECERA", it.id)
                        }
                        startActivity(intent)
                        finish()

                    }

                }
                is UiState.Error -> {
                    loadingDialog.hide()
                    showToast(state.message)
                }
            }
        }
    }



}