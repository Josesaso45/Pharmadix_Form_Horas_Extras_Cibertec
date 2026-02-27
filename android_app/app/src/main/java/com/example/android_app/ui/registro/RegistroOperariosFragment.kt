package com.example.android_app.ui.registro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_app.databinding.FragmentRegistroOperariosBinding
import com.example.android_app.ui.registro.adapter.RegistroOperarioAdapter
import com.google.android.material.snackbar.Snackbar
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class RegistroOperariosFragment : Fragment() {

    private var _binding: FragmentRegistroOperariosBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: RegistroOperarioAdapter

    private val qrLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents != null) {
            Snackbar.make(binding.root, "QR: ${result.contents}", Snackbar.LENGTH_LONG).show()
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
    }

    private fun configurarRecyclerView() {
        adapter = RegistroOperarioAdapter { registro, empleado ->
            Snackbar.make(binding.root, "Acción: ${registro.estado} – ${empleado.nombre}", Snackbar.LENGTH_SHORT).show()
        }
        binding.recyclerViewOperarios.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RegistroOperariosFragment.adapter
        }
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
