package com.example.tibuapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.tibuapp.data.local.entity.DetalleVentaEntity
import com.example.tibuapp.data.local.entity.VentaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VentaDao {

    @Query("SELECT * FROM ventas ORDER BY fecha DESC")
    fun obtenerTodas(): Flow<List<VentaEntity>>

    @Query("SELECT * FROM ventas WHERE estado= :estado ORDER BY fecha DESC")
    fun obtenerPorEstado(estado: String): Flow<List<VentaEntity>>

    @Insert
    suspend fun insertarVenta(venta: VentaEntity): Long

    @Insert
    suspend fun insertarDetalles(detalles: List<DetalleVentaEntity>)

    @Query("SELECT * FROM detalle_ventas WHERE ventaId = :ventaId")
    suspend fun obtenerDetalles(ventaId: Int): List<DetalleVentaEntity>

    @Transaction
    suspend fun insertarVentaCompleta(
        venta: VentaEntity,
        detalles: List<DetalleVentaEntity>
    ){
        val ventaId = insertarVenta(venta)
        val detallesConId = detalles.map { it.copy(ventaId = ventaId.toInt()) }
        insertarDetalles(detallesConId)
    }
}