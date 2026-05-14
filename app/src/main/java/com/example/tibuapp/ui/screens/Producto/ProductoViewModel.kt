package com.example.tibuapp.ui.screens.Producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tibuapp.data.local.entity.ProductoEntity
import com.example.tibuapp.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductoViewModel(private val repository: ProductRepository): ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState()) //para hacer los cambios en mi vista
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()// para mostrar los cambios


    init {
        cargarProductos()
    }

    private fun cargarProductos(){
        viewModelScope.launch {
            repository.obtenerTodos().collect { listaProductos ->
                _uiState.value = _uiState.value.copy(
                    productos = listaProductos,
                    cargando = false
                )
            }
        }
    }

    private fun mostrarPorCategoria(categoria: String){
        viewModelScope.launch {
            repository.buscarPorCategoria(categoria).collect { p ->
                _uiState.value = _uiState.value.copy(
                    productos = p
                )
            }
        }
    }

    fun buscar(texto: String){
        _uiState.value = _uiState.value.copy(busqueda = texto)
        viewModelScope.launch {
            if(texto.isBlank()){
                repository.obtenerTodos().collect { listaProductos ->
                    _uiState.value = _uiState.value.copy(
                        productos = listaProductos
                    )
                }
            }else{
                repository.buscar(texto)?.collect { productoE ->
                    _uiState.value = _uiState.value.copy(productos = productoE)
                }
            }
        }
    }

    fun camposVacios(campo: String){
        _uiState.value = _uiState.value.copy(
            error = "Debe ingresar ${campo} del Producto")
        return
    }

    fun guardarProducto(
        id: Int =0,
        nombre: String,
        descripcion: String,
        precio: String,
        stock: String,
        categoria: String,
        codigoBarras: String
    ){

        //validar no guardar al presionar dos veces el boton en un lapso corto
        //validacion de campos en blanco
        if(nombre.isBlank()) return camposVacios("nombre")
        if(descripcion.isBlank()) return camposVacios("descripcion")
        if(categoria.isBlank()) return camposVacios("categoria")

        //validacion de cantidades correctas
        val precioDouble = precio.toDoubleOrNull()
        if(precioDouble== null || precioDouble <=0){
            _uiState.value = _uiState.value.copy(error = "Precio inválido")
            return
        }
        val stockInt = stock.toIntOrNull()
        if(stockInt == null || stockInt <=0){
            _uiState.value = _uiState.value.copy(
                error = "Stock invalida")
            return
        }

        viewModelScope.launch {

            val nuevoProducto = ProductoEntity(
                id= id,
                nombre = nombre.trim(),
                descripcion = descripcion.trim(),
                precio = precioDouble,
                stock = stockInt,
                codigoBarras = codigoBarras.trim(),
                categoria = categoria.trim()
            )

            if(id==0){
                val resultado = repository.insertar(nuevoProducto)
                if(resultado.isSuccess){

                    _uiState.value = _uiState.value.copy(
                        mensajeExito = "Producto Agregado Exitosamente",
                        error = null,
                    )
                }else{
                    _uiState.value = _uiState.value.copy(
                        error = "Error al guardar ${resultado.exceptionOrNull()?.message}",
                    )
                }
            }else{
                repository.actualizar(nuevoProducto)
                _uiState.value = _uiState.value.copy(
                    mensajeExito = "Producto Actualizado Correctamente",
                    error = null
                )
            }
        }
    }

    fun desactivarProducto(id: Int){
        viewModelScope.launch {
            repository.desactivar(id)
            _uiState.value = _uiState.value.copy(
                mensajeExito = "Producto Eliminado"
            )
        }
    }

    fun limpiarMensajes(){
        _uiState.value = _uiState.value.copy(
            error = null,
            mensajeExito = null
        )
    }

    companion object {
        fun factory(repository: ProductRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return ProductoViewModel(repository) as T
                }
            }
        }
    }

}