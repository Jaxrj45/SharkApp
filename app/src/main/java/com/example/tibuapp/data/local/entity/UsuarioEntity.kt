package com.example.tibuapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios",
        indices = [Index(value = ["pin"], unique = true)]
    )
data class UsuarioEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int=0,

    val nombre: String,
    val pin: String,
    val rol: String,
    val activo: Boolean = true

)