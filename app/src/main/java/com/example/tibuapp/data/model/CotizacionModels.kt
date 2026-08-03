package com.example.tibuapp.data.model

import com.example.tibuapp.data.local.entity.ProductoEntity

// Una línea extraída del OCR
data class LineaOCR(
    val id: Int,
    val textoOriginal: String,          // Lo que leyó el OCR
    val textoEditado: String,           // Lo que corrigió el empleado
    val productoEncontrado: ProductoEntity? = null,  // Match en inventario
    val cantidad: Int = 1,
    val encontrado: Boolean = false,
    val ignorar: Boolean = false        // El empleado puede ignorar líneas
)

// La cotización completa
data class Cotizacion(
    val lineas: List<LineaOCR>,
    val fecha: Long = System.currentTimeMillis()
) {
    val lineasValidas get() = lineas.filter { it.encontrado && !it.ignorar }
    val total get() = lineasValidas.sumOf {
        (it.productoEncontrado?.precio ?: 0.0) * it.cantidad
    }
    val articulosNoEncontrados get() = lineas.filter { !it.encontrado && !it.ignorar }
}