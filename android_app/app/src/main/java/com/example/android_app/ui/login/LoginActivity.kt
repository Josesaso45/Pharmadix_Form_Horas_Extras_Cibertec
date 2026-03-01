package com.example.android_app.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.android_app.databinding.ActivityLoginBinding
import com.example.android_app.ui.dashboard.DashboardActivity
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observarEstado()
        configurarBotones()
    }

    private fun observarEstado() {
        viewModel.loginState.observe(this) { estado ->
            when (estado) {
                is LoginViewModel.LoginState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                }
                is LoginViewModel.LoginState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                }
                is LoginViewModel.LoginState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
                is LoginViewModel.LoginState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Snackbar.make(binding.root, estado.mensaje, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun configurarBotones() {
        binding.btnLogin.setOnClickListener {
            val usuario = binding.etUsuario.text?.toString()?.trim() ?: ""
            val password = binding.etPassword.text?.toString() ?: ""
            viewModel.login(usuario, password)
        }

        binding.tvCrearCuenta.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
