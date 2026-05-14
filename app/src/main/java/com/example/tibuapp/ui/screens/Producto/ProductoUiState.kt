package com.example.tibuapp.ui.screens.Producto

import com.example.tibuapp.data.local.entity.ProductoEntity

data class ProductoUiState(

    val productos: List<ProductoEntity> = emptyList(),
    val busqueda : String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val guardando: Boolean = false,
    val mensajeExito: String? = null

)