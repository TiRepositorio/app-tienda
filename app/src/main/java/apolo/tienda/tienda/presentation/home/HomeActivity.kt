package apolo.tienda.tienda.presentation.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import apolo.tienda.tienda.R
import apolo.tienda.tienda.data.remote.repository.InventoryRepositoryImpl
import apolo.tienda.tienda.databinding.ActivityHomeBinding
import apolo.tienda.tienda.di.NetworkModule
import apolo.tienda.tienda.presentation.inventory.list.ListInventoryActivity
import apolo.tienda.tienda.presentation.inventory.new.NewInventoryActivity
import apolo.tienda.tienda.presentation.dialog.EmpresaSelectorDialogFragment
import apolo.tienda.tienda.presentation.dialog.SucursalSelectorDialogFragment
import apolo.tienda.tienda.presentation.importaciondatos.ImportarDatosActivity
import apolo.tienda.tienda.presentation.offline.inventario.listar.ListarTomaInventariosActivity
import apolo.tienda.tienda.presentation.offline.inventario.nuevo.NuevoInventarioActivity
import apolo.tienda.tienda.presentation.state.UiState
import apolo.tienda.tienda.utils.LoadingDialog
import apolo.tienda.tienda.utils.Permiso
import apolo.tienda.tienda.utils.PermissionManager
import apolo.tienda.tienda.utils.PreferencesHelper
import apolo.tienda.tienda.utils.getBackendUrl
import apolo.tienda.tienda.utils.showToast

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)


        setupDependencies()
        setupIni()
        mostrarEmpresaSucursal()
        setupObservers()


    }


    fun setupIni() {
        if (!PreferencesHelper.getInstance().getOffline()) {
            viewModel.getUserConfig()
        } else {
            //configurar opciones despues de obtener los permisos del usuario
            setupRecyclerViewLocal()
        }
    }

    private fun setupObservers() {
        viewModel.userConfigsState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Mostrar loading
                    loadingDialog.show()
                }
                is UiState.Success -> {
                    val configs = state.data
                    PreferencesHelper.getInstance().saveCodEmpresa(configs!!.cod_empresa)
                    PreferencesHelper.getInstance().saveDescEmpresa(configs.desc_empresa)
                    PreferencesHelper.getInstance().saveCodSucursal(configs.cod_sucursal)
                    PreferencesHelper.getInstance().saveDescSucursal(configs.desc_sucursal)
                    mostrarEmpresaSucursal()


                    PermissionManager.setPermissions(configs.permisos)

                    //configurar opciones despues de obtener los permisos del usuario
                    setupRecyclerView()

                    // Usar configs
                    loadingDialog.hide()


                }
                is UiState.Error -> {
                    showToast(state.message)
                    loadingDialog.hide()
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    fun mostrarEmpresaSucursal() {
        val prefs = PreferencesHelper.getInstance()
        binding.tvUsuario.text = "${prefs.getUsuario()}"

        if(prefs.getOffline()) {
            binding.tvEmpresa.visibility = View.GONE
            binding.tvSucursal.visibility = View.GONE
        } else {
            binding.tvEmpresa.text = "${prefs.getCodEmpresa()} - ${prefs.getDescEmpresa()}"
            binding.tvSucursal.text = "${prefs.getCodSucursal()} - ${prefs.getDescSucursal()}"
        }


    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(!PreferencesHelper.getInstance().getOffline()) {
            menuInflater.inflate(R.menu.menu_main, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_select_empresa -> {
                viewModel.getEmpresas() // Traer desde la API si es necesario
                showEmpresaDialog()
                // o mostrar un diálogo
                true
            }
            R.id.action_select_sucursal -> {
                val codEmpresa = PreferencesHelper.getInstance().getCodEmpresa()
                viewModel.getSucursales("") // Traer desde la API si es necesario
                if (codEmpresa != null) {
                    showSucursalDialog(codEmpresa)
                }
                // o abrir otra pantalla
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun showEmpresaDialog() {
        val dialog = EmpresaSelectorDialogFragment { cod, desc ->
            // Guardás en SharedPreferences, por ejemplo
            val prefs = PreferencesHelper.getInstance()
            prefs.saveCodEmpresa(cod)
            prefs.saveDescEmpresa(desc)

            // Borramos la sucursal seleccionada anteriormente
            prefs.saveCodSucursal("")
            prefs.saveDescSucursal("")

            mostrarEmpresaSucursal()
            showSucursalDialog(cod)

        }
        dialog.show(supportFragmentManager, "SelectorEmpresa")
    }


    private fun showSucursalDialog(cod_empresa: String) {
        val dialog = SucursalSelectorDialogFragment(cod_empresa) { codigo, descripcion ->
            PreferencesHelper.getInstance().saveCodSucursal(codigo)
            PreferencesHelper.getInstance().saveDescSucursal(descripcion)
            //showToast("Sucursal seleccionada: $descripcion")
            mostrarEmpresaSucursal()
        }

        dialog.show(supportFragmentManager, "SucursalDialog")
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

    private fun setupRecyclerView() {


        val options = listOf(
            HomeOption("Nuevo Inventario", R.drawable.ic_add, "new_inventory"),
            HomeOption("Cargar Inventario", R.drawable.ic_inventory, "load_inventory"),
            //HomeOption("Ver Inventarios", R.drawable.ic_list, "view_inventory"),
            HomeOption("Salir", R.drawable.ic_logout, "logout")
        )

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = HomeAdapter(options) { option ->
            //Toast.makeText(this, "Seleccionaste: ${option.title}", Toast.LENGTH_SHORT).show()
            // Aca podés ir navegando a cada Activity correspondiente
            when (option.id) {
                "new_inventory" -> {

                    if (PreferencesHelper.getInstance().getCodEmpresa()!!.isEmpty() || PreferencesHelper.getInstance().getCodSucursal()!!.isEmpty()) {
                        showToast("Debe seleccionar una empresa y una sucursal válidas")
                        return@HomeAdapter
                    }

                    if (!PermissionManager.hasPermission(Permiso.CREAR_INV.value)) {
                        showToast("No posee permisos para esta operacion")
                        return@HomeAdapter
                    }


                    startActivity(Intent(this, NewInventoryActivity::class.java))
                }
                "load_inventory" -> {

                    if (PreferencesHelper.getInstance().getCodEmpresa()!!.isEmpty() || PreferencesHelper.getInstance().getCodSucursal()!!.isEmpty()) {
                        showToast("Debe seleccionar una empresa y una sucursal válidas")
                        return@HomeAdapter
                    }

                    if (!PermissionManager.hasPermission(Permiso.CARGAR_INV.value)
                        && !PermissionManager.hasPermission(Permiso.VER_INV.value)) {
                        showToast("No posee permisos para esta operacion")
                        return@HomeAdapter
                    }


                    val intent = Intent(this, ListInventoryActivity::class.java).apply {
                        putExtra("SOLO_LECTURA", false)
                    }
                    startActivity(intent)
                }
                "view_inventory" -> {

                    if (PreferencesHelper.getInstance().getCodEmpresa()!!.isEmpty() || PreferencesHelper.getInstance().getCodSucursal()!!.isEmpty()) {
                        showToast("Debe seleccionar una empresa y una sucursal válidas")
                        return@HomeAdapter
                    }

                    if (!PermissionManager.hasPermission(Permiso.VER_INV.value)) {
                        showToast("No posee permisos para esta operacion")
                        return@HomeAdapter
                    }


                    val intent = Intent(this, ListInventoryActivity::class.java).apply {
                        putExtra("SOLO_LECTURA", true)
                    }
                    startActivity(intent)
                }
                "logout" -> finish()
            }
        }
    }


    private fun setupRecyclerViewLocal() {


        val options = listOf(
            HomeOption("Nueva Toma", R.drawable.ic_add, "new_inventory"),
            HomeOption("Continuar Toma", R.drawable.ic_inventory, "load_inventory"),
            HomeOption("Importar Datos", R.drawable.ic_importar, "importar_datos"),
            HomeOption("Salir", R.drawable.ic_logout, "logout")
        )

        //startActivity(Intent(this, ImportarDatosActivity::class.java))

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = HomeAdapter(options) { option ->
            //Toast.makeText(this, "Seleccionaste: ${option.title}", Toast.LENGTH_SHORT).show()
            // Aca podés ir navegando a cada Activity correspondiente
            when (option.id) {
                "new_inventory" -> {

                    startActivity(Intent(this, NuevoInventarioActivity::class.java))
                }
                "load_inventory" -> {

                    startActivity(Intent(this, ListarTomaInventariosActivity::class.java))

                }

                "importar_datos" -> {

                    startActivity(Intent(this, ImportarDatosActivity::class.java))

                }

                "logout" -> finish()
            }
        }
    }


    @Deprecated("Deprecated desde Android 13. Usar OnBackPressedDispatcher si es posible")
    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar salida")
            .setMessage("¿Estás seguro de que deseas salir de la aplicación?")
            .setPositiveButton("Sí") { _, _ ->
                super.onBackPressed() // Ahora se llama a super solo si el usuario confirma
            }
            .setNegativeButton("Cancelar", null)
            .show()

    }

}
