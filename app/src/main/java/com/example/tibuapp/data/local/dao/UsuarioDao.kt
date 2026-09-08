package com.example.tibuapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tibuapp.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios WHERE activo =1")
    fun obtenerTodos(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE pin=:pin AND activo=1 LIMIT 1")
    suspend fun buscarPorPin(pin: String): UsuarioEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Update
    suspend fun actualizar(usuario: UsuarioEntity)

}