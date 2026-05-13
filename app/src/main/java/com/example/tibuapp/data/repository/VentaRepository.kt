package com.example.tibuapp.data.repository

import com.example.tibuapp.data.local.dao.ProductoDao
import com.example.tibuapp.data.local.dao.VentaDao
import com.example.tibuapp.data.local.entity.DetalleVentaEntity
import com.example.tibuapp.data.local.entity.VentaEntity
import kotlinx.coroutines.flow.Flow


sealed class ResultadoVenta {
    object Exito : ResultadoVenta()
    data class StockInsuficiente(
        val nombreProducto: String,
        val stockDisponible: Int,
        val cantidadPedida: Int
    ) : ResultadoVenta()
    data class Error(val mensaje: String) : ResultadoVenta()
}

class VentaRepository(
    private val ventaDao: VentaDao,
    private val productoDao: ProductoDao
){

    fun obtenerTodas(): Flow<List<VentaEntity>> =
        ventaDao.obtenerTodas()

    fun obtenerPorEstado(estado: String): Flow<List<VentaEntity>> =
        ventaDao.obtenerPorEstado(estado)

    suspend fun obtenerDetalles(ventaId: Int): List<DetalleVentaEntity> =
        ventaDao.obtenerDetalles(ventaId)

    suspend fun procesarVenta(
        detalles: List<DetalleVentaEntity>,
        usuarioId: Int
    ): ResultadoVenta{
        for (detalle in detalles){
            val producto = productoDao.obtenerPorId(detalle.ProductoId)
                ?: return ResultadoVenta.Error("Producto ID ${detalle.ProductoId} no Encontrado")

            if(producto.stock < detalle.cantidad){
                return ResultadoVenta.StockInsuficiente(
                    producto.nombre,
                    producto.stock,
                    cantidadPedida = detalle.cantidad
                )
            }
        }

        return try {
            val total = detalles.sumOf { it.subtotal }
            val venta = VentaEntity(
                total = total,
                usuarioId = usuarioId
            )
            ventaDao.insertarVentaCompleta(venta,detalles)

            for(detalle in detalles){
                productoDao.reducirStock(detalle.ProductoId,detalle.cantidad)
            }
            ResultadoVenta.Exito
        }catch (e: Exception){
            ResultadoVenta.Error("Error al procesar la venta: ${e.message}")
        }
    }
}