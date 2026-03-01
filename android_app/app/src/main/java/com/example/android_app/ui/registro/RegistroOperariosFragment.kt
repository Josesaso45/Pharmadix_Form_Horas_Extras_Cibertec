package com.example.android_app.ui.registro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_app.databinding.FragmentRegistroOperariosBinding
import com.example.android_app.ui.registro.adapter.RegistroOperarioAdapter
import com.example.android_app.R
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
        } else if (result.originalIntent?.getBooleanExtra("MANUAL_SEARCH", false) == true) {
            // El usuario presionó el botón de "Buscar manualmente" dentro de la cámara
            mostrarDialogoBusquedaManual()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegistroOperariosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarToolbar()
        configurarRecyclerView()
        configurarBotones()
        observarEstado()
        observarRegistros()
    }

    private fun configurarToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    confirmarCerrarSesion()
                    true
                }
                R.id.action_sync -> {
                    Snackbar.make(binding.root, "Sincronizando...", Snackbar.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun confirmarCerrarSesion() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que deseas salir del sistema?")
            .setPositiveButton("Cerrar Sesión") { _, _ ->
                ejecutarLogout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun ejecutarLogout() {
        // En una app real aquí limpiaríamos SharedPreferences/Token
        // com.example.android_app.data.remote.RetrofitClient.setToken("")
        
        Snackbar.make(binding.root, "Sesión cerrada", Snackbar.LENGTH_SHORT).show()
        
        // Simular navegación al Login (si existiera en el grafo)
        // requireActivity().finish() 
    }

    private fun configurarRecyclerView() {
        adapter = RegistroOperarioAdapter { registro, empleado ->
            // Diálogo de acción al hacer clic en un item del RecyclerView
            val currentHoja = if (hojaId == 0) 1 else hojaId // Fix para testeo
            when (registro.estado) {
                "PENDIENTE" -> mostrarDialogoEntrada(currentHoja, empleado.id, empleado.nombre)
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
        // En ambiente de desarrollo/test, si no hay ID (0) forzamos el 1 
        // para poder ver los datos del seed.
        val targetHojaId = if (hojaId == 0) 1 else hojaId
        
        viewModel.obtenerRegistrosConEmpleado(targetHojaId).observe(viewLifecycleOwner) { lista ->
            adapter.actualizarLista(lista)
            
            // Actualizar contadores en la UI
            val enProceso = lista.count { it.registro.estado == "EN_PROCESO" }
            val finalizados = lista.count { it.registro.estado == "FINALIZADO" }
            
            binding.chipEnProceso.text = "$enProceso en proceso"
            binding.chipFinalizados.text = "$finalizados finalizados"
            binding.tvTotalOperarios.text = "${lista.size} registrados"
            binding.tvNumeroHoja.text = "Hoja: #$targetHojaId"
            
            // Mostrar estado vacío si no hay registros
            binding.layoutEmptyState.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun observarEstado() {
        viewModel.uiState.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                is RegistroOperariosViewModel.UiState.Loading -> {
                    // Mostrar ProgressBar si existe en el layout
                }
                is RegistroOperariosViewModel.UiState.Success -> {
                    if (estado.mensaje == "cerrar_hoja_ok") {
                        com.google.android.material.snackbar.Snackbar.make(binding.root, "Hoja cerrada exitosamente", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                        parsearAccionQr(estado.mensaje)
                    }
                    viewModel.resetEstado()
                }
                is RegistroOperariosViewModel.UiState.Error -> {
                    com.google.android.material.snackbar.Snackbar.make(binding.root, estado.mensaje, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
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
                // Buscar registro y mostrar diálogo de salida
            }
            else -> {
                if (mensaje.isNotEmpty())
                    com.google.android.material.snackbar.Snackbar.make(binding.root, mensaje, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    // ── Diálogo Entrada ───────────────────────────────────────────────────
    private fun mostrarDialogoEntrada(hojaId: Int, empleadoId: Int, nombre: String) {
        val bottomSheet = RegistrarTiempoBottomSheet(hojaId, empleadoId) { targetHoja, empleado, actividad ->
            viewModel.registrarEntrada(targetHoja, empleado)
            com.google.android.material.snackbar.Snackbar.make(binding.root, "Entrada registrada - $actividad", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
        }
        bottomSheet.show(childFragmentManager, "RegistrarTiempoSheet")
    }

    // ── Diálogo Salida ────────────────────────────────────────────────────
    private fun mostrarDialogoSalida(registro: com.example.android_app.data.local.entity.RegistroTiempo, nombre: String) {
        val dialogView = layoutInflater.inflate(com.example.android_app.R.layout.dialog_cerrar_hoja, null)
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .show()
            
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<android.widget.TextView>(com.example.android_app.R.id.tvTitulo)?.text = "Registrar Salida"
        dialogView.findViewById<android.widget.TextView>(com.example.android_app.R.id.tvMensaje)?.text = "¿Confirmar salida de\n$nombre?\nEntrada: ${registro.horaEntrada}"
        val btnCancelar = dialogView.findViewById<android.widget.Button>(com.example.android_app.R.id.btnCancelarCierre)
        val btnConfirmar = dialogView.findViewById<android.widget.Button>(com.example.android_app.R.id.btnConfirmarCierre)
        
        btnConfirmar?.text = "Confirmar Salida"

        btnCancelar?.setOnClickListener { dialog.dismiss() }
        btnConfirmar?.setOnClickListener {
            viewModel.registrarSalida(registro)
            dialog.dismiss()
        }
    }

    // ── Resumen de operario FINALIZADO ────────────────────────────────────
    private fun mostrarResumen(registro: com.example.android_app.data.local.entity.RegistroTiempo, nombre: String) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
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
            val targetHojaId = if (hojaId == 0) 1 else hojaId
            qrLauncher.launch(com.journeyapps.barcodescanner.ScanOptions().apply {
                setDesiredBarcodeFormats(com.journeyapps.barcodescanner.ScanOptions.QR_CODE)
                setPrompt("Apunte al QR del gafete (Hoja $targetHojaId)")
                setBeepEnabled(true)
                setCaptureActivity(CustomScannerActivity::class.java)
            })
        }
        
        binding.btnBuscarManual.setOnClickListener {
            mostrarDialogoBusquedaManual()
        }

        binding.btnCerrarHoja.setOnClickListener {
            val dialogView = layoutInflater.inflate(com.example.android_app.R.layout.dialog_cerrar_hoja, null)
            val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .show()
                
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val btnCancelar = dialogView.findViewById<android.widget.Button>(com.example.android_app.R.id.btnCancelarCierre)
            val btnConfirmar = dialogView.findViewById<android.widget.Button>(com.example.android_app.R.id.btnConfirmarCierre)

            btnCancelar?.setOnClickListener { dialog.dismiss() }
            btnConfirmar?.setOnClickListener {
                val targetHojaId = if (hojaId == 0) 1 else hojaId
                viewModel.cerrarHoja(targetHojaId)
                dialog.dismiss()
            }
        }
    }

    private fun mostrarDialogoBusquedaManual() {
        val targetHojaId = if (hojaId == 0) 1 else hojaId
        val bottomSheet = BusquedaManualBottomSheet { empleado ->
            mostrarDialogoEntrada(targetHojaId, empleado.id, empleado.nombre)
        }
        bottomSheet.show(childFragmentManager, "BusquedaManualSheet")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
