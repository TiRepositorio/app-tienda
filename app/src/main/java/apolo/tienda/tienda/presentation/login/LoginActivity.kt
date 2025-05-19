package apolo.tienda.tienda.presentation.login

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import apolo.tienda.tienda.data.local.api.AuthLocalApiImpl
import apolo.tienda.tienda.data.local.repository.AuthLocalRepositoryImpl
import apolo.tienda.tienda.data.remote.api.ActualizacionApi
import apolo.tienda.tienda.data.remote.repository.ActualizacionRepositoryImpl
import apolo.tienda.tienda.data.remote.repository.AuthRepositoryImpl
import apolo.tienda.tienda.databinding.ActivityLoginBinding
import apolo.tienda.tienda.di.NetworkModule
import apolo.tienda.tienda.presentation.config.ConfigActivity
import apolo.tienda.tienda.presentation.home.HomeActivity
import apolo.tienda.tienda.presentation.importaciondatos.ImportarDatosActivity
import apolo.tienda.tienda.utils.AndroidPermissionHelper
import apolo.tienda.tienda.utils.PreferencesHelper
import apolo.tienda.tienda.utils.getBackendUrl
import apolo.tienda.tienda.utils.showToast

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var permissionHelper: AndroidPermissionHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val preferencesHelper = PreferencesHelper.getInstance()
        val backendIp = preferencesHelper.getBackendIp()

        // 🔐 Validar primero si está configurado
        if (backendIp.isNullOrEmpty()) {
            startActivity(Intent(this, ConfigActivity::class.java))
            finish() // Importante para que no siga este activity en el backstack
            return
        }

        // ✅ Recién ahora creamos Retrofit, API y repositorio
        val retrofit = NetworkModule.provideRetrofit(getBackendUrl(), preferencesHelper)
        val api = NetworkModule.provideAuthApi(retrofit)
        val apiActualizacion = NetworkModule.provideActualizacionApi(retrofit)
        val remoteRepository = AuthRepositoryImpl(api)

        val localApi = AuthLocalApiImpl()
        val localRepository = AuthLocalRepositoryImpl(localApi, applicationContext)
        val actualizacionRepository = ActualizacionRepositoryImpl(apiActualizacion)

        viewModel.setRemoteRepository(remoteRepository)
        viewModel.setLocalRepository(localRepository)
        viewModel.setActualizacionRepository(actualizacionRepository)

        setupListeners()
        setupObservers()
        setupPermissionHelper()

        val versionName = applicationContext.packageManager
            .getPackageInfo(applicationContext.packageName, 0).versionName

        binding.tvVersionApp.text = "Versión $versionName"
    }



    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {



            val username = binding.edtUser.text.toString()
            val password = binding.edtPassword.text.toString()

            val isOffline = PreferencesHelper.getInstance().getOffline()

            if (!isOffline) {
                viewModel.login(username, password) // login actual por API
            } else {
                viewModel.loginLocal(username, password) // nuevo login por DB local
            }


        }

        binding.btnImportarDatos.setOnClickListener {
            startActivity(Intent(this, ImportarDatosActivity::class.java))
        }

        binding.imgLogo.setOnLongClickListener {
            mostrarDialogConfiguracion()
            true
        }

        binding.btnActualizarVersion.setOnClickListener {
            if (puedeInstalarApks()) {
                viewModel.descargarEInstalar(this)
            } else {
                solicitarPermisoInstalacion()
            }
        }
    }

    private fun setupObservers() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> {
                    // mostrar loading
                    binding.loadingOverlay.visibility = android.view.View.VISIBLE
                }
                is LoginState.Success -> {
                    binding.loadingOverlay.visibility = android.view.View.GONE
                    showToast("Login correcto")
                    // ir a Home
                    PreferencesHelper.getInstance().saveUsuario(binding.edtUser.text.toString())
                    binding.edtUser.setText("")
                    binding.edtPassword.setText("")
                    binding.edtUser.requestFocus()
                    startActivity(Intent(this, HomeActivity::class.java))
                    //finish()
                }
                is LoginState.Error -> {
                    binding.loadingOverlay.visibility = android.view.View.GONE
                    showToast(state.message, Toast.LENGTH_LONG)
                }
            }
        }



        lifecycleScope.launchWhenStarted {
            viewModel.estadoActualizacion.collect { estado ->
                when (estado) {
                    is ActualizacionState.Idle -> {
                        // Nada por hacer
                    }
                    is ActualizacionState.Loading -> {
                        binding.loadingOverlay.visibility = View.VISIBLE
                    }
                    is ActualizacionState.Success -> {
                        binding.loadingOverlay.visibility = View.GONE
                        showToast("Instalador descargado. Procediendo a instalar.")
                    }
                    is ActualizacionState.Error -> {
                        binding.loadingOverlay.visibility = View.GONE
                        showToast("Error al descargar: ${estado.mensaje}")
                    }
                }
            }
        }



    }

    private fun setupPermissionHelper() {
        permissionHelper = AndroidPermissionHelper(
            activity = this,
            permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.REQUEST_INSTALL_PACKAGES

            )
        ).apply {
            onAllPermissionsGranted = {
                // Permisos concedidos
            }
            onPermissionsDenied = { denied ->
                // showToast("Permisos denegados: $denied")
            }
            checkAndRequestPermissions()
        }
    }

    private fun mostrarDialogConfiguracion() {
        AlertDialog.Builder(this)
            .setTitle("Configuración")
            .setMessage("¿Deseas ir a la pantalla de configuración?")
            .setPositiveButton("Sí") { _, _ ->
                startActivity(Intent(this, ConfigActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    override fun onResume() {
        super.onResume()
        val preferencesHelper = PreferencesHelper.getInstance()
        val msgError = preferencesHelper.getMsgError()

        if(!msgError.isNullOrEmpty()) {
            preferencesHelper.clearMsgError()
            showToast(msgError, Toast.LENGTH_LONG)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHelper.handlePermissionsResult(requestCode, grantResults)
    }


    private fun puedeInstalarApks(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    private fun solicitarPermisoInstalacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivityForResult(intent, 1234)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1234 && puedeInstalarApks()) {
            viewModel.descargarEInstalar(this)
        }
    }



}
