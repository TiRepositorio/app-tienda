package apolo.tienda.tienda.presentation.config

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import apolo.tienda.tienda.databinding.ActivityConfigBinding
import apolo.tienda.tienda.utils.PreferencesHelper
import apolo.tienda.tienda.utils.showToast

class ConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfigBinding

    private val viewModel: ConfigViewModel by lazy {
        ConfigViewModel(PreferencesHelper.getInstance() )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        loadSavedConfig()
    }



    private fun setupListeners() {
        binding.btnGuardar.setOnClickListener { onSaveClicked() }
    }

    private fun loadSavedConfig() {
        binding.etIp.setText(viewModel.getSavedIp())
        binding.etPort.setText(viewModel.getSavedPort())
    }

    private fun onSaveClicked() {
        val ip = binding.etIp.text.toString()
        val port = binding.etPort.text.toString()

        if (ip.isEmpty()) {
            showToast("Debe ingresar una IP")
            return
        }

        if (!viewModel.isValidIp(ip)) {
            showToast("La IP ingresada no es válida")
            return
        }

        viewModel.saveIp(ip)
        viewModel.savePort(port)
        finish()
    }


}
