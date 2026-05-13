package com.example.tibuapp.ui.screens.catalogo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tibuapp.data.local.entity.ProductoEntity
import com.example.tibuapp.ui.screens.Catalogo.CatalogoViewModel

@Composable
fun PantallaCatalogoScreen (viewModel: CatalogoViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Catálogo de productos",
            style = MaterialTheme.typography.headlineSmall

        )

        Text(
            text = "Productos cargados: ${uiState.productos.size}",
            modifier = Modifier.padding(top = 8.dp)
        )

        OutlinedTextField(
            value = uiState.textoBusqueda,
            onValueChange = { texto ->
                viewModel.cambiarTextoBusqueda(texto)
            },
            label = { Text("Buscar producto") },
            modifier = Modifier.padding(top = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.padding(top = 16.dp)
        ) {
            items(uiState.productos) { producto ->
                ProductoItem(producto)
            }
        }
    }
}

@Composable
fun ProductoItem(producto: ProductoEntity) {
    Card(
        modifier = Modifier
            .padding(vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(text = producto.nombre)
            Text(text = "Precio: Q${producto.precio}")
            Text(text = "Stock: ${producto.stock}")
        }
    }
}