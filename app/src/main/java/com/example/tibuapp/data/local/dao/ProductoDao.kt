package com.example.tibuapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tibuapp.data.local.entity.ProductoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    @Query("SELECT * FROM productos WHERE activo =1 AND favorito= 1 ORDER BY nombre ASC ")
    fun obtenerFavoritos(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE activo =1  ORDER BY nombre ASC ")
    fun obtenerTodos(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE categoria=:categoria and activo=1")
    fun buscarPorCategoria(categoria: String): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE id=:id")
    suspend fun obtenerPorId(id: Int): ProductoEntity?

    @Query("SELECT * FROM productos WHERE codigoBarras = :codigo LIMIT 1")
    suspend fun obtenerPorCodigoBarras(codigo: String): ProductoEntity?

    @Query("SELECT * FROM productos WHERE nombre LIKE '%' || :busqueda || '%' AND activo=1")
    fun buscar(busqueda: String): Flow<List<ProductoEntity>>?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(producto: ProductoEntity): Long

    @Update
    suspend fun actualizar(producto: ProductoEntity)

    @Query("UPDATE productos SET activo=0 WHERE id=:id")
    suspend fun desactivar(id: Int)

    @Query("UPDATE productos SET favorito=1 WHERE id=:id")
    suspend fun favorito(id: Int)

    @Query("UPDATE productos SET stock = stock - :cantidad WHERE id = :id")
    suspend fun reducirStock(id: Int, cantidad: Int)
}