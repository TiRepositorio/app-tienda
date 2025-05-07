package apolo.tienda.tienda.presentation.importaciondatos



import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apolo.tienda.tienda.data.remote.repository.ImportarDatosRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ImportarDatosViewModel : ViewModel() {

    private lateinit var repository: ImportarDatosRepositoryImpl

    private val _importarDatosState = MutableStateFlow<ImportarDatosState>(ImportarDatosState.Idle)
    val importarDatosState: StateFlow<ImportarDatosState> = _importarDatosState

    fun setRepository(repository: ImportarDatosRepositoryImpl) {
        this.repository = repository
    }

    fun importarDatos(context: Context) {
        viewModelScope.launch {
            _importarDatosState.value = ImportarDatosState.Loading

            val result = repository.importarDesdeZip(context) { index, total, tabla ->
                _importarDatosState.value = ImportarDatosState.Progress(index, total, tabla)
            }

            _importarDatosState.value = when {
                result.isSuccess -> ImportarDatosState.Success
                else -> ImportarDatosState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }
}