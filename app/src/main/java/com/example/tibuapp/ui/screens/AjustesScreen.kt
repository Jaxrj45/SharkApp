package com.example.tibuapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tibuapp.data.repository.PreferenciasRepository
import com.example.tibuapp.ui.theme.*
import com.example.tibuapp.ui.viewmodel.PreferenciasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    preferenciasViewModel: PreferenciasViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by preferenciasViewModel.uiState.collectAsStateWithLifecycle()
    val tibu = MaterialTheme.tibu

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ajustes",
                        style = MaterialTheme.typography.titleLarge,
                        color = tibu.textoPrimario
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            null,
                            tint = tibu.acento
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = tibu.superficie1
                )
            )
        },
        containerColor = tibu.fondo
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Sección apariencia
            item {
                SeccionAjustes(titulo = "Apariencia") {
                    FilaSwitch(
                        icono = Icons.Default.DarkMode,
                        iconoColor = Color(0xFF1C1C1E),
                        titulo = "Modo noche",
                        subtitulo = if (uiState.modoNocheAutomatico)
                            "Automático · 6pm a 6am"
                        else
                            if (uiState.modoNoche) "Activado" else "Desactivado",
                        checked = uiState.modoNoche,
                        enabled = !uiState.modoNocheAutomatico,
                        onCheckedChange = { preferenciasViewModel.toggleModoNoche(it) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = tibu.bordeSubtle
                    )
                    FilaSwitch(
                        icono = Icons.Default.Schedule,
                        iconoColor = Color(0xFFFF9500),
                        titulo = "Modo automático",
                        subtitulo = "Cambia según la hora del día",
                        checked = uiState.modoNocheAutomatico,
                        onCheckedChange = { preferenciasViewModel.toggleModoAutomatico(it) }
                    )
                }
            }

            // Selector de color
            item {
                SeccionAjustes(titulo = "Color de la app") {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Grid de colores 4x2
                        val colores = PreferenciasRepository.COLORES
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            colores.chunked(4).forEach { fila ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    fila.forEach { (hex, nombre) ->
                                        val color = hexAColor(hex)
                                        val seleccionado = uiState.colorAcento == hex
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(color)
                                                    .then(
                                                        if (seleccionado)
                                                            Modifier.border(
                                                                3.dp,
                                                                tibu.acento,
                                                                RoundedCornerShape(14.dp)
                                                            )
                                                        else Modifier
                                                    )
                                                    .clickable {
                                                        preferenciasViewModel.cambiarColor(hex)
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (seleccionado) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                nombre.split(" ").first(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (seleccionado)
                                                    tibu.acento
                                                else
                                                    tibu.textoSecundario,
                                                fontWeight = if (seleccionado)
                                                    FontWeight.SemiBold
                                                else
                                                    FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Vista previa del color seleccionado
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(tibu.acento),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Vista previa del color",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Sección acerca de
            item {
                SeccionAjustes(titulo = "Acerca de") {
                    FilaInfo(
                        icono = Icons.Default.Info,
                        iconoColor = tibu.acento,
                        titulo = "Tiburoncitos",
                        valor = "Versión 1.0.0"
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = tibu.bordeSubtle
                    )
                    FilaInfo(
                        icono = Icons.Default.Storage,
                        iconoColor = Color(0xFF34C759),
                        titulo = "Base de datos",
                        valor = "Local · Room"
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = tibu.bordeSubtle
                    )
                    FilaInfo(
                        icono = Icons.Default.Cloud,
                        iconoColor = Color(0xFF007AFF),
                        titulo = "Sincronización",
                        valor = "Google Sheets"
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ── Componentes de Ajustes ───────────────────────────────────

@Composable
fun SeccionAjustes(
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val tibu = MaterialTheme.tibu
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            titulo.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = tibu.textoSecundario,
            modifier = Modifier.padding(horizontal = 4.dp),
            fontWeight = FontWeight.Medium,
            letterSpacing = androidx.compose.ui.unit.TextUnit(
                0.5f,
                androidx.compose.ui.unit.TextUnitType.Sp
            )
        )
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = tibu.superficie1,
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                contenido()
            }
        }
    }
}

@Composable
fun FilaSwitch(
    icono: ImageVector,
    iconoColor: Color,
    titulo: String,
    subtitulo: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    val tibu = MaterialTheme.tibu
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconoColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                titulo,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) tibu.textoPrimario
                else tibu.textoSecundario,
                fontWeight = FontWeight.Medium
            )
            Text(
                subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = tibu.textoSecundario
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = tibu.acento,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = tibu.bordeSubtle
            )
        )
    }
}

@Composable
fun FilaInfo(
    icono: ImageVector,
    iconoColor: Color,
    titulo: String,
    valor: String
) {
    val tibu = MaterialTheme.tibu
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconoColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Text(
            titulo,
            style = MaterialTheme.typography.bodyMedium,
            color = tibu.textoPrimario,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            valor,
            style = MaterialTheme.typography.bodySmall,
            color = tibu.textoSecundario
        )
    }
}