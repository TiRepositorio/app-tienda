package apolo.tienda.tienda.presentation.login

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import apolo.tienda.tienda.data.remote.repository.AuthRepositoryImpl
import apolo.tienda.tienda.databinding.ActivityLoginBinding
import apolo.tienda.tienda.di.NetworkModule
import apolo.tienda.tienda.presentation.config.ConfigActivity
import apolo.tienda.tienda.presentation.home.HomeActivity
import apolo.tienda.tienda.utils.PreferencesHelper
import apolo.tienda.tienda.utils.getBackendUrl
import apolo.tienda.tienda.utils.showToast

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)


        val preferencesHelper = PreferencesHelper.getInstance()
        val backendIp = preferencesHelper.getBackendIp()

        if (backendIp.isNullOrEmpty()) {
            startActivity(Intent(this, ConfigActivity::class.java))
            return
        }

        setContentView(binding.root)

        // Acá creamos Retrofit con la IP dinámica
        val retrofit = NetworkModule.provideRetrofit(getBackendUrl(), preferencesHelper)

        val api = NetworkModule.provideAuthApi(retrofit)
        val repository = AuthRepositoryImpl(api)

        viewModel.setRepository(repository)

        setupListeners()
        setupObservers()
    }


    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.edtUser.text.toString()
            val password = binding.edtPassword.text.toString()
            viewModel.login(username, password)
        }

        binding.imgLogo.setOnLongClickListener {
            mostrarDialogConfiguracion()
            true
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
    }

    private fun mostrarDialogConfiguracion() {
        AlertDialog.Builder(this)
            .setTitle("Configuración")
            .setMessage("¿Deseas ir a la pantalla de configuración?")
            .setPositiveButton("Sí") { _, _ ->
                startActivity(Intent(this, ConfigActivity::class.java))
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


}
