package com.example.tibuapp.ui.voice

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tibuapp.ui.viewmodel.AsistenteViewModel
import java.util.Locale

@Composable
fun BotonVozFlotante(
    asistenteViewModel: AsistenteViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by asistenteViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Launcher para el reconocimiento de voz del sistema Android
    val lanzadorVoz = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            val textos = resultado.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val textoReconocido = textos?.firstOrNull() ?: ""
            if (textoReconocido.isNotBlank()) {
                asistenteViewModel.procesarConsulta(textoReconocido)
            } else {
                asistenteViewModel.setError("No se escuchó nada, intenta de nuevo")
            }
        } else {
            asistenteViewModel.setEscuchando(false)
        }
    }

    // Animación de pulso cuando está escuchando
    val escala by animateFloatAsState(
        targetValue = if (uiState.escuchando) 1.2f else 1f,
        animationSpec = if (uiState.escuchando) {
            infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            spring()
        },
        label = "escala_boton"
    )

    Box(modifier = modifier) {
        // Panel de resultados (aparece sobre el botón)
        AnimatedVisibility(
            visible = uiState.mostrarResultados,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            PanelResultados(
                uiState = uiState,
                onCerrar = { asistenteViewModel.cerrarResultados() }
            )
        }

        // Botón flotante principal
        if (!uiState.mostrarResultados) {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-GT")
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-GT")
                        putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "¿Qué producto buscas?")
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    }
                    asistenteViewModel.setEscuchando(true)
                    lanzadorVoz.launch(intent)
                },
                modifier = Modifier.scale(escala),
                containerColor = when {
                    uiState.escuchando -> MaterialTheme.colorScheme.error
                    uiState.procesando -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                },
                shape = CircleShape
            ) {
                when {
                    uiState.procesando -> CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    uiState.escuchando -> Icon(
                        Icons.Default.MicOff,
                        "Escuchando",
                        tint = Color.White
                    )
                    else -> Icon(
                        Icons.Default.Mic,
                        "Hablar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun PanelResultados(
    uiState: com.example.tibuapp.ui.viewmodel.AsistenteUiState,
    onCerrar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 450.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Mic,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Asistente de voz",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onCerrar) {
                    Icon(Icons.Default.Close, "Cerrar")
                }
            }

            // Lo que escuchó
            if (uiState.textoEscuchado.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "🎤 \"${uiState.textoEscuchado}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Respuesta en texto
            Text(
                text = uiState.respuestaTexto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Lista de productos encontrados
            if (uiState.productosEncontrados.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Productos encontrados:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.heightIn(max = 250.dp)
                ) {
                    items(uiState.productosEncontrados) { producto ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        producto.nombre,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (producto.categoria.isNotEmpty()) {
                                        Text(
                                            producto.categoria,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "Q ${String.format("%.2f", producto.precio)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    val colorStock = when {
                                        producto.stock == 0 -> MaterialTheme.colorScheme.error
                                        producto.stock < 5 -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.secondary
                                    }
                                    Text(
                                        "Stock: ${producto.stock}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorStock
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Error si lo hay
            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}