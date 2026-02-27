package com.example.android_app.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android_app.data.local.entity.RegistroTiempo

@Dao
interface RegistroTiempoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(registro: RegistroTiempo): Long

    @Update
    suspend fun actualizar(registro: RegistroTiempo)

    @Delete
    suspend fun eliminar(registro: RegistroTiempo)

    @Query("SELECT * FROM registros_tiempo WHERE hojaId = :hojaId ORDER BY id ASC")
    fun obtenerPorHoja(hojaId: Int): LiveData<List<RegistroTiempo>>

    @Query("SELECT * FROM registros_tiempo WHERE id = :id")
    suspend fun obtenerPorId(id: Int): RegistroTiempo?

    @Query("SELECT * FROM registros_tiempo WHERE empleadoId = :empleadoId AND hojaId = :hojaId LIMIT 1")
    suspend fun obtenerPorEmpleadoYHoja(empleadoId: Int, hojaId: Int): RegistroTiempo?

    @Query("UPDATE registros_tiempo SET horaEntrada = :hora, estado = 'EN_PROCESO' WHERE id = :id")
    suspend fun marcarEntrada(id: Int, hora: String)

    @Query("UPDATE registros_tiempo SET horaSalida = :hora, horasTotales = :horas, estado = 'FINALIZADO' WHERE id = :id")
    suspend fun marcarSalida(id: Int, hora: String, horas: Double)

    @Query("SELECT COUNT(*) FROM registros_tiempo WHERE hojaId = :hojaId AND estado = 'EN_PROCESO'")
    suspend fun contarEnProceso(hojaId: Int): Int
}
