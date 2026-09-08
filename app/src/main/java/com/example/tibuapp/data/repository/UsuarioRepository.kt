package com.example.tibuapp.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.tibuapp.data.local.dao.UsuarioDao
import com.example.tibuapp.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(private  val usuarioDao: UsuarioDao) {

    fun obtenerTodos(): Flow<List<UsuarioEntity>> =
        usuarioDao.obtenerTodos()

    suspend fun validarPin(pin: String): UsuarioEntity? =
        usuarioDao.buscarPorPin(pin)

    suspend fun insertar(usuario: UsuarioEntity): Result<Long> {
        return try {
            Result.success(usuarioDao.insertar(usuario))
        } catch (e: SQLiteConstraintException) {
            Result.failure(Exception("Este PIN ya está en uso, elige otro"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizar(usuario: UsuarioEntity) =
        usuarioDao.actualizar(usuario)


}