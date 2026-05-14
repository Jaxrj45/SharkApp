package com.example.tibuapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tibuapp.TibuApp
import com.example.tibuapp.ui.navigation.Pantalla
import androidx.compose.ui.platform.LocalContext
import com.example.tibuapp.ui.screens.Producto.ProductoViewModel

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as TibuApp

    val productoViewModel: ProductoViewModel = viewModel(
        factory = ProductoViewModel.factory(app.container.productoRepository)
    )

    NavHost(
        navController = navController,
        startDestination = Pantalla.Login.ruta
    ) {
        composable(Pantalla.Login.ruta) {
            PantallaLoginEsqueleto(
                onLoginExitoso = {
                    navController.navigate(Pantalla.MenuPrincipal.ruta) {
                        popUpTo(Pantalla.Login.ruta) { inclusive = true }
                    }
                }
            )
        }

        composable(Pantalla.MenuPrincipal.ruta) {
            PantallaMenuEsqueleto(navController)
        }

        composable(Pantalla.Catalogo.ruta) {
            CatalogoScreen(
                productoViewModel = productoViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}