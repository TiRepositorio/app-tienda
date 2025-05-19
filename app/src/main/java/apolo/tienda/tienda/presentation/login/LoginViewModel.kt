package apolo.tienda.tienda.presentation.login

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apolo.tienda.tienda.data.local.repository.AuthLocalRepositoryImpl
import apolo.tienda.tienda.data.local.request.LoginLocalRequest
import apolo.tienda.tienda.data.remote.repository.ActualizacionRepositoryImpl
import apolo.tienda.tienda.data.remote.repository.AuthRepositoryImpl
import apolo.tienda.tienda.utils.PreferencesHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    val loginState = MutableLiveData<LoginState>()

    private lateinit var remoteRepository: AuthRepositoryImpl
    private lateinit var localRepository: AuthLocalRepositoryImpl
    private lateinit var actualizacionRepository: ActualizacionRepositoryImpl

    private val _estadoActualizacion = MutableStateFlow<ActualizacionState>(ActualizacionState.Idle)
    val estadoActualizacion: StateFlow<ActualizacionState> = _estadoActualizacion

    fun setRemoteRepository(repository: AuthRepositoryImpl) {
        this.remoteRepository = repository
    }

    fun setLocalRepository(repository: AuthLocalRepositoryImpl) {
        this.localRepository = repository
    }

    fun setActualizacionRepository(repository: ActualizacionRepositoryImpl) {
        this.actualizacionRepository = repository
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            loginState.value = LoginState.Loading

            val result = remoteRepository.login(username, password)

            result.fold(
                onSuccess = { response ->
                    PreferencesHelper.getInstance().saveToken(response.token)
                    PreferencesHelper.getInstance().saveUsuario(username)
                    loginState.value = LoginState.Success
                },
                onFailure = {
                    loginState.value = LoginState.Error(it.message ?: "Ocurrió un error")
                }
            )
        }
    }

    fun loginLocal(username: String, password: String) {
        viewModelScope.launch {
            loginState.value = LoginState.Loading

            val result = localRepository.login(LoginLocalRequest(username, password))

            result.fold(
                onSuccess = { response ->
                    PreferencesHelper.getInstance().saveUsuario(response.codUsuario)
                    loginState.value = LoginState.Success
                },
                onFailure = {
                    loginState.value = LoginState.Error(it.message ?: "Error en login local")
                }
            )
        }
    }


    fun descargarEInstalar(context: Context) {
        viewModelScope.launch {
            _estadoActualizacion.value = ActualizacionState.Loading

            val result = actualizacionRepository.descargarApk(context)
            if (result.isSuccess) {
                val uri = result.getOrNull()!!

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }

                context.startActivity(intent)
                _estadoActualizacion.value = ActualizacionState.Success
            } else {
                _estadoActualizacion.value = ActualizacionState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

}


sealed class ActualizacionState {
    object Idle : ActualizacionState()
    object Loading : ActualizacionState()
    object Success : ActualizacionState()
    data class Error(val mensaje: String) : ActualizacionState()
}
