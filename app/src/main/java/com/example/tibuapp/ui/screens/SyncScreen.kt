package com.example.tibuapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tibuapp.data.model.DiferenciaProducto
import com.example.tibuapp.data.model.TipoDiferencia
import com.example.tibuapp.ui.components.PapeleriaScaffold
import com.example.tibuapp.ui.viewmodel.AsistenteViewModel
import com.example.tibuapp.ui.viewmodel.SyncEstado
import com.example.tibuapp.ui.viewmodel.SyncViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    syncViewModel: SyncViewModel,
    asistenteViewModel: AsistenteViewModel,
    onNavigateBack: () -> Unit
) {
    val estado by syncViewModel.estado.collectAsStateWithLifecycle()

    PapeleriaScaffold(
        asistenteViewModel = asistenteViewModel,
        topBar = {
            TopAppBar(
                title = { Text("🔄 Sincronizar Inventario") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) {
        when (val e = estado) {
            is SyncEstado.Inicial -> PantallaSyncInicial(
                onSincronizar = { syncViewModel.sincronizar() }
            )
            is SyncEstado.Sincronizando -> PantallaSyncCargando(
                "Conectando con Google Sheets..."
            )
            is SyncEstado.MostrandoDiferencias -> PantallaDiferencias(
                estado = e,
                onToggleNuevo = { syncViewModel.toggleNuevo(it) },
                onToggleModificado = { syncViewModel.toggleModificado(it) },
                onSeleccionarTodosNuevos = { syncViewModel.seleccionarTodosNuevos(it) },
                onSeleccionarTodosModificados = { syncViewModel.seleccionarTodosModificados(it) },
                onAplicar = { syncViewModel.aplicarCambios() }
            )
            is SyncEstado.Aplicando -> PantallaSyncCargando(
                "Aplicando cambios... ${e.progreso}/${e.total}"
            )
            is SyncEstado.Completado -> PantallaSyncCompletado(
                agregados = e.agregados,
                actualizados = e.actualizados,
                onVolver = {
                    syncViewModel.reiniciar()
                    onNavigateBack()
                }
            )
            is SyncEstado.Error -> PantallaSyncError(
                mensaje = e.mensaje,
                onReintentar = { syncViewModel.sincronizar() },
                onVolver = { syncViewModel.reiniciar() }
            )
        }
    }
}

@Composable
fun PantallaSyncInicial(onSincronizar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔄", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Sincronizar con Google Sheets",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "La app se conectará a tu hoja de Google Sheets, " +
                    "comparará los productos y te mostrará los cambios " +
                    "antes de aplicarlos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "📋 Antes de sincronizar",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    "Asegúrate de tener internet",
                    "El Sheet debe tener las columnas correctas",
                    "Solo se aplican los cambios que tú apruebes"
                ).forEach {
                    Text(
                        "• $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSincronizar,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Default.Sync, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Revisar cambios del Sheet")
        }
    }
}

@Composable
fun PantallaSyncCargando(mensaje: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(mensaje, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun PantallaDiferencias(
    estado: SyncEstado.MostrandoDiferencias,
    onToggleNuevo: (Int) -> Unit,
    onToggleModificado: (Int) -> Unit,
    onSeleccionarTodosNuevos: (Boolean) -> Unit,
    onSeleccionarTodosModificados: (Boolean) -> Unit,
    onAplicar: () -> Unit
) {
    val resultado = estado.resultado
    val totalSeleccionados = estado.nuevosSeleccionados.size +
            estado.modificadosSeleccionados.size

    Column(modifier = Modifier.fillMaxSize()) {
        // Resumen
        Surface(color = MaterialTheme.colorScheme.primaryContainer) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResumenItem("🆕 Nuevos", resultado.nuevos.size,
                    MaterialTheme.colorScheme.onPrimaryContainer)
                ResumenItem("✏️ Modificados", resultado.modificados.size,
                    MaterialTheme.colorScheme.onPrimaryContainer)
                ResumenItem("✅ Sin cambios", resultado.sinCambios,
                    MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sección: Nuevos
            if (resultado.nuevos.isNotEmpty()) {
                item {
                    SeccionEncabezado(
                        titulo = "🆕 Productos nuevos (${resultado.nuevos.size})",
                        todosSeleccionados = estado.nuevosSeleccionados.size == resultado.nuevos.size,
                        onSeleccionarTodos = onSeleccionarTodosNuevos
                    )
                }
                itemsIndexed(resultado.nuevos) { index, diff ->
                    TarjetaDiferencia(
                        diff = diff,
                        seleccionado = index in estado.nuevosSeleccionados,
                        onToggle = { onToggleNuevo(index) }
                    )
                }
            }

            // Sección: Modificados
            if (resultado.modificados.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SeccionEncabezado(
                        titulo = "✏️ Productos modificados (${resultado.modificados.size})",
                        todosSeleccionados = estado.modificadosSeleccionados.size ==
                                resultado.modificados.size,
                        onSeleccionarTodos = onSeleccionarTodosModificados
                    )
                }
                itemsIndexed(resultado.modificados) { index, diff ->
                    TarjetaDiferencia(
                        diff = diff,
                        seleccionado = index in estado.modificadosSeleccionados,
                        onToggle = { onToggleModificado(index) }
                    )
                }
            }

            // Errores
            if (resultado.errores.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "⚠️ Errores (${resultado.errores.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(resultado.errores.size) { index ->
                    Text(
                        resultado.errores[index],
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        // Botón aplicar
        Surface(shadowElevation = 8.dp) {
            Button(
                onClick = onAplicar,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
                enabled = totalSeleccionados > 0
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Aplicar $totalSeleccionados cambios seleccionados")
            }
        }
    }
}

@Composable
fun ResumenItem(label: String, cantidad: Int, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "$cantidad",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(label, style = MaterialTheme.typography.bodySmall, color = color)
    }
}

@Composable
fun SeccionEncabezado(
    titulo: String,
    todosSeleccionados: Boolean,
    onSeleccionarTodos: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            titulo,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = { onSeleccionarTodos(!todosSeleccionados) }) {
            Text(if (todosSeleccionados) "Deseleccionar todos" else "Seleccionar todos")
        }
    }
}

@Composable
fun TarjetaDiferencia(
    diff: DiferenciaProducto,
    seleccionado: Boolean,
    onToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (seleccionado) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(checked = seleccionado, onCheckedChange = { onToggle() })

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    diff.productoSheet.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                if (diff.productoSheet.categoria.isNotBlank()) {
                    Text(
                        diff.productoSheet.categoria,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Q ${String.format("%.2f", diff.productoSheet.precioUnitario)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    if (diff.productoSheet.precioMayor > 0) {
                        Text(
                            "Mayor: Q ${String.format("%.2f", diff.productoSheet.precioMayor)}" +
                                    " (x${diff.productoSheet.cantidadMinimaMayor}+)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                // Mostrar qué campos cambiaron
                if (diff.camposModificados.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Cambios: ${diff.camposModificados.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Badge de tipo
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (diff.tipo == TipoDiferencia.NUEVO)
                    MaterialTheme.colorScheme.tertiaryContainer
                else
                    MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    if (diff.tipo == TipoDiferencia.NUEVO) "Nuevo" else "Cambio",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    color = if (diff.tipo == TipoDiferencia.NUEVO)
                        MaterialTheme.colorScheme.onTertiaryContainer
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun PantallaSyncCompletado(
    agregados: Int,
    actualizados: Int,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✅", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "¡Sincronización completada!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                if (agregados > 0) {
                    Text("🆕 $agregados productos agregados",
                        style = MaterialTheme.typography.bodyLarge)
                }
                if (actualizados > 0) {
                    Text("✏️ $actualizados productos actualizados",
                        style = MaterialTheme.typography.bodyLarge)
                }
                if (agregados == 0 && actualizados == 0) {
                    Text("No se aplicaron cambios",
                        style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.large
        ) { Text("Volver al menú") }
    }
}

@Composable
fun PantallaSyncError(
    mensaje: String,
    onReintentar: () -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚠️", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            mensaje,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onReintentar,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.large
        ) { Text("Reintentar") }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.large
        ) { Text("Cancelar") }
    }
}