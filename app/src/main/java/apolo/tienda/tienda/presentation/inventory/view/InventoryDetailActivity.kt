package apolo.tienda.tienda.presentation.inventory.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import apolo.tienda.tienda.data.remote.repository.InventoryRepositoryImpl
import apolo.tienda.tienda.databinding.ActivityInventoryDetailBinding
import apolo.tienda.tienda.di.NetworkModule
import apolo.tienda.tienda.domain.model.Inventory
import apolo.tienda.tienda.presentation.state.UiState
import apolo.tienda.tienda.utils.LoadingDialog
import apolo.tienda.tienda.utils.Permiso
import apolo.tienda.tienda.utils.PermissionManager
import apolo.tienda.tienda.utils.PreferencesHelper
import apolo.tienda.tienda.utils.getBackendUrl
import apolo.tienda.tienda.utils.showToast

class InventoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryDetailBinding
    private val viewModel: InventoryDetailViewModel by viewModels()
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var inventario: Inventory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Detalle de Inventario"

        loadingDialog = LoadingDialog(this)

        setupDependencies()
        getIntentData()
        setupObservers()

        viewModel.getDetalleInventario(
            codEmpresa = inventario.codEmpresa,
            nroRegistro = inventario.nroRegistro.toString()
        )
    }

    private fun setupDependencies() {
        val retrofit = NetworkModule.provideRetrofit(getBackendUrl(), PreferencesHelper.getInstance())
        val api = NetworkModule.provideInventoryApi(retrofit)
        val repository = InventoryRepositoryImpl(api)
        viewModel.setRepository(repository)
    }

    private fun getIntentData() {
        val nroRegistro = intent.getStringExtra("NRO_REGISTRO")
        val codEmpresa = intent.getStringExtra("COD_EMPRESA")
        val codSucursal = intent.getStringExtra("COD_SUCURSAL")

        if (nroRegistro == null || codEmpresa == null || codSucursal == null) {
            showToast("Inventario inválido")
            finish()
            return
        }

        binding.tvNroInventario.text = "Inventario Nro.: ${nroRegistro}"
        binding.tvEmpresa.text = "Empresa: ${codEmpresa}"
        binding.tvSucursal.text = "Sucursal: ${codSucursal}"

        inventario = Inventory(
            nroRegistro = nroRegistro.toInt(),
            codEmpresa = codEmpresa,
            codSucursal = codSucursal,
            fecha = "",
            descripcion = "",
            descEmpresa = "",
            descSucursal = ""
        )
    }



    private fun setupObservers() {
        viewModel.detalleState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> loadingDialog.show()
                is UiState.Success -> {
                    loadingDialog.hide()
                    val detalles = state.data ?: emptyList()
                    val adapterRV = InventoryDetailAdapter(detalles)

                    binding.rvDetalles.apply {
                        layoutManager = LinearLayoutManager(this@InventoryDetailActivity)
                        adapter = adapterRV
                    }

                    // cabecera
                    if (detalles.isNotEmpty()) {
                        val first = detalles.first()
                        binding.tvNroInventario.text = "Inventario Nro.: ${first.nro_registro}"
                        binding.tvEmpresa.text = "Empresa: ${first.cod_empresa}-${first.desc_empresa}"
                        binding.tvSucursal.text = "Sucursal: ${first.cod_sucursal}-${first.desc_sucursal}"
                    }
                    // Mostrar total de registros
                    binding.tvTotalRegistros.text = "Total de registros: ${detalles.size}"


                    val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                        override fun onMove(
                            recyclerView: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder
                        ): Boolean = false

                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            val position = viewHolder.adapterPosition
                            val item = adapterRV.getItemAt(position)

                            if (!PermissionManager.hasPermission(Permiso.ELIMINAR_DET.value)) {
                                showToast("No tiene permiso para eliminar.")
                                adapterRV.notifyItemChanged(position) // vuelve atrás el swipe
                                return
                            }

                            AlertDialog.Builder(this@InventoryDetailActivity)
                                .setTitle("Eliminar detalle")
                                .setMessage("¿Está seguro que desea eliminar este detalle?")
                                .setPositiveButton("Sí") { _, _ ->
                                    viewModel.deleteDetInventario(item) // Tu lógica de eliminación
                                }
                                .setNegativeButton("Cancelar") { _, _ ->
                                    adapterRV.notifyItemChanged(position)
                                }
                                .setCancelable(false)
                                .show()
                        }
                    })

                    itemTouchHelper.attachToRecyclerView(binding.rvDetalles)
                }
                is UiState.Error -> {
                    loadingDialog.hide()
                    showToast(state.message)
                }
            }
        }

        viewModel.deleteItemState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> loadingDialog.show()
                is UiState.Success -> {
                    loadingDialog.hide()
                    showToast("Detalle eliminado correctamente")
                    viewModel.getDetalleInventario(
                        codEmpresa = inventario.codEmpresa,
                        nroRegistro = inventario.nroRegistro.toString()
                    )
                }
                is UiState.Error -> {
                    loadingDialog.hide()
                    showToast("Error: ${state.message}")
                }
            }
        }

    }
}
