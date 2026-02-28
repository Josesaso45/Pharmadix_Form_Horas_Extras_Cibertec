package com.example.android_app.ui.registro.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.android_app.R
import com.example.android_app.data.local.entity.Empleado
import com.example.android_app.data.local.entity.RegistroTiempo
import com.example.android_app.databinding.ItemRegistroOperarioBinding

/**
 * Adaptador RecyclerView – patrón ViewHolder Cibertec Sesión 3.
 * Diseño basado en Stitch "Registro de Operarios (Optimized Vertical Tablet)"
 * Screen ID: d26ba961e2c54366b246aabb73724b0c
 *
 * Estados visuales:
 *  PENDIENTE  → Chip gris    #9E9E9E
 *  EN_PROCESO → Chip naranja #F57C00
 *  FINALIZADO → Chip verde   #388E3C
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
            // ── Datos del operario ────────────────────────
            tvNombreOperario.text = item.empleado.nombre

            // Avatar: iniciales (ej: "María Rodriguez" → "MR")
            tvIniciales.text = item.empleado.nombre
                .split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercaseChar().toString() }

            // Gafete + puesto en una línea (Stitch: "GAF-012 • Supervisor")
            tvGafete.text = buildString {
                append(item.empleado.gafete)
                if (item.empleado.puesto.isNotBlank()) {
                    append(" • ${item.empleado.puesto}")
                }
            }

            // ── Horas ────────────────────────────────────
            tvHoraEntrada.text = item.registro.horaEntrada ?: "--:--"
            tvHoraSalida.text  = item.registro.horaSalida  ?: "--:--"
            tvHorasTotales.text = item.registro.horasTotales
                ?.let { String.format("%.1f h", it) } ?: "--"

            // ── Chip de estado con color dinámico ────────
            val (colorRes, texto) = when (item.registro.estado) {
                "EN_PROCESO"  -> Pair(R.color.estado_en_proceso, "EN PROCESO")
                "FINALIZADO"  -> Pair(R.color.estado_finalizado, "FINALIZADO")
                else          -> Pair(R.color.estado_pendiente,  "PENDIENTE")
            }
            chipEstado.chipBackgroundColor =
                ColorStateList.valueOf(ContextCompat.getColor(root.context, colorRes))
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
