package com.example.tibuapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.tibuapp.ui.theme.*

// ── TopAppBar ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TibuTopBar(
    titulo: String,
    onNavigateBack: (() -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    val tibu = MaterialTheme.tibu
    TopAppBar(
        title = {
            Text(
                titulo,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        },
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color.White
                    )
                }
            }
        },
        actions = acciones,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = tibu.acento,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

// ── Tarjeta de menú ──────────────────────────────────────────

@Composable
fun TarjetaMenu(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tibu = MaterialTheme.tibu

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = tibu.superficie1
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, tibu.bordeSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tibu.acentoSuave),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icono,
                    contentDescription = null,
                    tint = tibu.acento,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = tibu.textoPrimario,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = tibu.textoSecundario
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = tibu.bordeSubtle,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Badge de stock ───────────────────────────────────────────

@Composable
fun BadgeStock(stock: Int) {
    val tibu = MaterialTheme.tibu
    val (bgColor, textColor, texto) = when {
        stock == 0 -> Triple(
            if (tibu.esNoche) Rojo100D else Rojo100L,
            if (tibu.esNoche) Rojo800D else Rojo800L,
            "Sin stock"
        )
        stock < 5 -> Triple(
            if (tibu.esNoche) Amarillo100D else Amarillo100L,
            if (tibu.esNoche) Amarillo800D else Amarillo800L,
            "Poco stock"
        )
        else -> Triple(
            if (tibu.esNoche) Verde100D else Verde100L,
            if (tibu.esNoche) Verde800D else Verde800L,
            "En stock"
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            texto,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Precio formateado ────────────────────────────────────────

@Composable
fun TextoPrecio(
    precio: Double,
    modifier: Modifier = Modifier,
    grande: Boolean = false
) {
    val tibu = MaterialTheme.tibu
    Text(
        "Q ${String.format("%.2f", precio)}",
        style = if (grande) MaterialTheme.typography.headlineSmall
        else MaterialTheme.typography.titleMedium,
        color = tibu.acento,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

// ── Botón primario ───────────────────────────────────────────

@Composable
fun TibuBoton(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    habilitado: Boolean = true
) {
    val tibu = MaterialTheme.tibu
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = habilitado,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = tibu.acento,
            contentColor = Color.White,
            disabledContainerColor = tibu.bordeSubtle,
            disabledContentColor = tibu.textoSecundario
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        if (icono != null) {
            Icon(icono, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelLarge)
    }
}

// ── Botón secundario ─────────────────────────────────────────

@Composable
fun TibuBotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null
) {
    val tibu = MaterialTheme.tibu
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, tibu.acento),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = tibu.acento
        )
    ) {
        if (icono != null) {
            Icon(icono, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelLarge)
    }
}

// ── Campo de búsqueda ────────────────────────────────────────

@Composable
fun CampoBusqueda(
    valor: String,
    onValorChange: (String) -> Unit,
    placeholder: String = "Buscar...",
    modifier: Modifier = Modifier
) {
    val tibu = MaterialTheme.tibu
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        modifier = modifier,
        placeholder = {
            Text(
                placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = tibu.textoSecundario
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                null,
                tint = tibu.textoSecundario,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            if (valor.isNotEmpty()) {
                IconButton(onClick = { onValorChange("") }) {
                    Icon(
                        Icons.Default.Clear,
                        null,
                        tint = tibu.textoSecundario,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = tibu.acento,
            unfocusedBorderColor = tibu.bordeSubtle,
            focusedContainerColor = tibu.superficie1,
            unfocusedContainerColor = tibu.superficie2,
            cursorColor = tibu.acento,
            focusedTextColor = tibu.textoPrimario,
            unfocusedTextColor = tibu.textoPrimario
        ),
        textStyle = MaterialTheme.typography.bodyMedium
    )
}

// ── Estado vacío ─────────────────────────────────────────────

@Composable
fun EstadoVacio(
    emoji: String,
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier
) {
    val tibu = MaterialTheme.tibu
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            titulo,
            style = MaterialTheme.typography.titleLarge,
            color = tibu.textoPrimario
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            subtitulo,
            style = MaterialTheme.typography.bodyMedium,
            color = tibu.textoSecundario
        )
    }
}

// ── Indicador de dot de stock ─────────────────────────────────

@Composable
fun DotStock(stock: Int, modifier: Modifier = Modifier) {
    val tibu = MaterialTheme.tibu
    val color = when {
        stock == 0 -> if (tibu.esNoche) Rojo800D else Rojo800L
        stock < 5 -> if (tibu.esNoche) Amarillo800D else Amarillo800L
        else -> if (tibu.esNoche) Verde800D else Verde800L
    }
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(RoundedCornerShape(50))
            .background(color)
    )
}