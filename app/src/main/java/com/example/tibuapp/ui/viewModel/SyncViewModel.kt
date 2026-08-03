package com.example.tibuapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tibuapp.data.local.entity.ProductoEntity
import com.example.tibuapp.data.model.DiferenciaProducto
import com.example.tibuapp.data.model.ResultadoSync
import com.example.tibuapp.data.repository.ProductRepository
import com.example.tibuapp.data.repository.SyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class SyncEstado {
    object Inicial : SyncEstado()
    object Sincronizando : SyncEstado()
    data class MostrandoDiferencias(
        val resultado: ResultadoSync,
        val nuevosSeleccionados: Set<Int> = emptySet(),
        val modificadosSeleccionados: Set<Int> = emptySet()
    ) : SyncEstado()
    data class Aplicando(val progreso: Int, val total: Int) : SyncEstado()
    data class Completado(
        val agregados: Int,
        val actualizados: Int
    ) : SyncEstado()
    data class Error(val mensaje: String) : SyncEstado()
}

class SyncViewModel(
    private val syncRepository: SyncRepository,
    private val productoRepository: ProductRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<SyncEstado>(SyncEstado.Inicial)
    val estado: StateFlow<SyncEstado> = _estado.asStateFlow()

    fun sincronizar() {
        viewModelScope.launch {
            _estado.value = SyncEstado.Sincronizando
            val resultado = syncRepository.sincronizar()
            if (resultado.isSuccess) {
                val sync = resultado.getOrNull()!!
                _estado.value = SyncEstado.MostrandoDiferencias(
                    resultado = sync,
                    // Por defecto todos los nuevos y modificados están seleccionados
                    nuevosSeleccionados = sync.nuevos.indices.toSet(),
                    modificadosSeleccionados = sync.modificados.indices.toSet()
                )
            } else {
                _estado.value = SyncEstado.Error(
                    resultado.exceptionOrNull()?.message ?: "Error desconocido"
                )
            }
        }
    }

    fun toggleNuevo(index: Int) {
        val estado = _estado.value as? SyncEstado.MostrandoDiferencias ?: return
        val seleccionados = estado.nuevosSeleccionados.toMutableSet()
        if (index in seleccionados) seleccionados.remove(index)
        else seleccionados.add(index)
        _estado.value = estado.copy(nuevosSeleccionados = seleccionados)
    }

    fun toggleModificado(index: Int) {
        val estado = _estado.value as? SyncEstado.MostrandoDiferencias ?: return
        val seleccionados = estado.modificadosSeleccionados.toMutableSet()
        if (index in seleccionados) seleccionados.remove(index)
        else seleccionados.add(index)
        _estado.value = estado.copy(modificadosSeleccionados = seleccionados)
    }

    fun seleccionarTodosNuevos(seleccionar: Boolean) {
        val estado = _estado.value as? SyncEstado.MostrandoDiferencias ?: return
        _estado.value = estado.copy(
            nuevosSeleccionados = if (seleccionar)
                estado.resultado.nuevos.indices.toSet()
            else emptySet()
        )
    }

    fun seleccionarTodosModificados(seleccionar: Boolean) {
        val estado = _estado.value as? SyncEstado.MostrandoDiferencias ?: return
        _estado.value = estado.copy(
            modificadosSeleccionados = if (seleccionar)
                estado.resultado.modificados.indices.toSet()
            else emptySet()
        )
    }

    fun aplicarCambios() {
        val estadoActual = _estado.value as? SyncEstado.MostrandoDiferencias ?: return
        viewModelScope.launch {
            val nuevosAplicar = estadoActual.nuevosSeleccionados
                .map { estadoActual.resultado.nuevos[it] }
            val modificadosAplicar = estadoActual.modificadosSeleccionados
                .map { estadoActual.resultado.modificados[it] }
            val total = nuevosAplicar.size + modificadosAplicar.size

            _estado.value = SyncEstado.Aplicando(0, total)

            val productosLocales = productoRepository.obtenerTodos().first()
            syncRepository.aplicarNuevos(nuevosAplicar)
            syncRepository.aplicarModificados(modificadosAplicar, productosLocales)

            _estado.value = SyncEstado.Completado(
                agregados = nuevosAplicar.size,
                actualizados = modificadosAplicar.size
            )
        }
    }

    fun reiniciar() {
        _estado.value = SyncEstado.Inicial
    }

    companion object {
        fun factory(
            syncRepository: SyncRepository,
            productoRepository: ProductRepository
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return SyncViewModel(syncRepository, productoRepository) as T
                }
            }
        }
    }
}