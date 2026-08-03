package com.example.tibuapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tibuapp.data.repository.PreferenciasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

data class PreferenciasUiState(
    val colorAcento: String = "#007AFF",
    val modoNoche: Boolean = false,
    val modoNocheAutomatico: Boolean = true
)

class PreferenciasViewModel(
    application: Application,
    private val repository: PreferenciasRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PreferenciasUiState())
    val uiState: StateFlow<PreferenciasUiState> = _uiState.asStateFlow()

    init {
        // Observar cambios en las preferencias
        viewModelScope.launch {
            combine(
                repository.colorAcento,
                repository.modoNoche,
                repository.modoNocheAutomatico
            ) { color, noche, automatico ->
                PreferenciasUiState(
                    colorAcento = color,
                    modoNoche = if (automatico) esHoraNoche() else noche,
                    modoNocheAutomatico = automatico
                )
            }.collect { estado ->
                _uiState.value = estado
            }
        }
    }

    // Determina si es hora de noche (6pm - 6am)
    private fun esHoraNoche(): Boolean {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hora >= 18 || hora < 6
    }

    fun cambiarColor(color: String) {
        viewModelScope.launch {
            repository.guardarColor(color)
        }
    }

    fun toggleModoNoche(activo: Boolean) {
        viewModelScope.launch {
            repository.guardarModoNoche(activo)
        }
    }

    fun toggleModoAutomatico(activo: Boolean) {
        viewModelScope.launch {
            repository.guardarModoNocheAutomatico(activo)
            // Si activa el automático, aplicar según la hora
            if (activo) {
                repository.guardarModoNoche(esHoraNoche())
            }
        }
    }

    // Verificar si es hora de cambiar el modo (llamar periódicamente)
    fun verificarHoraModo() {
        if (_uiState.value.modoNocheAutomatico) {
            val deberiaSerNoche = esHoraNoche()
            if (deberiaSerNoche != _uiState.value.modoNoche) {
                viewModelScope.launch {
                    repository.guardarModoNoche(deberiaSerNoche)
                }
            }
        }
    }

    companion object {
        fun factory(
            application: Application,
            repository: PreferenciasRepository
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return PreferenciasViewModel(application, repository) as T
                }
            }
        }
    }
}