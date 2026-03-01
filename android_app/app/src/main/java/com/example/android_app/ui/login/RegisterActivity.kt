package com.example.android_app.ui.login

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.android_app.databinding.ActivityRegisterBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/**
 * RegisterActivity – Pantalla de registro de nuevo usuario (tomador).
 * Diseño basado en Stitch: "Pharmadix User Registration"
 * Screen ID: 46b0bb2ebe884249ad47fe835bc4cd74
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observarEstado()
        configurarBotones()
    }

    private fun observarEstado() {
        viewModel.registerState.observe(this) { estado ->
            when (estado) {
                is RegisterViewModel.RegisterState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCrearCuenta.isEnabled = true
                }
                is RegisterViewModel.RegisterState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnCrearCuenta.isEnabled = false
                }
                is RegisterViewModel.RegisterState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    // Mostrar diálogo de éxito y volver al Login
                    MaterialAlertDialogBuilder(this)
                        .setTitle("¡Cuenta Creada!")
                        .setMessage("Tu cuenta ha sido registrada exitosamente.\n\nAhora puedes iniciar sesión con tu usuario y contraseña.")
                        .setPositiveButton("INICIAR SESIÓN") { _, _ ->
                            finish() // Vuelve al LoginActivity
                        }
                        .setCancelable(false)
                        .show()
                }
                is RegisterViewModel.RegisterState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCrearCuenta.isEnabled = true
                    Snackbar.make(binding.root, estado.mensaje, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun configurarBotones() {
        // Volver al login
        binding.btnBack.setOnClickListener { finish() }
        
        // Link "Iniciar Sesión"
        binding.tvIniciarSesion.setOnClickListener { finish() }

        // Botón principal de registro
        binding.btnCrearCuenta.setOnClickListener {
            val usuario = binding.etUsuario.text?.toString()?.trim() ?: ""
            val nombre = binding.etNombre.text?.toString()?.trim() ?: ""
            val password = binding.etPassword.text?.toString() ?: ""
            val confirmPassword = binding.etConfirmPassword.text?.toString() ?: ""
            viewModel.registrar(usuario, nombre, password, confirmPassword)
        }
    }
}
