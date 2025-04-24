package apolo.tienda.tienda.presentation.inventory.list

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import apolo.tienda.tienda.data.remote.repository.InventoryRepositoryImpl
import apolo.tienda.tienda.data.remote.request.CloseInventoryRequest
import apolo.tienda.tienda.data.remote.response.ListInventoryResponse
import apolo.tienda.tienda.databinding.ActivityInventoryListBinding
import apolo.tienda.tienda.di.NetworkModule
import apolo.tienda.tienda.presentation.inventory.load.InventoryLoadActivity
import apolo.tienda.tienda.presentation.inventory.main.InventoryMainActivity
import apolo.tienda.tienda.presentation.inventory.view.InventoryDetailActivity
import apolo.tienda.tienda.presentation.state.UiState
import apolo.tienda.tienda.utils.*

class ListInventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryListBinding
    private val viewModel: ListInventoryViewModel by viewModels()
    private lateinit var loadingDialog: LoadingDialog
    private var soloLectura: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        getIntentData()
        setupDependencies()
        setupObservers()

        viewModel.getListInventory(PreferencesHelper.getInstance().getCodEmpresa()!!)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Inventarios Activos"
    }

    private fun getIntentData() {
        soloLectura = intent.getBooleanExtra("SOLO_LECTURA", false)
        soloLectura = true
    }

    private fun setupDependencies() {
        loadingDialog = LoadingDialog(this)
        val retrofit = NetworkModule.provideRetrofit(
            getBackendUrl(),
            PreferencesHelper.getInstance()
        )
        val api = NetworkModule.provideInventoryApi(retrofit)
        val repository = InventoryRepositoryImpl(api)
        viewModel.setRepository(repository)
    }

    private fun setupObservers() {
        viewModel.listInventoryState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> loadingDialog.show()
                is UiState.Success -> {
                    loadingDialog.hide()
                    state.data?.let { setupRecyclerView(it) }
                }
                is UiState.Error -> {
                    loadingDialog.hide()
                    showToast(state.message)
                }
            }
        }


        viewModel.closeInventoryState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> loadingDialog.show()
                is UiState.Success -> {
                    loadingDialog.hide()
                    viewModel.getListInventory(PreferencesHelper.getInstance().getCodEmpresa()!!)
                    showToast("Inventario cerrado!")
                }
                is UiState.Error -> {
                    loadingDialog.hide()
                    showToast(state.message)
                }
            }
        }
    }

    private fun setupRecyclerView(items: List<ListInventoryResponse>) {


        val adapter = ListInventoryAdapter(
            items,
            soloLectura,
            onCerrarClicked = { inventario ->
                //showToast("Cerrar inventario nro: ${inventario.nro_registro}")
                var request = CloseInventoryRequest(cod_empresa = inventario.cod_empresa,
                                                    nro_registro = inventario.nro_registro)
                viewModel.closeInventory(request)

            },
            onCargarClicked = { inventario ->
                //showToast("Cargar inventario nro: ${inventario.nro_registro}")
                val intent = Intent(binding.root.context, InventoryLoadActivity::class.java)
                intent.putExtra("COD_EMPRESA", inventario.cod_empresa)
                intent.putExtra("COD_SUCURSAL", inventario.cod_sucursal)
                intent.putExtra("NRO_REGISTRO", inventario.nro_registro)
                binding.root.context.startActivity(intent)
            },
            onItemClicked = { inventario ->

                //val intent = Intent(this, InventoryDetailActivity::class.java).apply {
                val intent = Intent(this, InventoryMainActivity::class.java).apply {
                    /*putExtra("NRO_REGISTRO", inventario.nro_registro)
                    putExtra("COD_EMPRESA", inventario.cod_empresa)
                    putExtra("COD_SUCURSAL", inventario.cod_sucursal)*/
                    putExtra("INVENTARIO", inventario)
                }
                startActivity(intent)
            }
        )

        binding.rvInventories.apply {
            layoutManager = LinearLayoutManager(this@ListInventoryActivity)
            this.adapter = adapter
        }
    }
}
