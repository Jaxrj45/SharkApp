package com.example.tibuapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "detalle_ventas")
data class DetalleVentaEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int,

    val ventaId: Int,
    val ProductoId: Int,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double
)