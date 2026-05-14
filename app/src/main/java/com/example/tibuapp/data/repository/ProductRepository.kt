package com.example.tibuapp.data.repository

import com.example.tibuapp.data.local.dao.ProductoDao
import com.example.tibuapp.data.local.entity.ProductoEntity
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productoDao: ProductoDao) {

    fun obtenerTodos(): Flow<List<ProductoEntity>> =
        productoDao.obtenerTodos()
    fun obtenerFavoritos(): Flow<List<ProductoEntity>> =
        productoDao.obtenerFavoritos()


    fun buscarPorCategoria(categoria: String): Flow<List<ProductoEntity>> =
        productoDao.buscarPorCategoria(categoria)

    fun buscar(texto: String): Flow<List<ProductoEntity>>? =
        productoDao.buscar(texto)

    suspend fun obtenerPorId(id: Int): ProductoEntity? =
        productoDao.obtenerPorId(id)

    suspend fun obtenerPorCodigoBarras(codigo: String): ProductoEntity? =
        productoDao.obtenerPorCodigoBarras(codigo)

    suspend fun insertar(producto: ProductoEntity): Result<Long> {
        return try{
            Result.success(productoDao.insertar(producto))
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    suspend fun actualizar(producto: ProductoEntity) =
        productoDao.actualizar(producto)

    suspend fun desactivar(id: Int) =
        productoDao.desactivar(id)

    suspend fun favorito(id: Int) =
        productoDao.favorito(id)

    suspend fun reducirStock(id: Int, cantidad: Int)=
        productoDao.reducirStock(id,cantidad)

}