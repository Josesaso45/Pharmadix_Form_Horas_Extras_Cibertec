package com.example.android_app.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.android_app.data.remote.RetrofitClient
import com.example.android_app.data.remote.model.LoginRequest
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val nombreUsuario: String) : LoginState()
        data class Error(val mensaje: String) : LoginState()
    }

    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    fun login(usuario: String, password: String) {
        if (usuario.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Complete todos los campos")
            return
        }
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(usuario, password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    RetrofitClient.setToken(body.token)
                    val prefs = getApplication<Application>()
                        .getSharedPreferences("pharmadix_prefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit()
                        .putInt("userId", body.usuario.id)
                        .putString("userName", body.usuario.nombre)
                        .putString("token", body.token)
                        .apply()
                    _loginState.value = LoginState.Success(body.usuario.nombre)
                } else {
                    _loginState.value = LoginState.Error("Usuario o contraseña incorrectos")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Error de conexión: ${e.message}")
            }
        }
    }
}
