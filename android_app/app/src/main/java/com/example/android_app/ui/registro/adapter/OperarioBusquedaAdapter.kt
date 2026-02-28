package com.example.android_app.ui.registro.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.android_app.data.local.entity.Empleado
import com.example.android_app.databinding.ItemOperarioBusquedaBinding

class OperarioBusquedaAdapter(
    private val onClick: (Empleado) -> Unit
) : RecyclerView.Adapter<OperarioBusquedaAdapter.BusquedaViewHolder>() {

    private var empleados: List<Empleado> = emptyList()

    fun actualizarLista(nuevosEmpleados: List<Empleado>) {
        empleados = nuevosEmpleados
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusquedaViewHolder {
        val binding = ItemOperarioBusquedaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BusquedaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BusquedaViewHolder, position: Int) {
        holder.bind(empleados[position], onClick)
    }

    override fun getItemCount(): Int = empleados.size

    class BusquedaViewHolder(private val binding: ItemOperarioBusquedaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(empleado: Empleado, onClick: (Empleado) -> Unit) {
            binding.tvNombreBusqueda.text = empleado.nombre
            binding.tvGafeteBusqueda.text = empleado.gafete

            binding.root.setOnClickListener {
                onClick(empleado)
            }
        }
    }
}
