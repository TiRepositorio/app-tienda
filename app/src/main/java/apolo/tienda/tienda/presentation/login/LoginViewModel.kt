package apolo.tienda.tienda.presentation.login

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apolo.tienda.tienda.data.local.repository.AuthLocalRepositoryImpl
import apolo.tienda.tienda.data.local.request.LoginLocalRequest
import apolo.tienda.tienda.data.remote.repository.AuthRepositoryImpl
import apolo.tienda.tienda.utils.PreferencesHelper
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    val loginState = MutableLiveData<LoginState>()

    private lateinit var remoteRepository: AuthRepositoryImpl
    private lateinit var localRepository: AuthLocalRepositoryImpl

    fun setRemoteRepository(repository: AuthRepositoryImpl) {
        this.remoteRepository = repository
    }

    fun setLocalRepository(repository: AuthLocalRepositoryImpl) {
        this.localRepository = repository
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

}
