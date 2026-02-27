package com.example.android_app.ui.registro.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.android_app.R
import com.example.android_app.data.local.entity.Empleado
import com.example.android_app.data.local.entity.RegistroTiempo
import com.example.android_app.databinding.ItemRegistroOperarioBinding

/**
 * Adaptador RecyclerView – patrón ViewHolder Cibertec Sesión 3.
 * Visualiza los 3 estados: PENDIENTE (gris) / EN_PROCESO (naranja) / FINALIZADO (verde)
 */
class RegistroOperarioAdapter(
    private val onItemClick: (RegistroTiempo, Empleado) -> Unit
) : RecyclerView.Adapter<RegistroOperarioAdapter.ViewHolder>() {

    data class RegistroConEmpleado(
        val registro: RegistroTiempo,
        val empleado: Empleado
    )

    private val lista = mutableListOf<RegistroConEmpleado>()

    // ── ViewHolder: patrón Cibertec ────────────────────────
    inner class ViewHolder(val binding: ItemRegistroOperarioBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRegistroOperarioBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        with(holder.binding) {
            tvNombreOperario.text = item.empleado.nombre
            tvGafete.text = "Gafete: ${item.empleado.gafete}"
            tvPuesto.text = item.empleado.puesto
            tvHoraEntrada.text = item.registro.horaEntrada ?: "--:--"
            tvHoraSalida.text = item.registro.horaSalida ?: "--:--"
            tvHorasTotales.text = item.registro.horasTotales?.let { String.format("%.2f h", it) } ?: "--"

            val (colorRes, texto) = when (item.registro.estado) {
                "EN_PROCESO"  -> Pair(R.color.estado_en_proceso, "EN PROCESO")
                "FINALIZADO"  -> Pair(R.color.estado_finalizado, "FINALIZADO")
                else          -> Pair(R.color.estado_pendiente, "PENDIENTE")
            }
            chipEstado.setChipBackgroundColorResource(colorRes)
            chipEstado.text = texto

            root.setOnClickListener { onItemClick(item.registro, item.empleado) }
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<RegistroConEmpleado>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}
