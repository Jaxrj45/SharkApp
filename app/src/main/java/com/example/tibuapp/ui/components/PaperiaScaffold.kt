package com.example.tibuapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tibuapp.ui.voice.BotonVozFlotante
import com.example.tibuapp.ui.viewmodel.AsistenteViewModel

@Composable
fun PapeleriaScaffold(
    asistenteViewModel: AsistenteViewModel,
    topBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Scaffold(topBar = topBar) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Contenido de la pantalla
            content()

            // Botón de voz siempre visible abajo a la derecha
            BotonVozFlotante(
                asistenteViewModel = asistenteViewModel,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
}