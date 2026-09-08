package com.example.tibuapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tibuapp.ui.components.PapeleriaScaffold
import com.example.tibuapp.ui.navigation.Pantalla
import com.example.tibuapp.ui.viewmodel.AsistenteViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.tibuapp.ui.components.TarjetaMenu
import com.example.tibuapp.ui.components.TibuBoton
import com.example.tibuapp.ui.components.TibuBotonSecundario
import com.example.tibuapp.ui.theme.*

// ESQUELETO — pantalla temporal de Login
@Composable
fun PantallaLoginEsqueleto(onLoginExitoso: () -> Unit) {
    val tibu = MaterialTheme.tibu

    LaunchedEffect(Unit) {
        // Pequeña pausa para mostrar el splash y luego entrar
        kotlinx.coroutines.delay(1500)
        onLoginExitoso()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tibu.acento),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🦈", style = MaterialTheme.typography.displayLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Tiburoncitos",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Sistema de inventario",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
fun PantallaMenuEsqueleto(
    navController: NavController,
    asistenteViewModel: AsistenteViewModel
) {
    val tibu = MaterialTheme.tibu

    PapeleriaScaffold(asistenteViewModel = asistenteViewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(tibu.fondo)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tibu.acento)
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = 28.dp)
            ) {
                Column {
                    Text(
                        "🦈 Tiburoncitos",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Sistema de inventario",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                // Botón de ajustes arriba a la derecha
                IconButton(
                    onClick = { navController.navigate(Pantalla.Ajustes.ruta) },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        null,
                        tint = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // Opciones del menú
            LazyColumn (
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "ACCIONES RÁPIDAS",
                        style = MaterialTheme.typography.labelSmall,
                        color = tibu.textoSecundario,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }
                item {
                    TarjetaMenu(
                        icono = Icons.Default.Inventory2,
                        titulo = "Catálogo",
                        subtitulo = "Ver y gestionar productos",
                        onClick = { navController.navigate(Pantalla.Catalogo.ruta) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    TarjetaMenu(
                        icono = Icons.Default.QrCodeScanner,
                        titulo = "Escáner",
                        subtitulo = "Buscar por código de barras",
                        onClick = { navController.navigate(Pantalla.Escaner.ruta) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    TarjetaMenu(
                        icono = Icons.Default.Description,
                        titulo = "Cotizador",
                        subtitulo = "Procesar listas escolares",
                        onClick = { navController.navigate(Pantalla.Cotizacion.ruta) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    TarjetaMenu(
                        icono = Icons.Default.CloudSync,
                        titulo = "Sincronizar",
                        subtitulo = "Actualizar desde Google Sheets",
                        onClick = { navController.navigate(Pantalla.Sync.ruta) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
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