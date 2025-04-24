package apolo.tienda.tienda.presentation.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apolo.tienda.tienda.data.remote.repository.AuthRepositoryImpl
import apolo.tienda.tienda.utils.PreferencesHelper
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    val loginState = MutableLiveData<LoginState>()

    private lateinit var repository: AuthRepositoryImpl

    fun setRepository(repository: AuthRepositoryImpl) {
        this.repository = repository
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            loginState.value = LoginState.Loading

            val result = repository.login(username, password)

            result.fold(
                onSuccess = { response ->

                    val prefs = PreferencesHelper.getInstance()
                    prefs.saveToken(response.token)

                    loginState.value = LoginState.Success
                },
                onFailure = {
                    loginState.value = LoginState.Error(it.message ?: "Ocurrió un error")
                }
            )
        }
    }

}
