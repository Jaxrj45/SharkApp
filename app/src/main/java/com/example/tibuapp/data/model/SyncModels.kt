package com.example.tibuapp.data.model

// Un producto tal como viene del Sheet
data class ProductoSheet(
    val codigoBarras: String,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val precioUnitario: Double,
    val precioMayor: Double,
    val cantidadMinimaMayor: Int,
    val stock: Int,
    val estado: String,
    val fechaAgregado: String,
    val fechaModificado: String,
    val notas: String
)

// El resultado de comparar Sheet vs Room
data class DiferenciaProducto(
    val productoSheet: ProductoSheet,
    val tipo: TipoDiferencia,
    val camposModificados: List<String> = emptyList()
)

enum class TipoDiferencia {
    NUEVO,          // Existe en Sheet pero no en Room
    MODIFICADO,     // Existe en ambos pero con diferencias
    SIN_CAMBIOS     // Igual en ambos lados
}

// Resultado de la sincronización completa
data class ResultadoSync(
    val nuevos: List<DiferenciaProducto>,
    val modificados: List<DiferenciaProducto>,
    val sinCambios: Int,
    val errores: List<String>,
    val fechaSync: Long = System.currentTimeMillis()
)