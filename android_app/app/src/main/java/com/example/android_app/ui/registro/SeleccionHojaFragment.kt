package com.example.android_app.ui.registro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_app.R
import com.example.android_app.databinding.FragmentSeleccionHojaBinding
import com.example.android_app.ui.registro.adapter.HojaTiempoAdapter
import com.google.android.material.snackbar.Snackbar

class SeleccionHojaFragment : Fragment() {

    private var _binding: FragmentSeleccionHojaBinding? = null
    private val binding get() = _binding!!
    
    // Reutilizamos el ViewModel principal que ya expone hojasActivas
    private val viewModel: RegistroOperariosViewModel by viewModels()
    private lateinit var adapter: HojaTiempoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeleccionHojaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarToolbar()
        configurarRecyclerView()
        observarHojas()
        binding.fabNuevaHoja.setOnClickListener {
            // Generar un ID local temporal (en un caso real lo da el backend al crearla o se asigna tras sincronizar)
            val nuevoId = (1000..9999).random() 
            val numeroHojaStr = "HOJA-$nuevoId"
            
            viewModel.crearHojaVacia(nuevoId, numeroHojaStr) { hojaIdGenerada ->
                // Cuando se crea la hoja en Room, navegamos automáticamente a su vista de operaciones
                val bundle = android.os.Bundle().apply {
                    putInt("hojaId", hojaIdGenerada)
                }
                findNavController().navigate(R.id.action_seleccionHoja_to_registro, bundle)
            }
        }
    }

    private fun configurarToolbar() {
        binding.toolbarSeleccion.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    // Similar al logout en RegistroOperariosFragment
                    Snackbar.make(binding.root, "Sesión cerrada", Snackbar.LENGTH_SHORT).show()
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

    private fun configurarRecyclerView() {
        adapter = HojaTiempoAdapter { hoja ->
            // Navegar al registro pasándole el ID de la hoja seleccionada
            val bundle = android.os.Bundle().apply {
                putInt("hojaId", hoja.id)
            }
            findNavController().navigate(R.id.action_seleccionHoja_to_registro, bundle)
        }
        binding.recyclerViewHojas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SeleccionHojaFragment.adapter
        }
    }

    private fun observarHojas() {
        viewModel.hojasActivas.observe(viewLifecycleOwner) { lista ->
            adapter.actualizarLista(lista)
            binding.layoutEmptyStateHojas.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
