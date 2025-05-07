package apolo.tienda.tienda.presentation.offline.inventario.listar

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import apolo.tienda.tienda.data.local.api.InventarioLocalApiImpl
import apolo.tienda.tienda.data.local.response.ListaTomaInventarioResponse
import apolo.tienda.tienda.data.remote.repository.InventoryRepositoryImpl
import apolo.tienda.tienda.databinding.ActivityListarTomaInventariosBinding
import apolo.tienda.tienda.di.NetworkModule
import apolo.tienda.tienda.presentation.offline.inventario.cargar.CargarTomaInventarioActivity
import apolo.tienda.tienda.presentation.offline.inventario.nuevo.NuevoInventarioActivity
import apolo.tienda.tienda.presentation.state.UiState
import apolo.tienda.tienda.utils.LoadingDialog
import apolo.tienda.tienda.utils.PreferencesHelper
import apolo.tienda.tienda.utils.getBackendUrl
import apolo.tienda.tienda.utils.showToast

class ListarTomaInventariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListarTomaInventariosBinding
    private val viewModel: ListarTomaInventariosViewModel by viewModels()
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListarTomaInventariosBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setupToolbar()
        setupDependencies()
        setupObservers()
        setupListeners()

        viewModel.getListTomaInventario(applicationContext)


    }

    private fun setupListeners() {
        binding.fabAgregarToma.setOnClickListener {
            startActivity(Intent(this, NuevoInventarioActivity::class.java))
            finish()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getListTomaInventario(applicationContext)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Tomas de Inventario"
    }


    private fun setupDependencies() {
        loadingDialog = LoadingDialog(this)

        val retrofit = NetworkModule.provideRetrofit(
            baseUrl = getBackendUrl(),
            preferencesHelper = PreferencesHelper.getInstance()
        )
        val api = NetworkModule.provideInventoryApi(retrofit)
        val repositoryRemote = InventoryRepositoryImpl(api)

        val repository = InventarioLocalApiImpl()
        viewModel.setRepository(repository)
        viewModel.setRepositoryRemote(repositoryRemote)
    }

    private fun setupObservers() {
        viewModel.listaTomaInventarioState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> binding.swipeRefreshLayout.isRefreshing = true
                is UiState.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    state.data?.let { setupRecyclerView(it) }
                }
                is UiState.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    showToast(state.message)
                }
            }
        }

        viewModel.finalizarTomaInventarioState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> loadingDialog.show()
                is UiState.Success -> {
                    loadingDialog.hide()
                    showToast("Inventario finalizado correctamente")
                    viewModel.getListTomaInventario(applicationContext) // refrescar la lista
                }
                is UiState.Error -> {
                    loadingDialog.hide()
                    showToast(state.message)
                }
            }
        }


        viewModel.enviarTomaInventarioState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> loadingDialog.show()
                is UiState.Success -> {
                    loadingDialog.hide()
                    showToast("¡Toma de Inventario enviado correctamente!")
                    viewModel.getListTomaInventario(applicationContext)
                    //finish() // o recargar el listado
                }
                is UiState.Error -> {
                    loadingDialog.hide()
                    showToast("Error al enviar: ${state.message}")
                }
            }
        }

    }


    private fun setupRecyclerView(items: List<ListaTomaInventarioResponse>) {


        val adapter = ListarTomaInventariosAdapter(
            items,
            onContinuarClicked = { inventario ->
                //showToast("Cargar inventario nro: ${inventario.nro_registro}")
                val intent = Intent(binding.root.context, CargarTomaInventarioActivity::class.java)
                intent.putExtra("ID_INVENTARIO_CABECERA", inventario.id)
                binding.root.context.startActivity(intent)
            },
            onEnviarClicked = { inventario ->


                AlertDialog.Builder(this)
                    .setTitle("Confirmar envío")
                    .setMessage("¿Estás seguro que desea enviar los datos del inventario?")
                    .setPositiveButton("Sí") { _, _ ->

                        val id = inventario.id
                        if (id != 0) {
                            viewModel.enviarTomaInventario(this, id)
                        } else {
                            showToast("ID de inventario inválido.")
                        }

                    }
                    .setNegativeButton("Cancelar", null)
                    .show()





            },
            onFinalizarClicked = { inventario ->


                AlertDialog.Builder(this)
                    .setTitle("Finalizar")
                    .setMessage("¿Estás seguro que desea finalizar esta toma de inventario?")
                    .setPositiveButton("Sí") { _, _ ->

                        viewModel.finalizarTomaInventario(applicationContext, inventario.id)

                    }
                    .setNegativeButton("Cancelar", null)
                    .show()


            }
        )

        binding.rvInventories.apply {
            layoutManager = LinearLayoutManager(this@ListarTomaInventariosActivity)
            this.adapter = adapter
        }
    }


    override fun onResume() {
        viewModel.getListTomaInventario(applicationContext)
        super.onResume()
    }



}