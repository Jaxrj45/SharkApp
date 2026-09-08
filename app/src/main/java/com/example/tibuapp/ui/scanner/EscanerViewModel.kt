package com.example.tibuapp.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tibuapp.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EscanerViewModel(
    private val repository: ProductRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(EscanerUiState())
    val uiState: StateFlow<EscanerUiState> = _uiState.asStateFlow()

    fun procesarCodigo(codigo: String){
        if(_uiState.value.cargando) return

        _uiState.value = _uiState.value.copy(
            cargando = true,
            codigoDetectado = codigo,
            noEncontrado = false,
            productoEncontrado = null
        )

        viewModelScope.launch {
            val producto = repository.obtenerPorCodigoBarras(codigo)
            if(producto !=null){
                _uiState.value = _uiState.value.copy(
                    productoEncontrado = producto,
                    noEncontrado = false,
                    escaneando = false,
                    cargando = false

                )
            }else{
                _uiState.value = _uiState.value.copy(
                    noEncontrado = true,
                    productoEncontrado = null,
                    escaneando = false,
                    cargando = false
                )
            }
        }
    }

    fun reiniciarEscaner(){
        _uiState.value = EscanerUiState()
    }

    companion object {
        fun factory(repository: ProductRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return EscanerViewModel(repository) as T
                }
            }
        }
    }

}