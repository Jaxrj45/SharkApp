package com.example.tibuapp.di

import android.content.Context
import com.example.tibuapp.data.local.AppDatabase
import com.example.tibuapp.data.repository.ProductRepository
import com.example.tibuapp.data.repository.UsuarioRepository
import com.example.tibuapp.data.repository.VentaRepository
import com.example.tibuapp.data.sync.GoogleSheetsService
import com.example.tibuapp.data.repository.SyncRepository
import com.example.tibuapp.data.repository.PreferenciasRepository


// Container = caja donde viven todas las dependencias
class AppContainer(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    val productoRepository = ProductRepository(database.productoDao())
    val usuarioRepository = UsuarioRepository(database.usuarioDao())
    val sheetsService = GoogleSheetsService(context)
    val preferenciasRepository = PreferenciasRepository(context)


    val ventaRepository = VentaRepository(
        ventaDao = database.ventaDao(),
        productoDao = database.productoDao()
    )

    val syncRepository = SyncRepository(
        context = context,
        productoRepository = productoRepository,
        sheetsService = sheetsService
    )



}