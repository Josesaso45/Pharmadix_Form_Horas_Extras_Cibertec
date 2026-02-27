package com.example.android_app.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android_app.data.local.entity.Empleado

@Dao
interface EmpleadoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(empleado: Empleado)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(empleados: List<Empleado>)

    @Update
    suspend fun actualizar(empleado: Empleado)

    @Delete
    suspend fun eliminar(empleado: Empleado)

    @Query("SELECT * FROM empleados ORDER BY nombre ASC")
    fun obtenerTodos(): LiveData<List<Empleado>>

    @Query("SELECT * FROM empleados WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Empleado?

    @Query("SELECT * FROM empleados WHERE gafete = :gafete LIMIT 1")
    suspend fun obtenerPorGafete(gafete: String): Empleado?

    @Query("SELECT * FROM empleados WHERE activo = 1 ORDER BY nombre ASC")
    fun obtenerActivos(): LiveData<List<Empleado>>
}
