package com.example.tibuapp.ui.screens

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.camera.core.Preview
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tibuapp.data.local.entity.ProductoEntity
import com.example.tibuapp.ui.scanner.CodigoBarrasAnalizador
import com.example.tibuapp.ui.scanner.EscanerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EscanerScreen(

    escanearViewModel: EscanerViewModel,
    onNavigateBack: () -> Unit,
    onAgregarAlCarrito: (ProductoEntity) -> Unit,
    onRegistrarProducto: (String) -> Unit //
) {

    val uiState by escanearViewModel.uiState.collectAsStateWithLifecycle()
    val permisoCamara = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!permisoCamara.status.isGranted) {
            permisoCamara.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(" Escanear ") },
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

    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                !permisoCamara.status.isGranted -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("📷", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Se necesita permiso de cámara",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { permisoCamara.launchPermissionRequest() }) {
                            Text("Conceder permiso")

                        }
                    }
                }

                uiState.escaneando->{
                    VistaEscaner(
                        onCodigoDetectado = {escanearViewModel.procesarCodigo(it)}
                    )

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Box(
                            modifier = Modifier
                                .size(250.dp)
                                .background(Color.Transparent)
                        ){
                            EsquinasEscaner()
                        }
                    }

                    Text(
                        text = "Apunta al codigo de Barras",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 48.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.White
                    )
                }
                uiState.productoEncontrado !=null -> {
                    ResultadoProducto(
                        producto = uiState.productoEncontrado!!,
                        onAgregarAlCarrito={
                            onAgregarAlCarrito(uiState.productoEncontrado!!)
                            escanearViewModel.reiniciarEscaner()
                        },
                        onEscanearOtro = {escanearViewModel.reiniciarEscaner()}
                    )
                }

                uiState.noEncontrado->{
                    ProductoNoEncontrado(
                        codigo = uiState.codigoDetectado,
                        onRegistrar = {
                            onRegistrarProducto(uiState.codigoDetectado)
                        },
                        onEscanearOtro= {escanearViewModel.reiniciarEscaner()}

                    )
                }
            }

        }

    }
}


@Composable
fun VistaEscaner(onCodigoDetectado: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analisis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(executor, CodigoBarrasAnalizador(onCodigoDetectado))
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analisis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}


@Composable
fun EsquinasEscaner() {
    // Esquinas decorativas del visor
    val color = Color.White
    val grosor = 4.dp
    val largo = 40.dp
    Box(modifier = Modifier.fillMaxSize()) {
        // Superior izquierda
        Box(Modifier.size(largo, grosor).background(color).align(Alignment.TopStart))
        Box(Modifier.size(grosor, largo).background(color).align(Alignment.TopStart))
        // Superior derecha
        Box(Modifier.size(largo, grosor).background(color).align(Alignment.TopEnd))
        Box(Modifier.size(grosor, largo).background(color).align(Alignment.TopEnd))
        // Inferior izquierda
        Box(Modifier.size(largo, grosor).background(color).align(Alignment.BottomStart))
        Box(Modifier.size(grosor, largo).background(color).align(Alignment.BottomStart))
        // Inferior derecha
        Box(Modifier.size(largo, grosor).background(color).align(Alignment.BottomEnd))
        Box(Modifier.size(grosor, largo).background(color).align(Alignment.BottomEnd))
    }
}


@Composable
fun ResultadoProducto(
    producto: ProductoEntity,
    onAgregarAlCarrito: () -> Unit,
    onEscanearOtro: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✅", style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Producto encontrado", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(producto.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (producto.categoria.isNotEmpty()) {
                    Text(producto.categoria, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Q ${String.format("%.2f", producto.precio)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Stock disponible: ${producto.stock}",
                    color = if (producto.stock > 0)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAgregarAlCarrito,
            modifier = Modifier.fillMaxWidth(),
            enabled = producto.stock > 0
        ) {
            Icon(Icons.Default.AddCircle, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar al carrito")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onEscanearOtro,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Escanear otro") }
    }
}

@Composable
fun ProductoNoEncontrado(
    codigo: String,
    onRegistrar: () -> Unit,
    onEscanearOtro: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("❓", style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Producto no encontrado", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Código: $codigo",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRegistrar,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Registrar este producto") }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onEscanearOtro,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Escanear otro") }
    }
}

