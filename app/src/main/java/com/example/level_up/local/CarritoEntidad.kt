package cl.levelup.mobile.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carrito")
data class CarritoEntidad(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productoId: Int,
    val nombre: String,
    val precio: Int,
    val cantidad: Int
)
