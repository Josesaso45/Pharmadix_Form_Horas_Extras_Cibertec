package com.example.android_app.ui.registro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_app.databinding.FragmentRegistroOperariosBinding
import com.example.android_app.ui.registro.adapter.RegistroOperarioAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

class RegistroOperariosFragment : Fragment() {

    private var _binding: FragmentRegistroOperariosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegistroOperariosViewModel by viewModels()
    private lateinit var adapter: RegistroOperarioAdapter

    // ID de la hoja activa — se recibirá por argumentos en la navegación real
    private val hojaId: Int get() = arguments?.getInt("hojaId", 0) ?: 0

    // ── QR Scanner ───────────────────────────────────────────────────────
    private val qrLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents != null) {
            viewModel.procesarQr(gafete = result.contents, hojaId = hojaId)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegistroOperariosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarRecyclerView()
        configurarBotones()
        observarEstado()
        observarRegistros()
    }

    private fun configurarRecyclerView() {
        adapter = RegistroOperarioAdapter { registro, empleado ->
            // Diálogo de acción al hacer clic en un item del RecyclerView
            when (registro.estado) {
                "PENDIENTE" -> mostrarDialogoEntrada(hojaId, empleado.id, empleado.nombre)
                "EN_PROCESO" -> mostrarDialogoSalida(registro, empleado.nombre)
                "FINALIZADO" -> mostrarResumen(registro, empleado.nombre)
            }
        }
        binding.recyclerViewOperarios.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RegistroOperariosFragment.adapter
        }
    }

    private fun observarRegistros() {
        if (hojaId > 0) {
            viewModel.obtenerRegistros(hojaId).observe(viewLifecycleOwner) { registros ->
                // Aquí habría que unir con empleados - simplificado para MVP
            }
        }
    }

    private fun observarEstado() {
        viewModel.uiState.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                is RegistroOperariosViewModel.UiState.Loading -> {
                    // Mostrar ProgressBar si existe en el layout
                }
                is RegistroOperariosViewModel.UiState.Success -> {
                    // Interpretar mensaje de acción del QR
                    parsearAccionQr(estado.mensaje)
                    viewModel.resetEstado()
                }
                is RegistroOperariosViewModel.UiState.Error -> {
                    Snackbar.make(binding.root, estado.mensaje, Snackbar.LENGTH_LONG).show()
                    viewModel.resetEstado()
                }
                else -> {}
            }
        }
    }

    private fun parsearAccionQr(mensaje: String) {
        when {
            mensaje.startsWith("accion_entrada:") -> {
                val partes = mensaje.split(":")
                val empleadoId = partes.getOrNull(1)?.toIntOrNull() ?: return
                val nombre = partes.getOrNull(2) ?: "Operario"
                mostrarDialogoEntrada(hojaId, empleadoId, nombre)
            }
            mensaje.startsWith("accion_salida:") -> {
                val partes = mensaje.split(":")
                val registroId = partes.getOrNull(1)?.toIntOrNull() ?: return
                val nombre = partes.getOrNull(2) ?: "Operario"
                // Buscar registro actual para mostrar diálogo
                Snackbar.make(binding.root, "Confirmar salida de $nombre", Snackbar.LENGTH_SHORT).show()
            }
            else -> {
                if (mensaje.isNotEmpty())
                    Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    // ── Diálogo Entrada ───────────────────────────────────────────────────
    private fun mostrarDialogoEntrada(hojaId: Int, empleadoId: Int, nombre: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Registrar Entrada")
            .setMessage("¿Confirmar entrada de\n$nombre?")
            .setPositiveButton("✅ Confirmar Entrada") { _, _ ->
                lifecycleScope.launch {
                    val empleado = viewModel.buscarEmpleado(empleadoId)
                    if (empleado != null) {
                        viewModel.registrarEntrada(hojaId, empleado)
                    } else {
                        Snackbar.make(binding.root, "Operario no encontrado", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ── Diálogo Salida ────────────────────────────────────────────────────
    private fun mostrarDialogoSalida(registro: com.example.android_app.data.local.entity.RegistroTiempo, nombre: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Registrar Salida")
            .setMessage("¿Confirmar salida de\n$nombre?\nEntrada: ${registro.horaEntrada}")
            .setPositiveButton("🔴 Confirmar Salida") { _, _ ->
                viewModel.registrarSalida(registro)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ── Resumen de operario FINALIZADO ────────────────────────────────────
    private fun mostrarResumen(registro: com.example.android_app.data.local.entity.RegistroTiempo, nombre: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Resumen de Turno")
            .setMessage(
                "$nombre\n" +
                "Entrada: ${registro.horaEntrada}\n" +
                "Salida:  ${registro.horaSalida}\n" +
                "Total:   ${registro.horasTotales} h"
            )
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun configurarBotones() {
        binding.fabEscanearQr.setOnClickListener {
            qrLauncher.launch(ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Apunte al QR del gafete del operario")
                setBeepEnabled(true)
            })
        }
        binding.btnBuscarManual.setOnClickListener {
            Snackbar.make(binding.root, "Búsqueda manual (próxima iteración)", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
