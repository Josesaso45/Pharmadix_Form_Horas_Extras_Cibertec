package com.example.android_app.ui.registro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.android_app.databinding.FragmentRegistrarTiempoSheetBinding
import com.example.android_app.data.local.entity.Empleado
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegistrarTiempoBottomSheet(
    private val hojaId: Int,
    private val empleadoId: Int,
    private val onRegistrar: (Int, Empleado, String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentRegistrarTiempoSheetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegistroOperariosViewModel by viewModels({ requireParentFragment() })

    private var empleadoSeleccionado: Empleado? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrarTiempoSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        cargarDatosEmpleado()
        configurarSpinner()
        configurarBotones()
    }

    private fun cargarDatosEmpleado() {
        lifecycleScope.launch {
            val emp = viewModel.buscarEmpleado(empleadoId)
            if (emp != null) {
                empleadoSeleccionado = emp
                binding.tvNombreRegistrar.text = emp.nombre
                binding.tvDetalleRegistrar.text = "${emp.gafete} • ${emp.puesto}"
            } else {
                Snackbar.make(binding.root, "Operario no encontrado", Snackbar.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }

    private fun configurarSpinner() {
        val actividades = arrayOf("Seleccionar actividad...", "Envasado", "Empaque", "Control de Calidad", "Limpieza", "Mantenimiento")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, actividades)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerActividad.adapter = adapter
    }

    private fun configurarBotones() {
        binding.btnCerrarSheet.setOnClickListener {
            dismiss()
        }

        binding.btnRegistrarEntrada.setOnClickListener {
            val actividad = binding.spinnerActividad.selectedItem.toString()
            if (actividad == "Seleccionar actividad...") {
                Snackbar.make(binding.root, "Por favor seleccione una actividad", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            empleadoSeleccionado?.let {
                onRegistrar(hojaId, it, actividad)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
