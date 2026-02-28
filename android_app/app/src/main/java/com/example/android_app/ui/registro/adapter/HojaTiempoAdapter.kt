package com.example.android_app.ui.registro.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.android_app.data.local.entity.HojaTiempo
import com.example.android_app.databinding.ItemHojaTiempoBinding

class HojaTiempoAdapter(
    private val onClick: (HojaTiempo) -> Unit
) : RecyclerView.Adapter<HojaTiempoAdapter.HojaViewHolder>() {

    private var hojas: List<HojaTiempo> = emptyList()

    fun actualizarLista(nuevasHojas: List<HojaTiempo>) {
        hojas = nuevasHojas
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HojaViewHolder {
        val binding = ItemHojaTiempoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HojaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HojaViewHolder, position: Int) {
        holder.bind(hojas[position], onClick)
    }

    override fun getItemCount(): Int = hojas.size

    class HojaViewHolder(private val binding: ItemHojaTiempoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(hoja: HojaTiempo, onClick: (HojaTiempo) -> Unit) {
            binding.tvHojaId.text = hoja.numeroHoja
            binding.tvProducto.text = "Lote ID: ${hoja.loteId ?: "N/A"}"
            binding.badgeEstado.text = hoja.estado.uppercase()
            binding.badgeTurno.text = hoja.turno.uppercase()
            
            // Colores del estado según "estado"
            if (hoja.estado.equals("ABIERTA", ignoreCase = true) || hoja.estado.equals("EN_PROCESO", ignoreCase = true)) {
                binding.badgeEstado.setTextColor(binding.root.context.getColor(com.example.android_app.R.color.pharmadix_green))
            } else {
                binding.badgeEstado.setTextColor(binding.root.context.getColor(com.example.android_app.R.color.text_secondary))
            }

            binding.root.setOnClickListener {
                onClick(hoja)
            }
        }
    }
}
