package com.example.tibuapp.di

import android.content.Context
import com.example.tibuapp.data.local.AppDatabase
import com.example.tibuapp.data.repository.ProductRepository
import com.example.tibuapp.data.repository.UsuarioRepository
import com.example.tibuapp.data.repository.VentaRepository

// Container = caja donde viven todas las dependencias
class AppContainer(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    val productoRepository = ProductRepository(database.productoDao())
    val usuarioRepository = UsuarioRepository(database.usuarioDao())

    val ventaRepository = VentaRepository(
        ventaDao = database.ventaDao(),
        productoDao = database.productoDao()
    )



}