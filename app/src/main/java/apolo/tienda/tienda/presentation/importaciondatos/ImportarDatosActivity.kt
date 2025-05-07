package apolo.tienda.tienda.presentation.importaciondatos




import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import apolo.tienda.tienda.databinding.ActivityImportarDatosBinding
import apolo.tienda.tienda.data.remote.api.ImportarDatosApi
import apolo.tienda.tienda.data.remote.repository.ImportarDatosRepositoryImpl
import apolo.tienda.tienda.di.NetworkModule
import apolo.tienda.tienda.utils.PreferencesHelper
import apolo.tienda.tienda.utils.getBackendUrl
import apolo.tienda.tienda.utils.showToast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ImportarDatosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImportarDatosBinding
    private val viewModel: ImportarDatosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportarDatosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val retrofit = NetworkModule.provideRetrofit(getBackendUrl(), PreferencesHelper.getInstance())
        val api = retrofit.create(ImportarDatosApi::class.java)
        val repository = ImportarDatosRepositoryImpl(api)

        viewModel.setRepository(repository)

        observeViewModel()
        viewModel.importarDatos(applicationContext)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.importarDatosState.collectLatest { state ->
                when (state) {
                    is ImportarDatosState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                        binding.txtProgress.visibility = View.GONE
                    }
                    is ImportarDatosState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.txtProgress.visibility = View.VISIBLE
                        binding.txtProgress.text = "Iniciando importación..."
                    }
                    is ImportarDatosState.Progress -> {
                        binding.txtProgress.text = "Importando ${state.tabla} (${state.current}/${state.total})"
                    }
                    is ImportarDatosState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.txtProgress.visibility = View.GONE
                        showToast("¡Importación completada!")
                        finish()
                    }
                    is ImportarDatosState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.txtProgress.visibility = View.GONE
                        showToast("Error: ${state.message}")
                        finish()
                    }
                }
            }
        }
    }
}
