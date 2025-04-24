package apolo.tienda.tienda.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apolo.tienda.tienda.data.remote.repository.InventoryRepositoryImpl
import apolo.tienda.tienda.data.remote.response.EmpresaResponse
import apolo.tienda.tienda.data.remote.response.SucursalResponse
import apolo.tienda.tienda.data.remote.response.UserConfigResponse
import apolo.tienda.tienda.presentation.state.UiState
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private lateinit var repository: InventoryRepositoryImpl

    private val _empresasState = MutableLiveData<List<EmpresaResponse>>()
    val empresasState: LiveData<List<EmpresaResponse>> = _empresasState

    private val _errorEmpresaState = MutableLiveData<String>()
    val errorEmpresaState: LiveData<String> = _errorEmpresaState


    private val _sucursalesState = MutableLiveData<List<SucursalResponse>>()
    val sucursalesState: LiveData<List<SucursalResponse>> = _sucursalesState

    private val _errorSucursalState = MutableLiveData<String>()
    val errorSucursalState: LiveData<String> = _errorSucursalState



    private val _userConfigsState = MutableLiveData<UiState<UserConfigResponse>>()
    val userConfigsState: LiveData<UiState<UserConfigResponse>> = _userConfigsState



    fun setRepository(repo: InventoryRepositoryImpl) {
        this.repository = repo
    }

    fun getEmpresas() {
        viewModelScope.launch {
            val result = repository.getEmpresas()
            result.fold(
                onSuccess = { _empresasState.value = it },
                onFailure = { _errorEmpresaState.value = it.message ?: "Error desconocido" }
            )
        }
    }


    fun getSucursales(cod_empresa: String) {
        viewModelScope.launch {
            val result = repository.getSucursales(cod_empresa)
            result.fold(
                onSuccess = { _sucursalesState.value = it },
                onFailure = { _errorSucursalState.value = it.message ?: "Error desconocido" }
            )
        }
    }

    fun getUserConfig() {
        viewModelScope.launch {
            _userConfigsState.value = UiState.Loading

            val result = repository.getUserConfig()
            result.fold(
                onSuccess = { _userConfigsState.value = UiState.Success(it) },
                onFailure = { _userConfigsState.value = UiState.Error(it.message ?: "Error desconocido") }
            )
        }
    }



}
