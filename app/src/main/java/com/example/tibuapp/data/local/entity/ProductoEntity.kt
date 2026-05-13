package com.example.tibuapp.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class ProductoEntity (

    @PrimaryKey(autoGenerate = true)
    val id: Int =0,

    val nombre: String,
    val descripcion: String= "",
    val precio: Double,
    val  stock: Int,
    val codigoBarras: String="",
    val categoria: String ="",
    val imagenUrl: String ="",
    val activo: Boolean = true
)