package com.example.tibuapp.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.tibuapp.data.local.entity.ProductoEntity
import com.example.tibuapp.data.model.DiferenciaProducto
import com.example.tibuapp.data.model.ProductoSheet
import com.example.tibuapp.data.model.ResultadoSync
import com.example.tibuapp.data.model.TipoDiferencia
import com.example.tibuapp.data.sync.GoogleSheetsService
import kotlinx.coroutines.flow.first

class SyncRepository(
    private val context: Context,
    private val productoRepository: ProductRepository,
    private val sheetsService: GoogleSheetsService
) {

    fun hayInternet(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun sincronizar(): Result<ResultadoSync> {
        if (!hayInternet()) {
            return Result.failure(Exception("Sin conexión a internet"))
        }

        // 1. Leer productos del Sheet
        val resultadoSheet = sheetsService.leerProductos()
        if (resultadoSheet.isFailure) {
            return Result.failure(resultadoSheet.exceptionOrNull()!!)
        }
        val productosSheet = resultadoSheet.getOrNull()!!

        // 2. Leer productos locales
        val productosLocales = productoRepository.obtenerTodos().first()

        // 3. Comparar
        val nuevos = mutableListOf<DiferenciaProducto>()
        val modificados = mutableListOf<DiferenciaProducto>()
        val errores = mutableListOf<String>()
        var sinCambios = 0

        for (productoSheet in productosSheet) {
            try {
                val local = productosLocales.find { local ->
                    // Buscar por código de barras primero, luego por nombre
                    (productoSheet.codigoBarras.isNotBlank() &&
                            local.codigoBarras == productoSheet.codigoBarras) ||
                            local.nombre.equals(productoSheet.nombre, ignoreCase = true)
                }

                if (local == null) {
                    nuevos.add(DiferenciaProducto(
                        productoSheet = productoSheet,
                        tipo = TipoDiferencia.NUEVO
                    ))
                } else {
                    val cambios = detectarCambios(local, productoSheet)
                    if (cambios.isEmpty()) {
                        sinCambios++
                    } else {
                        modificados.add(DiferenciaProducto(
                            productoSheet = productoSheet,
                            tipo = TipoDiferencia.MODIFICADO,
                            camposModificados = cambios
                        ))
                    }
                }
            } catch (e: Exception) {
                errores.add("Error en '${productoSheet.nombre}': ${e.message}")
            }
        }

        return Result.success(
            ResultadoSync(
                nuevos = nuevos,
                modificados = modificados,
                sinCambios = sinCambios,
                errores = errores
            )
        )
    }

    private fun detectarCambios(
        local: ProductoEntity,
        sheet: ProductoSheet
    ): List<String> {
        val cambios = mutableListOf<String>()
        if (local.precio != sheet.precioUnitario) cambios.add("Precio unitario")
        if (local.precioMayor != sheet.precioMayor) cambios.add("Precio mayor")
        if (local.cantidadMinimaMayor != sheet.cantidadMinimaMayor) cambios.add("Cantidad mínima mayor")
        if (local.stock != sheet.stock) cambios.add("Stock")
        if (local.descripcion != sheet.descripcion) cambios.add("Descripción")
        if (local.categoria != sheet.categoria) cambios.add("Categoría")
        return cambios
    }

    suspend fun aplicarNuevos(productos: List<DiferenciaProducto>) {
        for (diff in productos) {
            val entidad = sheetAEntidad(diff.productoSheet)
            productoRepository.insertar(entidad)
        }
    }

    suspend fun aplicarModificados(
        productos: List<DiferenciaProducto>,
        productosLocales: List<ProductoEntity>
    ) {
        for (diff in productos) {
            val local = productosLocales.find { local ->
                (diff.productoSheet.codigoBarras.isNotBlank() &&
                        local.codigoBarras == diff.productoSheet.codigoBarras) ||
                        local.nombre.equals(diff.productoSheet.nombre, ignoreCase = true)
            } ?: continue

            val actualizado = local.copy(
                nombre = diff.productoSheet.nombre,
                descripcion = diff.productoSheet.descripcion,
                categoria = diff.productoSheet.categoria,
                precio = diff.productoSheet.precioUnitario,
                precioMayor = diff.productoSheet.precioMayor,
                cantidadMinimaMayor = diff.productoSheet.cantidadMinimaMayor,
                stock = diff.productoSheet.stock,
                notas = diff.productoSheet.notas,
                fechaModificado = System.currentTimeMillis()
            )
            productoRepository.actualizar(actualizado)
        }
    }

    private fun sheetAEntidad(sheet: ProductoSheet): ProductoEntity {
        return ProductoEntity(
            nombre = sheet.nombre,
            descripcion = sheet.descripcion,
            categoria = sheet.categoria,
            precio = sheet.precioUnitario,
            precioMayor = sheet.precioMayor,
            cantidadMinimaMayor = sheet.cantidadMinimaMayor,
            stock = sheet.stock,
            codigoBarras = sheet.codigoBarras,
            notas = sheet.notas,
            activo = sheet.estado.equals("Activo", ignoreCase = true)
        )
    }
}