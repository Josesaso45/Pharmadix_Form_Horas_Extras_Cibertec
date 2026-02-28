package com.example.android_app.ui.registro

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_app.data.local.entity.Empleado
import com.example.android_app.databinding.FragmentBusquedaManualBinding
import com.example.android_app.ui.registro.adapter.OperarioBusquedaAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BusquedaManualBottomSheet(
    private val onOperarioSeleccionado: (Empleado) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentBusquedaManualBinding? = null
    private val binding get() = _binding!!

    // Necesitamos que este viewModel comparta ciclo o resuelva datos
    private val viewModel: RegistroOperariosViewModel by viewModels({ requireParentFragment() })
    private lateinit var adapter: OperarioBusquedaAdapter
    private var listaOriginal: List<Empleado> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBusquedaManualBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        configurarRecyclerView()
        observarEmpleados()
        configurarBuscador()
    }

    private fun configurarRecyclerView() {
        adapter = OperarioBusquedaAdapter { empleado ->
            onOperarioSeleccionado(empleado)
            dismiss()
        }
        binding.rvOperariosManual.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOperariosManual.adapter = adapter
    }

    private fun observarEmpleados() {
        // Al observar aquí, forzamos a Room a hacer la consulta si no la había hecho
        viewModel.empleadosCatalogo.observe(viewLifecycleOwner) { empleados ->
            listaOriginal = empleados
            if (empleados.isEmpty()) {
                binding.tvEmptyCatalog.visibility = View.VISIBLE
                binding.rvOperariosManual.visibility = View.GONE
            } else {
                binding.tvEmptyCatalog.visibility = View.GONE
                binding.rvOperariosManual.visibility = View.VISIBLE
                adapter.actualizarLista(empleados)
            }
        }
    }

    private fun configurarBuscador() {
        binding.etBuscarOperario.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                if (query.isEmpty()) {
                    adapter.actualizarLista(listaOriginal)
                } else {
                    val filtrados = listaOriginal.filter {
                        it.nombre.lowercase().contains(query) || it.gafete.lowercase().contains(query)
                    }
                    adapter.actualizarLista(filtrados)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
