package com.example.tibuapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tibuapp.ui.screens.MainNavHost
import com.example.tibuapp.ui.theme.TibuappTheme
import com.example.tibuapp.ui.viewmodel.PreferenciasViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = applicationContext as TibuApp
            val preferenciasViewModel: PreferenciasViewModel = viewModel(
                factory = PreferenciasViewModel.factory(
                    application = application,
                    repository = app.container.preferenciasRepository
                )
            )
            val prefs by preferenciasViewModel.uiState.collectAsStateWithLifecycle()

            TibuappTheme(
                colorAcento = prefs.colorAcento,
                modoNoche = prefs.modoNoche
            ) {
                MainNavHost(preferenciasViewModel = preferenciasViewModel)
            }
        }
    }
}