package com.example.tibuapp.ui.scanner

import com.example.tibuapp.data.local.entity.ProductoEntity

data class EscanerUiState(
    val escaneando: Boolean= true,
    val codigoDetectado: String = "",
    val productoEncontrado: ProductoEntity? = null,
    val noEncontrado: Boolean = false,
    val cargando: Boolean = false
)