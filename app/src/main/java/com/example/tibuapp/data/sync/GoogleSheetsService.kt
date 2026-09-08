package com.example.tibuapp.data.sync

import android.content.Context
import com.example.tibuapp.data.model.ProductoSheet
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleSheetsService(private val context: Context) {

    companion object {
        // ID de Google Sheet
        const val SHEET_ID = "1tY-v_A5KzyEwmLDQVpwSBqC7Fmw15ohlneEHm_xaW_k"
        const val RANGO = "Sheet1!A2:L1000" // Desde fila 2 para no jalar encabezados
    }

    private fun crearServicio(): Sheets {
        // Carga las credenciales desde assets
        val credencialesStream = context.assets.open("service_account.json")
        val credenciales = GoogleCredentials
            .fromStream(credencialesStream)
            .createScoped(listOf(SheetsScopes.SPREADSHEETS_READONLY))

        return Sheets.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            HttpCredentialsAdapter(credenciales)
        )
            .setApplicationName("TiburoncitosApp")
            .build()
    }

    suspend fun leerProductos(): Result<List<ProductoSheet>> = withContext(Dispatchers.IO) {
        try {
            val servicio = crearServicio()
            val respuesta = servicio.spreadsheets().values()
                .get(SHEET_ID, RANGO)
                .execute()

            val filas = respuesta.getValues() ?: emptyList()
            val productos = filas.mapNotNull { fila ->
                parsearFila(fila)
            }.filter { it.estado.equals("Activo", ignoreCase = true) }

            Result.success(productos)
        } catch (e: Exception) {
            Result.failure(Exception("Error al leer Sheet: ${e.message}"))
        }
    }

    private fun parsearFila(fila: List<Any>): ProductoSheet? {
        return try {
            // Necesitamos al menos nombre y precio
            if (fila.size < 5) return null
            val nombre = fila.getOrNull(1)?.toString()?.trim() ?: return null
            if (nombre.isBlank()) return null

            ProductoSheet(
                codigoBarras = fila.getOrNull(0)?.toString()?.trim() ?: "",
                nombre = nombre,
                descripcion = fila.getOrNull(2)?.toString()?.trim() ?: "",
                categoria = fila.getOrNull(3)?.toString()?.trim() ?: "",
                precioUnitario = fila.getOrNull(4)?.toString()?.toDoubleOrNull() ?: 0.0,
                precioMayor = fila.getOrNull(5)?.toString()?.toDoubleOrNull() ?: 0.0,
                cantidadMinimaMayor = fila.getOrNull(6)?.toString()?.toIntOrNull() ?: 0,
                stock = fila.getOrNull(7)?.toString()?.toIntOrNull() ?: 0,
                estado = fila.getOrNull(8)?.toString()?.trim() ?: "Activo",
                fechaAgregado = fila.getOrNull(9)?.toString()?.trim() ?: "",
                fechaModificado = fila.getOrNull(10)?.toString()?.trim() ?: "",
                notas = fila.getOrNull(11)?.toString()?.trim() ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }
}