package com.example.tibuapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ventas")
data class VentaEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    var fecha: Long = System.currentTimeMillis(),
    var total: Double,
    var usuarioId: Int,
    var estado: String = "COMPLETADA",
    var notas: String=""
)