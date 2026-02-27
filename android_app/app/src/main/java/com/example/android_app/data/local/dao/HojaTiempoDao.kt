package com.example.android_app.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android_app.data.local.entity.HojaTiempo

@Dao
interface HojaTiempoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(hoja: HojaTiempo): Long

    @Update
    suspend fun actualizar(hoja: HojaTiempo)

    @Delete
    suspend fun eliminar(hoja: HojaTiempo)

    @Query("SELECT * FROM hojas_tiempo ORDER BY fechaEmision DESC")
    fun obtenerTodas(): LiveData<List<HojaTiempo>>

    @Query("SELECT * FROM hojas_tiempo WHERE id = :id")
    suspend fun obtenerPorId(id: Int): HojaTiempo?

    @Query("SELECT * FROM hojas_tiempo WHERE tomadorId = :tomadorId ORDER BY fechaEmision DESC")
    fun obtenerPorTomador(tomadorId: Int): LiveData<List<HojaTiempo>>

    @Query("SELECT * FROM hojas_tiempo WHERE sincronizada = 0")
    suspend fun obtenerPendientesSincronizacion(): List<HojaTiempo>

    @Query("UPDATE hojas_tiempo SET sincronizada = 1 WHERE id = :id")
    suspend fun marcarComoSincronizada(id: Int)
}
