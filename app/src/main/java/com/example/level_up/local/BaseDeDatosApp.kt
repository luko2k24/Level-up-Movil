package com.example.level_up.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase



@Database(
    entities = [
        ProductoEntidad::class,
        UsuarioEntidad::class,
        CarritoEntidad::class,
        ReseniaEntidad::class,
        PedidoEntidad::class
    ],
    version = 5, // <-- ¡VERSIÓN FINAL NECESARIA PARA INCLUIR EL CAMPO 'ROL'!
    exportSchema = false
)
abstract class BaseDeDatosApp : RoomDatabase() {

    abstract fun ProductoDao(): ProductoDao
    abstract fun UsuarioDao(): UsuarioDao
    abstract fun CarritoDao():CarritoDao
    abstract fun ReseniaDao(): ReseniaDao
    abstract fun PedidoDao(): PedidoDao

    companion object {
        @Volatile private var INSTANCIA: BaseDeDatosApp? = null

        fun obtener(contexto: Context): BaseDeDatosApp =
            INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    contexto.applicationContext,
                    BaseDeDatosApp::class.java,
                    "levelup.db"
                )
                    .fallbackToDestructiveMigration()  // <-- Fuerza la eliminación de la DB corrupta y crea el nuevo esquema (con el campo 'rol')
                    .build()
                    .also { INSTANCIA = it }
            }
    }
}