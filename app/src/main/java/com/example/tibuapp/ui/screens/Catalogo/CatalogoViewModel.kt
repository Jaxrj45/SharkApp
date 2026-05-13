package com.example.tibuapp.ui.screens.Catalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tibuapp.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CatalogoViewModel (private val productRepository: ProductRepository): ViewModel(){

    private val _uiState = MutableStateFlow(CatalogoUiState())
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    init{
        cargarProductos()
    }

    private fun cargarProductos(){
        viewModelScope.launch {
            productRepository.obtenerTodos().collect { listaProductos -> _uiState.value = _uiState.value.copy(
                productos = listaProductos,
                estaCargando = false
            ) }
        }
    }

    fun cambiarTextoBusqueda(texto: String){
        _uiState.value = _uiState.value.copy(textoBusqueda = texto)

        viewModelScope.launch {
            if(texto.isBlank()){
                productRepository.obtenerTodos().collect { listaProductos->
                    _uiState.value = _uiState.value.copy(productos = listaProductos)
                }
            }else{
                productRepository.buscar(texto)?.collect { listaFiltrada ->
                    _uiState.value = _uiState.value.copy(productos = listaFiltrada)
                }
            }
        }
    }
}