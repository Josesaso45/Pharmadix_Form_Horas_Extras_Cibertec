package com.example.android_app.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.android_app.data.remote.RetrofitClient
import com.example.android_app.data.remote.model.RegisterRequest
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    sealed class RegisterState {
        object Idle : RegisterState()
        object Loading : RegisterState()
        data class Success(val mensaje: String) : RegisterState()
        data class Error(val mensaje: String) : RegisterState()
    }

    private val _registerState = MutableLiveData<RegisterState>(RegisterState.Idle)
    val registerState: LiveData<RegisterState> = _registerState

    fun registrar(usuario: String, nombre: String, password: String, confirmPassword: String) {
        // Validaciones locales
        if (usuario.isBlank() || nombre.isBlank() || password.isBlank()) {
            _registerState.value = RegisterState.Error("Complete todos los campos")
            return
        }
        if (usuario.length < 3) {
            _registerState.value = RegisterState.Error("El usuario debe tener al menos 3 caracteres")
            return
        }
        if (password.length < 6) {
            _registerState.value = RegisterState.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }
        if (password != confirmPassword) {
            _registerState.value = RegisterState.Error("Las contraseñas no coinciden")
            return
        }

        _registerState.value = RegisterState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.register(
                    RegisterRequest(usuario, nombre, password)
                )
                if (response.isSuccessful && response.body() != null) {
                    _registerState.value = RegisterState.Success(
                        "¡Cuenta creada exitosamente!\nUsuario: $usuario"
                    )
                } else if (response.code() == 409) {
                    _registerState.value = RegisterState.Error("El usuario '$usuario' ya existe")
                } else {
                    _registerState.value = RegisterState.Error("Error al crear la cuenta")
                }
            } catch (e: ConnectException) {
                _registerState.value = RegisterState.Error(
                    "No se puede conectar al servidor.\nVerifica que esté en ejecución."
                )
            } catch (e: SocketTimeoutException) {
                _registerState.value = RegisterState.Error(
                    "El servidor tardó demasiado.\nIntenta de nuevo."
                )
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(
                    "Error inesperado: ${e.localizedMessage}"
                )
            }
        }
    }
}
