package com.example.tibuapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tibuapp.ui.viewmodel.CotizacionEstado
import com.example.tibuapp.data.model.LineaOCR
import com.example.tibuapp.ui.components.PapeleriaScaffold
import com.example.tibuapp.ui.viewmodel.AsistenteViewModel
import com.example.tibuapp.ui.viewmodel.CotizacionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CotizacionScreen(
    cotizacionViewModel: CotizacionViewModel,
    asistenteViewModel: AsistenteViewModel,
    onNavigateBack: () -> Unit,
    onCompartirWhatsApp: (String) -> Unit,
    onGenerarPDF: (String) -> Unit
) {
    val uiState by cotizacionViewModel.uiState.collectAsStateWithLifecycle()
    val permisoCamera = rememberPermissionState(android.Manifest.permission.CAMERA)

    val lanzadorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { cotizacionViewModel.procesarImagen(it) } }

    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito) {
            uiState.uriFotoTemporal?.let { cotizacionViewModel.procesarImagen(it) }
        }
    }

    PapeleriaScaffold(
        asistenteViewModel = asistenteViewModel,
        topBar = {
            TopAppBar(
                title = { Text("📄 Cotizador de Lista") },
                navigationIcon = {
                    IconButton(onClick = {
                        cotizacionViewModel.reiniciar()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                actions = {
                    if (uiState.estado !is CotizacionEstado.Inicial) {
                        IconButton(onClick = { cotizacionViewModel.reiniciar() }) {
                            Icon(Icons.Default.Refresh, "Nueva cotización")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) {
        when (val estado = uiState.estado) {
            is CotizacionEstado.Inicial -> PantallaInicial(
                onTomarFoto = {
                    if (permisoCamera.status.isGranted) {
                        val uri = cotizacionViewModel.crearUriFotoTemporal()
                        lanzadorCamara.launch(uri)
                    } else {
                        permisoCamera.launchPermissionRequest()
                    }
                },
                onSeleccionarGaleria = { lanzadorGaleria.launch("image/*") },
                permisoConcedido = permisoCamera.status.isGranted,
                permisoDenegado = permisoCamera.status.shouldShowRationale
            )

            is CotizacionEstado.ProcesandoOCR ->
                PantallaCargando("Analizando la lista...")

            is CotizacionEstado.RevisionLineas -> PantallaRevision(
                lineas = estado.lineas,
                onEditarLinea = { id, texto -> cotizacionViewModel.editarLinea(id, texto) },
                onIgnorarLinea = { cotizacionViewModel.toggleIgnorarLinea(it) },
                onAgregarLinea = { cotizacionViewModel.agregarLinea() },
                onContinuar = { cotizacionViewModel.buscarProductos() }
            )

            is CotizacionEstado.BuscandoProductos ->
                PantallaCargando("Buscando precios en inventario...")

            is CotizacionEstado.CotizacionLista -> PantallaCotizacion(
                cotizacion = estado.cotizacion,
                onToggleIncluir = { cotizacionViewModel.toggleIncluirLinea(it) },
                onAjustarCantidad = { id, cant ->
                    cotizacionViewModel.ajustarCantidad(id, cant)
                },
                onCompartirWhatsApp = {
                    onCompartirWhatsApp(generarTextoWhatsApp(estado.cotizacion))
                },
                onGenerarPDF = {
                    onGenerarPDF(generarTextoWhatsApp(estado.cotizacion))
                }
            )

            is CotizacionEstado.Error -> PantallaError(
                mensaje = estado.mensaje,
                onReintentar = { cotizacionViewModel.reiniciar() }
            )
        }
    }
}

// ── Pantalla inicial ─────────────────────────────────────────

@Composable
fun PantallaInicial(
    onTomarFoto: () -> Unit,
    onSeleccionarGaleria: () -> Unit,
    permisoConcedido: Boolean,
    permisoDenegado: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📄", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Cotizador de lista escolar",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Toma una foto de la lista o selecciónala de la galería. " +
                    "La app leerá los artículos y buscará los precios automáticamente.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onTomarFoto,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Default.CameraAlt, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (permisoConcedido) "Tomar foto de la lista"
                else "Tomar foto (requiere permiso)"
            )
        }

        if (permisoDenegado) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    "Permiso denegado. Ve a Configuración → Apps → Tibuapp → " +
                            "Permisos → Cámara para habilitarlo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onSeleccionarGaleria,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Default.PhotoLibrary, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Seleccionar de galería")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "💡 Consejos para mejor resultado",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    "Buena iluminación, sin sombras",
                    "Foto derecha, sin inclinación",
                    "Acércate para que el texto sea grande",
                    "Fondo contrastante con el papel"
                ).forEach { consejo ->
                    Text(
                        "• $consejo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

// ── Pantalla de carga ────────────────────────────────────────

@Composable
fun PantallaCargando(mensaje: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(mensaje, style = MaterialTheme.typography.titleMedium)
        }
    }
}

// ── Pantalla de revisión de líneas OCR ──────────────────────

@Composable
fun PantallaRevision(
    lineas: List<LineaOCR>,
    onEditarLinea: (Int, String) -> Unit,
    onIgnorarLinea: (Int) -> Unit,
    onAgregarLinea: () -> Unit,
    onContinuar: () -> Unit
) {
    val lineasActivas = lineas.count { !it.ignorar }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Revisa los artículos detectados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Corrige errores del OCR. Toca el ojo para ignorar líneas irrelevantes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(lineas, key = { _, linea -> linea.id }) { index, linea ->
                LineaRevisionItem(
                    numero = index + 1,
                    linea = linea,
                    onEditar = { onEditarLinea(linea.id, it) },
                    onIgnorar = { onIgnorarLinea(linea.id) }
                )
            }
            item {
                OutlinedButton(
                    onClick = onAgregarLinea,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar artículo manualmente")
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        Surface(shadowElevation = 8.dp) {
            Button(
                onClick = onContinuar,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.Search, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buscar precios ($lineasActivas artículos)")
            }
        }
    }
}

@Composable
fun LineaRevisionItem(
    numero: Int,
    linea: LineaOCR,
    onEditar: (String) -> Unit,
    onIgnorar: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (linea.ignorar)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (linea.ignorar) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = if (linea.ignorar)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "$numero",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (linea.ignorar)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            OutlinedTextField(
                value = linea.textoEditado,
                onValueChange = onEditar,
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = !linea.ignorar,
                textStyle = LocalTextStyle.current.copy(
                    textDecoration = if (linea.ignorar)
                        TextDecoration.LineThrough
                    else
                        null
                ),
                shape = MaterialTheme.shapes.large
            )

            IconButton(onClick = onIgnorar) {
                Icon(
                    if (linea.ignorar) Icons.Default.VisibilityOff
                    else Icons.Default.Visibility,
                    "Ignorar",
                    tint = if (linea.ignorar)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Pantalla de cotización final ─────────────────────────────

@Composable
fun PantallaCotizacion(
    cotizacion: com.example.tibuapp.data.model.Cotizacion,
    onToggleIncluir: (Int) -> Unit,
    onAjustarCantidad: (Int, Int) -> Unit,
    onCompartirWhatsApp: () -> Unit,
    onGenerarPDF: () -> Unit
) {
    val incluidos = cotizacion.lineas.count { !it.ignorar && it.encontrado }
    val sinPrecio = cotizacion.lineas.count { !it.encontrado }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "✅ $incluidos encontrados",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (sinPrecio > 0) {
                        Text(
                            "❌ $sinPrecio sin precio",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Total estimado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        "Q ${String.format("%.2f", cotizacion.total)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(
                cotizacion.lineas,
                key = { _, linea -> linea.id }
            ) { index, linea ->
                LineaCotizacionItem(
                    numero = index + 1,
                    linea = linea,
                    onToggleIncluir = { onToggleIncluir(linea.id) },
                    onAjustarCantidad = { onAjustarCantidad(linea.id, it) }
                )
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        Surface(shadowElevation = 8.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCompartirWhatsApp,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.Share, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp")
                }
                Button(
                    onClick = onGenerarPDF,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.PictureAsPdf, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF")
                }
            }
        }
    }
}

@Composable
fun LineaCotizacionItem(
    numero: Int,
    linea: LineaOCR,
    onToggleIncluir: () -> Unit,
    onAjustarCantidad: (Int) -> Unit
) {
    val encontrado = linea.encontrado
    val incluido = !linea.ignorar

    Card(
        colors = CardDefaults.cardColors(
            containerColor = when {
                !encontrado -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                !incluido -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            if (incluido && encontrado) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = incluido && encontrado,
                onCheckedChange = { if (encontrado) onToggleIncluir() },
                enabled = encontrado
            )

            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = when {
                    !encontrado -> MaterialTheme.colorScheme.error
                    !incluido -> MaterialTheme.colorScheme.surfaceVariant
                    else -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "$numero",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            !encontrado -> MaterialTheme.colorScheme.onError
                            !incluido -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.onPrimary
                        }
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = linea.productoEncontrado?.nombre ?: linea.textoEditado,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (!incluido) TextDecoration.LineThrough else null
                )
                Text(
                    text = if (encontrado) "\"${linea.textoEditado}\""
                    else "Sin precio en inventario",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (encontrado)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.error
                )
            }

            if (encontrado) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Q ${String.format("%.2f",
                            (linea.productoEncontrado?.precio ?: 0.0) * linea.cantidad)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (incluido)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilledIconButton(
                            onClick = { onAjustarCantidad(linea.cantidad - 1) },
                            enabled = linea.cantidad > 1,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Remove, null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            "${linea.cantidad}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.widthIn(min = 20.dp),
                            textAlign = TextAlign.Center
                        )
                        FilledIconButton(
                            onClick = { onAjustarCantidad(linea.cantidad + 1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Add, null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Pantalla de error ────────────────────────────────────────

@Composable
fun PantallaError(mensaje: String, onReintentar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("😕", style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            mensaje,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onReintentar,
            shape = MaterialTheme.shapes.large
        ) { Text("Intentar de nuevo") }
    }
}

// ── Generador de texto para compartir ───────────────────────

fun generarTextoWhatsApp(
    cotizacion: com.example.tibuapp.data.model.Cotizacion
): String {
    val sb = StringBuilder()
    sb.appendLine("🦈 *Tiburoncitos - Cotización*")
    sb.appendLine("─────────────────────")
    var numero = 1
    cotizacion.lineas
        .filter { !it.ignorar && it.encontrado }
        .forEach { linea ->
            val precio = linea.productoEncontrado?.precio ?: 0.0
            sb.appendLine(
                "$numero. ${linea.productoEncontrado?.nombre} " +
                        "x${linea.cantidad} = Q ${String.format("%.2f", precio * linea.cantidad)}"
            )
            numero++
        }
    if (cotizacion.articulosNoEncontrados.isNotEmpty()) {
        sb.appendLine()
        sb.appendLine("❌ *Sin precio en inventario:*")
        cotizacion.articulosNoEncontrados.forEach {
            sb.appendLine("• ${it.textoEditado}")
        }
    }
    sb.appendLine("─────────────────────")
    sb.appendLine("*Total: Q ${String.format("%.2f", cotizacion.total)}*")
    return sb.toString()
}