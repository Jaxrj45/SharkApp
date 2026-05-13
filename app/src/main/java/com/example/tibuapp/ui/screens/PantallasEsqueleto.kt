package com.example.tibuapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tibuapp.ui.navigation.Pantalla

// ESQUELETO — pantalla temporal de Login
@Composable
fun PantallaLoginEsqueleto(onLoginExitoso: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Papelería",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text("Ingresa tu PIN", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onLoginExitoso) {
            Text("Entrar (temporal)")
        }
    }
}

// ESQUELETO — menú principal temporal
@Composable
fun PantallaMenuEsqueleto(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Menú Principal", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate(Pantalla.Catalogo.ruta) },
            modifier = Modifier.fillMaxWidth()
        ) { Text(" Catálogo de Productos") }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) { Text("🛒 Nueva Venta") }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) { Text(" Historial") }
    }
}

// ESQUELETO — catálogo temporal
@Composable
fun PantallaCatalogoEsqueleto() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Aquí irá el catálogo de productos")
    }
}