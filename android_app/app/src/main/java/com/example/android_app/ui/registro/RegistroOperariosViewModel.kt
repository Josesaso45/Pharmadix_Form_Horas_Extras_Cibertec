package com.example.android_app.ui.registro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.android_app.data.local.PharmadixDatabase
import com.example.android_app.data.local.entity.Empleado
import com.example.android_app.data.local.entity.RegistroTiempo
import com.example.android_app.data.repository.RegistroRepository
import com.example.android_app.ui.registro.adapter.RegistroOperarioAdapter
import kotlinx.coroutines.launch

/**
 * ViewModel para RegistroOperariosFragment.
 * Expone el estado de la pantalla mediante LiveData (patrón MVVM Cibertec).
 */
class RegistroOperariosViewModel(application: Application) : AndroidViewModel(application) {

    // ── Repositorio (creado con los DAOs de Room) ─────────────────────────
    private val db = PharmadixDatabase.getInstance(application)
    private val repository = RegistroRepository(
        db.empleadoDao(),
        db.hojaTiempoDao(),
        db.registroTiempoDao()
    )

    // ── Estado de la pantalla ─────────────────────────────────────────────
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val mensaje: String) : UiState()
        data class Error(val mensaje: String) : UiState()
    }

    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> = _uiState

    // ── Catálogo de empleados (para búsqueda manual) ────────────────────
    val empleadosCatalogo: LiveData<List<Empleado>> = db.empleadoDao().obtenerActivos()

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val count = db.empleadoDao().obtenerTodosSincrono().size
            if (count == 0) {
                db.empleadoDao().insertarAll(listOf(
                    Empleado(1, "Juan Carlos Pérez", "EMP-1234", "Envasado", "Operario de Envasado", "ACTIVO"),
                    Empleado(2, "María Elena López", "EMP-5678", "Etiquetado", "Operaria de Etiquetado", "ACTIVO"),
                    Empleado(3, "Carlos Alberto García", "EMP-9012", "Empaque", "Operario de Empaque", "ACTIVO"),
                    Empleado(4, "Ana Patricia Martínez", "EMP-3456", "Calidad", "Operaria de Control de Calidad", "ACTIVO"),
                    Empleado(5, "Roberto Hernández", "EMP-7890", "Limpieza", "Operario de Limpieza", "ACTIVO")
                ))
            }
        }
    }

    // ── Lista de Hojas de Tiempo ──────────────────────────────────────────
    val hojasActivas: LiveData<List<com.example.android_app.data.local.entity.HojaTiempo>> = db.hojaTiempoDao().obtenerTodas()

    // ── Hoja de tiempo activa ─────────────────────────────────────────────
    private val _hojaId = MutableLiveData<Int>(0)

    fun crearHojaVacia(hojaId: Int, numero: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val nuevaHoja = com.example.android_app.data.local.entity.HojaTiempo(
                id = hojaId,
                numeroHoja = numero,
                loteId = null,
                tomadorId = 1, // Simulando usuario logueado
                fechaEmision = "2024-11-20T10:00:00Z",
                turno = "DIA",
                estado = "ABIERTA"
            )
            db.hojaTiempoDao().insertar(nuevaHoja)
            
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onComplete(hojaId)
            }
        }
    }

    // Registros de la hoja activa (observa Room en tiempo real)
    // Se combina con la info de empleados para el Adapter
    fun obtenerRegistrosConEmpleado(hojaId: Int): LiveData<List<RegistroOperarioAdapter.RegistroConEmpleado>> {
        val ld = MutableLiveData<List<RegistroOperarioAdapter.RegistroConEmpleado>>()
        
        // Observamos los registros y cuando cambian, buscamos sus empleados
        repository.obtenerRegistrosPorHoja(hojaId).observeForever { registros ->
            viewModelScope.launch {
                val empleados = db.empleadoDao().obtenerTodosSincrono()
                val map = empleados.associateBy { it.id }
                val listaCombinada = registros.mapNotNull { reg ->
                    map[reg.empleadoId]?.let { emp ->
                        RegistroOperarioAdapter.RegistroConEmpleado(reg, emp)
                    }
                }
                ld.postValue(listaCombinada)
            }
        }
        return ld
    }

    // ── Buscar operario por gafete (QR) ───────────────────────────────────
    fun procesarQr(gafete: String, hojaId: Int, actividad: String = "Operación General") {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val empleado = repository.buscarEmpleadoPorGafete(gafete)
            if (empleado == null) {
                _uiState.value = UiState.Error("Operario no encontrado: gafete $gafete")
                return@launch
            }
            val registroExistente = db.registroTiempoDao().obtenerPorEmpleadoYHoja(empleado.id, hojaId)
            when (registroExistente?.estado) {
                "EN_PROCESO" -> _uiState.value = UiState.Success("accion_salida:${registroExistente.id}:${empleado.nombre}")
                "FINALIZADO" -> _uiState.value = UiState.Error("${empleado.nombre} ya finalizó su turno")
                else -> _uiState.value = UiState.Success("accion_entrada:${empleado.id}:${empleado.nombre}:$actividad")
            }
        }
    }

    // ── Registrar Entrada ─────────────────────────────────────────────────
    fun registrarEntrada(hojaId: Int, empleado: Empleado, actividad: String = "Operación General") {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val resultado = repository.registrarEntrada(hojaId, empleado, actividad)
            _uiState.value = if (resultado.isSuccess)
                UiState.Success("Entrada registrada: ${empleado.nombre}")
            else
                UiState.Error(resultado.exceptionOrNull()?.message ?: "Error al registrar entrada")
        }
    }

    // ── Registrar Salida ──────────────────────────────────────────────────
    fun registrarSalida(registro: RegistroTiempo) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val resultado = repository.registrarSalida(registro)
            _uiState.value = if (resultado.isSuccess) {
                val r = resultado.getOrThrow()
                UiState.Success("Salida registrada. Total: ${r.horasTotales} h")
            } else {
                UiState.Error(resultado.exceptionOrNull()?.message ?: "Error al registrar salida")
            }
        }
    }

    // ── Buscar empleado por ID (para el diálogo) ──────────────────────────
    suspend fun buscarEmpleado(empleadoId: Int): Empleado? =
        db.empleadoDao().obtenerPorId(empleadoId)

    fun resetEstado() { _uiState.value = UiState.Idle }
}
