package com.example.android_app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.android_app.data.local.dao.EmpleadoDao
import com.example.android_app.data.local.dao.HojaTiempoDao
import com.example.android_app.data.local.dao.RegistroTiempoDao
import com.example.android_app.data.local.entity.*

@Database(
    entities = [Empleado::class, Lote::class, HojaTiempo::class, RegistroTiempo::class, Usuario::class],
    version = 1,
    exportSchema = false
)
abstract class PharmadixDatabase : RoomDatabase() {

    abstract fun empleadoDao(): EmpleadoDao
    abstract fun hojaTiempoDao(): HojaTiempoDao
    abstract fun registroTiempoDao(): RegistroTiempoDao

    companion object {
        @Volatile
        private var INSTANCE: PharmadixDatabase? = null

        fun getInstance(context: Context): PharmadixDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PharmadixDatabase::class.java,
                    "pharmadix_times.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
