package com.example.tibuapp.ui.screens.Catalogo

import com.example.tibuapp.data.local.entity.ProductoEntity

data class CatalogoUiState (
    val productos: List<ProductoEntity> = emptyList(),
    val textoBusqueda: String = "",
    val estaCargando: Boolean = false,
    val mensajeError: String? = null
    )