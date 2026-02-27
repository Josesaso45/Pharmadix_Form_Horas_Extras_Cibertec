package com.example.android_app.data.repository

import com.example.android_app.data.local.dao.EmpleadoDao
import com.example.android_app.data.local.dao.HojaTiempoDao
import com.example.android_app.data.local.dao.RegistroTiempoDao
import com.example.android_app.data.local.entity.Empleado
import com.example.android_app.data.local.entity.HojaTiempo
import com.example.android_app.data.local.entity.RegistroTiempo
import com.example.android_app.data.remote.RetrofitClient
import com.example.android_app.data.remote.model.RegistroSalidaRequest
import com.example.android_app.data.remote.model.RegistroTiempoRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Repositorio que unifica Room (local) y Retrofit (remoto).
 * Patrón Repository recomendado por la arquitectura MVVM de Cibertec.
 */
class RegistroRepository(
    private val empleadoDao: EmpleadoDao,
    private val hojaTiempoDao: HojaTiempoDao,
    private val registroTiempoDao: RegistroTiempoDao
) {

    fun obtenerRegistrosPorHoja(hojaId: Int) =
        registroTiempoDao.obtenerPorHoja(hojaId)

    suspend fun buscarEmpleadoPorGafete(gafete: String): Empleado? =
        withContext(Dispatchers.IO) { empleadoDao.obtenerPorGafete(gafete) }

    suspend fun buscarHoja(hojaId: Int): HojaTiempo? =
        withContext(Dispatchers.IO) { hojaTiempoDao.obtenerPorId(hojaId) }

    /**
     * Registra la entrada de un operario.
     * 1) Guarda localmente en Room (offline-first)
     * 2) Intenta sincronizar con el backend
     */
    suspend fun registrarEntrada(
        hojaId: Int,
        empleado: Empleado,
        actividad: String
    ): Result<RegistroTiempo> = withContext(Dispatchers.IO) {
        val horaNow = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

        // 1. Verificar si ya existe un registro en esta hoja para este operario
        val existente = registroTiempoDao.obtenerPorEmpleadoYHoja(empleado.id, hojaId)
        if (existente != null && existente.estado != "PENDIENTE") {
            return@withContext Result.failure(
                IllegalStateException("El operario ya tiene un registro activo: ${existente.estado}")
            )
        }

        // 2. Guardar localmente
        val registro = RegistroTiempo(
            hojaId = hojaId,
            empleadoId = empleado.id,
            actividad = actividad,
            horaEntrada = horaNow,
            estado = "EN_PROCESO"
        )
        val localId = registroTiempoDao.insertar(registro).toInt()
        val registroGuardado = registro.copy(id = localId)

        // 3. Sincronizar con backend en segundo plano
        try {
            val request = RegistroTiempoRequest(
                hojaId = hojaId,
                empleadoId = empleado.id,
                actividad = actividad,
                horaEntrada = horaNow
            )
            RetrofitClient.apiService.registrarEntrada(request)
        } catch (_: Exception) {
            // Offline: el registro local ya fue guardado
        }

        Result.success(registroGuardado)
    }

    /**
     * Registra la salida de un operario y calcula las horas totales.
     * 1) Actualiza Room localmente
     * 2) Intenta sincronizar con el backend
     */
    suspend fun registrarSalida(registro: RegistroTiempo): Result<RegistroTiempo> =
        withContext(Dispatchers.IO) {
            val horaFmt = DateTimeFormatter.ofPattern("HH:mm")
            val horaSalida = LocalTime.now().format(horaFmt)
            val horasTotales = calcularHoras(registro.horaEntrada ?: "00:00", horaSalida)

            // 1. Actualizar Room
            registroTiempoDao.marcarSalida(registro.id, horaSalida, horasTotales)
            val registroActualizado = registro.copy(
                horaSalida = horaSalida,
                horasTotales = horasTotales,
                estado = "FINALIZADO"
            )

            // 2. Sincronizar con backend
            try {
                RetrofitClient.apiService.registrarSalida(
                    registro.id,
                    RegistroSalidaRequest(horaSalida)
                )
            } catch (_: Exception) {
                // Offline: la actualización local ya fue guardada
            }

            Result.success(registroActualizado)
        }

    private fun calcularHoras(entrada: String, salida: String): Double {
        return try {
            val fmt = DateTimeFormatter.ofPattern("HH:mm")
            val tIn = LocalTime.parse(entrada, fmt)
            val tOut = LocalTime.parse(salida, fmt)
            val mins = if (tOut.isAfter(tIn)) {
                java.time.Duration.between(tIn, tOut).toMinutes()
            } else {
                // turno nocturno: cruzó medianoche
                java.time.Duration.between(tIn, tOut).plusHours(24).toMinutes()
            }
            String.format("%.2f", mins / 60.0).toDouble()
        } catch (_: Exception) {
            0.0
        }
    }
}
