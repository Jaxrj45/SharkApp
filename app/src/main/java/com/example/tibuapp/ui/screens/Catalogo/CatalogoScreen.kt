package com.example.tibuapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tibuapp.data.local.entity.ProductoEntity
import com.example.tibuapp.ui.components.BadgeStock
import com.example.tibuapp.ui.components.PapeleriaScaffold
import com.example.tibuapp.ui.components.TextoPrecio
import com.example.tibuapp.ui.components.TibuBoton
import com.example.tibuapp.ui.components.TibuBotonSecundario
import com.example.tibuapp.ui.screens.Producto.ProductoViewModel
import com.example.tibuapp.ui.theme.Blanco
import com.example.tibuapp.ui.theme.Gris200
import com.example.tibuapp.ui.theme.Gris500
import com.example.tibuapp.ui.theme.Gris900
import com.example.tibuapp.ui.theme.Naranja100
import com.example.tibuapp.ui.theme.Naranja500
import com.example.tibuapp.ui.theme.Rojo500
import com.example.tibuapp.ui.viewmodel.AsistenteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    productoViewModel: ProductoViewModel,
    asistenteViewModel: AsistenteViewModel,
    codigoBarrasInicial: String? = null,
    onNavigateBack: () -> Unit
) {
    val uiState by productoViewModel.uiState.collectAsStateWithLifecycle()
    var mostrarFormulario by remember { mutableStateOf(false) }
    var productoEditando by remember { mutableStateOf<ProductoEntity?>(null) }

    LaunchedEffect(codigoBarrasInicial) {
        if (!codigoBarrasInicial.isNullOrBlank()) {
            productoEditando = null
            mostrarFormulario = true
        }
    }

    uiState.mensajeExito?.let { mensaje ->
        LaunchedEffect(mensaje) {
            kotlinx.coroutines.delay(500)
            productoViewModel.limpiarMensajes()
            mostrarFormulario = false
            productoEditando = null
        }
    }

    PapeleriaScaffold(
        asistenteViewModel = asistenteViewModel,
        topBar = {
            TopAppBar(
                title = { Text("📦 Catálogo de Productos") },
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
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Barra de búsqueda
                OutlinedTextField(
                    value = uiState.busqueda,
                    onValueChange = { productoViewModel.buscar(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar producto...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (uiState.busqueda.isNotEmpty()) {
                            IconButton(onClick = { productoViewModel.buscar("") }) {
                                Icon(Icons.Default.Clear, "Limpiar")
                            }
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Contador de productos
                Text(
                    text = "${uiState.productos.size} productos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Lista de productos
                if (uiState.productos.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📦", style = MaterialTheme.typography.displayMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No hay productos aún",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Toca + para agregar el primero",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.productos, key = { it.id }) { producto ->
                            TarjetaProducto(
                                producto = producto,
                                onEditar = {
                                    productoEditando = producto
                                    mostrarFormulario = true
                                },
                                onEliminar = {
                                    productoViewModel.desactivarProducto(producto.id)
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }

            // Botón + abajo a la izquierda
            FloatingActionButton(
                onClick = {
                    productoEditando = null
                    mostrarFormulario = true
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, "Agregar producto")
            }

            // Nota: el botón 🎤 lo pone PapeleriaScaffold abajo a la derecha automáticamente
        }
    }

    // Formulario deslizable desde abajo
    if (mostrarFormulario) {
        FormularioProducto(
            producto = productoEditando,
            codigoBarrasInicial = codigoBarrasInicial,
            onGuardar = { nombre, desc, precio, stock, cat, codigo ->
                productoViewModel.guardarProducto(
                    id = productoEditando?.id ?: 0,
                    nombre = nombre,
                    descripcion = desc,
                    precio = precio,
                    stock = stock,
                    categoria = cat,
                    codigoBarras = codigo
                )
            },
            onCerrar = {
                mostrarFormulario = false
                productoEditando = null
                productoViewModel.limpiarMensajes()
            },
            error = uiState.error,
            exito = uiState.mensajeExito
        )
    }
}

@Composable
fun TarjetaProducto(
    producto: ProductoEntity,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Gris200)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Indicador de categoría
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Naranja100),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    producto.nombre.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Naranja500,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = Gris900,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                if (producto.categoria.isNotEmpty()) {
                    Text(
                        producto.categoria,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gris500
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextoPrecio(precio = producto.precio)
                    BadgeStock(stock = producto.stock)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                IconButton(
                    onClick = onEditar,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        null,
                        tint = Naranja500,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { mostrarConfirmacion = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        null,
                        tint = Rojo500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("¿Eliminar producto?", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Text(
                    "Se desactivará '${producto.nombre}'.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gris500
                )
            },
            confirmButton = {
                TibuBoton(
                    texto = "Eliminar",
                    onClick = {
                        onEliminar()
                        mostrarConfirmacion = false
                    },
                    modifier = Modifier,
                )
            },
            dismissButton = {
                TibuBotonSecundario(
                    texto = "Cancelar",
                    onClick = { mostrarConfirmacion = false }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioProducto(
    producto: ProductoEntity?,
    codigoBarrasInicial: String?,
    onGuardar: (String, String, String, String, String, String) -> Unit,
    onCerrar: () -> Unit,
    error: String?,
    exito: String?
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var nombre by remember(producto) { mutableStateOf(producto?.nombre ?: "") }
    var descripcion by remember(producto) { mutableStateOf(producto?.descripcion ?: "") }
    var precio by remember(producto) { mutableStateOf(producto?.precio?.toString() ?: "") }
    var stock by remember(producto) { mutableStateOf(producto?.stock?.toString() ?: "") }
    var categoria by remember(producto) { mutableStateOf(producto?.categoria ?: "") }
    var codigoBarras by remember(producto, codigoBarrasInicial) {
        mutableStateOf(producto?.codigoBarras ?: codigoBarrasInicial ?: "")
    }

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = sheetState,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .width(44.dp)
                    .height(5.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                ) {}
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = if (producto == null) Icons.Default.Add
                            else Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (producto == null) "Nuevo producto" else "Editar producto",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (producto == null) "Agrega un artículo al catálogo"
                            else "Actualiza los datos del artículo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del producto") },
                placeholder = { Text("Ej: Cuaderno universitario") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                leadingIcon = { Icon(Icons.Default.Inventory2, null) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                placeholder = { Text("Ej: 100 hojas, cuadros, tamaño carta") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3,
                shape = MaterialTheme.shapes.large,
                leadingIcon = { Icon(Icons.Default.Notes, null) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    leadingIcon = {
                        Text("Q", fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                    }
                )
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    leadingIcon = { Icon(Icons.Default.Inventory, null) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = categoria,
                onValueChange = { categoria = it },
                label = { Text("Categoría") },
                placeholder = { Text("Cuadernos, Escritura, Arte...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                leadingIcon = { Icon(Icons.Default.Category, null) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = codigoBarras,
                onValueChange = { codigoBarras = it },
                label = { Text("Código de barras") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                leadingIcon = { Icon(Icons.Default.QrCode2, null) },
                trailingIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.QrCodeScanner, "Escanear código")
                    }
                }
            )

            if (error != null || exito != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = if (error != null) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (error != null) Icons.Default.Error
                            else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (error != null) MaterialTheme.colorScheme.onErrorContainer
                            else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error ?: exito ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (error != null) MaterialTheme.colorScheme.onErrorContainer
                            else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCerrar,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = MaterialTheme.shapes.large
                ) { Text("Cancelar") }

                Button(
                    onClick = {
                        onGuardar(nombre, descripcion, precio, stock, categoria, codigoBarras)
                    },
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.Save, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar")
                }
            }
        }
    }
}