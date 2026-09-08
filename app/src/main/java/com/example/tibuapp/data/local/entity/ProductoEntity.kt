package com.example.tibuapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val descripcion: String = "",
    val precio: Double,
    val precioMayor: Double = 0.0,
    val cantidadMinimaMayor: Int = 0,
    val stock: Int,
    val codigoBarras: String = "",
    val categoria: String = "",
    val imagenUrl: String = "",
    val activo: Boolean = true,
    val favorito: Boolean = false,
    val fechaAgregado: Long = System.currentTimeMillis(),
    val fechaModificado: Long = System.currentTimeMillis(),
    val notas: String = ""
)