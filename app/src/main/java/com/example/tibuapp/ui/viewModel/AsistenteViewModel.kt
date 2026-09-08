package com.example.tibuapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tibuapp.data.local.entity.ProductoEntity
import com.example.tibuapp.data.repository.ProductRepository
import com.example.tibuapp.ui.voice.AsistenteVoz
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AsistenteUiState(
    val escuchando: Boolean = false,
    val procesando: Boolean = false,
    val textoEscuchado: String = "",
    val respuestaTexto: String = "",
    val productosEncontrados: List<ProductoEntity> = emptyList(),
    val mostrarResultados: Boolean = false,
    val error: String? = null
)

class AsistenteViewModel(
    application: Application,
    private val repository: ProductRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AsistenteUiState())
    val uiState: StateFlow<AsistenteUiState> = _uiState.asStateFlow()

    private val asistenteVoz = AsistenteVoz(application)

    // Procesa el texto que reconoció el micrófono
    fun procesarConsulta(textoHablado: String) {
        if (textoHablado.isBlank()) return

        _uiState.value = _uiState.value.copy(
            textoEscuchado = textoHablado,
            procesando = true,
            escuchando = false,
            error = null
        )

        viewModelScope.launch {
            val texto = textoHablado.lowercase().trim()

            // Extraemos la palabra clave de búsqueda del texto hablado
            val terminoBusqueda = extraerTerminoBusqueda(texto)

            if (terminoBusqueda.isBlank()) {
                val respuesta = "No entendí tu consulta. Intenta decir el nombre de un producto."
                _uiState.value = _uiState.value.copy(
                    respuestaTexto = respuesta,
                    procesando = false,
                    mostrarResultados = true
                )
                asistenteVoz.hablar(respuesta)
                return@launch
            }

            // Buscar en la base de datos
            val productos = repository.buscar(terminoBusqueda)?.first() ?: emptyList() // verificar
            val respuesta = construirRespuesta(texto, productos, terminoBusqueda)

            _uiState.value = _uiState.value.copy(
                productosEncontrados = productos,
                respuestaTexto = respuesta,
                procesando = false,
                mostrarResultados = true
            )

            asistenteVoz.hablar(respuesta)
        }
    }

    // Interpreta qué producto está buscando el empleado
    private fun extraerTerminoBusqueda(texto: String): String {
        val patrones = listOf(
            "cuánto cuesta ", "cuanto cuesta ",
            "precio de ", "precio del ",
            "tenemos ", "hay ",
            "busca ", "buscar ",
            "muéstrame ", "muestrame ",
            "qué precio tiene ", "que precio tiene ",
            "código ", "codigo "
        )
        var termino = texto
        for (patron in patrones) {
            if (texto.contains(patron)) {
                termino = texto.substringAfter(patron).trim()
                // Limpiar palabras finales comunes
                termino = termino
                    .removeSuffix("?")
                    .removeSuffix(".")
                    .replace("¿", "")
                break
            }
        }
        // Si no encontró patrón, usa todo el texto como búsqueda
        return termino.trim()
    }

    // Construye la respuesta hablada según los resultados
    private fun construirRespuesta(
        consulta: String,
        productos: List<ProductoEntity>,
        termino: String
    ): String {
        return when {
            productos.isEmpty() -> {
                "No encontré ningún producto llamado $termino en el inventario."
            }
            productos.size == 1 -> {
                val p = productos.first()
                val stockMsg = when {
                    p.stock == 0 -> "pero no tenemos en stock"
                    p.stock < 5 -> "quedan solo ${p.stock} unidades"
                    else -> "hay ${p.stock} en stock"
                }
                "${p.nombre} cuesta Q ${String.format("%.2f", p.precio)} y $stockMsg."
            }
            else -> {
                // Múltiples resultados
                val primeros = productos.take(3)
                val nombresYPrecios = primeros.joinToString(", ") {
                    "${it.nombre} a Q ${String.format("%.2f", it.precio)}"
                }
                val resto = if (productos.size > 3)
                    " y ${productos.size - 3} más."
                else "."
                "Encontré ${productos.size} productos: $nombresYPrecios$resto"
            }
        }
    }

    fun setEscuchando(valor: Boolean) {
        _uiState.value = _uiState.value.copy(escuchando = valor)
    }

    fun cerrarResultados() {
        _uiState.value = AsistenteUiState()
        asistenteVoz.detener()
    }

    fun setError(mensaje: String) {
        _uiState.value = _uiState.value.copy(
            error = mensaje,
            escuchando = false,
            procesando = false
        )
    }

    override fun onCleared() {
        super.onCleared()
        asistenteVoz.liberar()
    }

    companion object {
        fun factory(
            application: Application,
            repository: ProductRepository
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return AsistenteViewModel(application, repository) as T
                }
            }
        }
    }
}