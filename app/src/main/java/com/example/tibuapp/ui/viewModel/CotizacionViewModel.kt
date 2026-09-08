package com.example.tibuapp.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tibuapp.data.local.entity.ProductoEntity
import com.example.tibuapp.data.model.Cotizacion
import com.example.tibuapp.data.model.LineaOCR
import com.example.tibuapp.data.repository.ProductRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

sealed class CotizacionEstado {
    object Inicial : CotizacionEstado()
    object ProcesandoOCR : CotizacionEstado()
    data class RevisionLineas(val lineas: List<LineaOCR>) : CotizacionEstado()
    object BuscandoProductos : CotizacionEstado()
    data class CotizacionLista(val cotizacion: Cotizacion) : CotizacionEstado()
    data class Error(val mensaje: String) : CotizacionEstado()
}

data class CotizacionUiState(
    val estado: CotizacionEstado = CotizacionEstado.Inicial,
    val uriFotoTemporal: Uri? = null
)

class CotizacionViewModel(
    application: Application,
    private val repository: ProductRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CotizacionUiState())
    val uiState: StateFlow<CotizacionUiState> = _uiState.asStateFlow()

    private val reconocedor = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Crea un URI temporal para guardar la foto de la cámara
    fun crearUriFotoTemporal(): Uri {
        val context = getApplication<Application>()
        val directorio = File(context.cacheDir, "camera").also { it.mkdirs() }
        val archivo = File(directorio, "lista_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )
        _uiState.value = _uiState.value.copy(uriFotoTemporal = uri)
        return uri
    }

    fun procesarImagen(uri: Uri) {
        _uiState.value = _uiState.value.copy(estado = CotizacionEstado.ProcesandoOCR)

        viewModelScope.launch {
            try {
                val imagen = InputImage.fromFilePath(getApplication(), uri)
                val resultado = reconocedor.process(imagen).await()

                // Mejor extracción: ordenamos por posición vertical para respetar el orden de la lista
                val lineas = resultado.textBlocks
                    .sortedBy { it.boundingBox?.top ?: 0 }
                    .flatMap { bloque ->
                        bloque.lines.sortedBy { it.boundingBox?.top ?: 0 }
                    }
                    .map { it.text.trim() }
                    .filter { texto ->
                        texto.length > 2 &&
                                !texto.matches(Regex("^[\\d.,\\s\\-_]+$")) &&
                                !texto.matches(Regex("^[^a-zA-ZáéíóúÁÉÍÓÚñÑ]+$"))
                    }
                    .distinct() // Evita duplicados
                    .mapIndexed { index, texto ->
                        LineaOCR(
                            id = index,
                            textoOriginal = texto,
                            textoEditado = limpiarTexto(texto)
                        )
                    }

                if (lineas.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        estado = CotizacionEstado.Error(
                            "No se encontró texto legible. Intenta con mejor iluminación o una foto más cerca."
                        )
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        estado = CotizacionEstado.RevisionLineas(lineas)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    estado = CotizacionEstado.Error("Error al procesar: ${e.message}")
                )
            }
        }
    }

    // Limpia el texto extraído del OCR
    private fun limpiarTexto(texto: String): String {
        return texto
            .replace(Regex("[|\\\\]"), "")  // Artefactos comunes del OCR
            .replace(Regex("\\s+"), " ")     // Espacios múltiples
            .trim()
            .lowercase()
            .replaceFirstChar { it.uppercase() } // Primera letra mayúscula
    }

    fun editarLinea(id: Int, nuevoTexto: String) {
        val estado = _uiState.value.estado as? CotizacionEstado.RevisionLineas ?: return
        _uiState.value = _uiState.value.copy(
            estado = estado.copy(
                lineas = estado.lineas.map {
                    if (it.id == id) it.copy(textoEditado = nuevoTexto) else it
                }
            )
        )
    }

    fun toggleIgnorarLinea(id: Int) {
        val estado = _uiState.value.estado as? CotizacionEstado.RevisionLineas ?: return
        _uiState.value = _uiState.value.copy(
            estado = estado.copy(
                lineas = estado.lineas.map {
                    if (it.id == id) it.copy(ignorar = !it.ignorar) else it
                }
            )
        )
    }

    fun agregarLinea() {
        val estado = _uiState.value.estado as? CotizacionEstado.RevisionLineas ?: return
        val nuevaLinea = LineaOCR(
            id = (estado.lineas.maxOfOrNull { it.id } ?: 0) + 1,
            textoOriginal = "",
            textoEditado = ""
        )
        _uiState.value = _uiState.value.copy(
            estado = estado.copy(lineas = estado.lineas + nuevaLinea)
        )
    }

    fun buscarProductos() {
        val estado = _uiState.value.estado as? CotizacionEstado.RevisionLineas ?: return
        val lineasActivas = estado.lineas.filter { !it.ignorar && it.textoEditado.isNotBlank() }

        _uiState.value = _uiState.value.copy(estado = CotizacionEstado.BuscandoProductos)

        viewModelScope.launch {
            val lineasConProductos = lineasActivas.map { linea ->
                val productos = repository.buscar(linea.textoEditado)?.first()
                linea.copy(
                    productoEncontrado = productos?.firstOrNull(),
                    encontrado = productos?.isNotEmpty() ?: false
                )
            }
            _uiState.value = _uiState.value.copy(
                estado = CotizacionEstado.CotizacionLista(Cotizacion(lineas = lineasConProductos))
            )
        }
    }

    fun toggleIncluirLinea(id: Int) {
        val estado = _uiState.value.estado as? CotizacionEstado.CotizacionLista ?: return
        _uiState.value = _uiState.value.copy(
            estado = estado.copy(
                cotizacion = estado.cotizacion.copy(
                    lineas = estado.cotizacion.lineas.map {
                        if (it.id == id) it.copy(ignorar = !it.ignorar) else it
                    }
                )
            )
        )
    }

    fun ajustarCantidad(id: Int, cantidad: Int) {
        val estado = _uiState.value.estado as? CotizacionEstado.CotizacionLista ?: return
        _uiState.value = _uiState.value.copy(
            estado = estado.copy(
                cotizacion = estado.cotizacion.copy(
                    lineas = estado.cotizacion.lineas.map {
                        if (it.id == id) it.copy(cantidad = cantidad.coerceAtLeast(1)) else it
                    }
                )
            )
        )
    }

    fun reiniciar() {
        _uiState.value = CotizacionUiState()
    }

    override fun onCleared() {
        super.onCleared()
        reconocedor.close()
    }

    companion object {
        fun factory(application: Application, repository: ProductRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return CotizacionViewModel(application, repository) as T
                }
            }
        }
    }
}