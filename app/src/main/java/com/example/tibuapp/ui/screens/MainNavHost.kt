package com.example.tibuapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tibuapp.TibuApp
import com.example.tibuapp.ui.navigation.Pantalla
import androidx.compose.ui.platform.LocalContext
import com.example.tibuapp.ui.scanner.EscanerViewModel
import com.example.tibuapp.ui.screens.Producto.ProductoViewModel
import com.example.tibuapp.ui.viewmodel.AsistenteViewModel
import com.example.tibuapp.ui.viewmodel.CotizacionViewModel
import com.example.tibuapp.ui.viewmodel.PreferenciasViewModel
import com.example.tibuapp.ui.viewmodel.SyncViewModel

@Composable
fun MainNavHost(preferenciasViewModel: PreferenciasViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as TibuApp

    val productoViewModel: ProductoViewModel = viewModel(
        factory = ProductoViewModel.factory(app.container.productoRepository)
    )

    val escanerViewModel: EscanerViewModel = viewModel(
        factory = EscanerViewModel.factory(app.container.productoRepository)
    )

    val asistenteViewModel: AsistenteViewModel = viewModel(
        factory = AsistenteViewModel.factory(
            application = context.applicationContext as android.app.Application,
            repository = app.container.productoRepository
        )
    )

    val cotizacionViewModel: CotizacionViewModel = viewModel(
        factory = CotizacionViewModel.factory(
            application = context.applicationContext as android.app.Application,
            repository = app.container.productoRepository
        )
    )

    // ViewModel
    val syncViewModel: SyncViewModel = viewModel(
        factory = SyncViewModel.factory(
            syncRepository = app.container.syncRepository,
            productoRepository = app.container.productoRepository
        )
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
            PantallaMenuEsqueleto(
                navController = navController,
                asistenteViewModel = asistenteViewModel
            )
        }

        composable(Pantalla.Ajustes.ruta) {
            AjustesScreen(
                preferenciasViewModel = preferenciasViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }


        //  Una sola ruta para el catálogo — con codigoBarras opcional
        composable(
            route = "${Pantalla.Catalogo.ruta}?codigoBarras={codigoBarras}",
            arguments = listOf(
                navArgument("codigoBarras") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val codigoBarras = backStackEntry.arguments?.getString("codigoBarras")
            CatalogoScreen(
                productoViewModel = productoViewModel,
                asistenteViewModel = asistenteViewModel, // ✅ agregado
                codigoBarrasInicial = codigoBarras,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Pantalla.Escaner.ruta) {
            EscanerScreen(
                escanearViewModel = escanerViewModel,
                onNavigateBack = { navController.popBackStack() },
                onAgregarAlCarrito = { producto -> /* Fase del carrito */ },
                onRegistrarProducto = { codigo ->
                    navController.navigate(Pantalla.Catalogo.crearRuta(codigoBarras = codigo)) {
                        popUpTo(Pantalla.Escaner.ruta) { inclusive = true }
                    }
                }
            )
        }

        composable(Pantalla.Cotizacion.ruta) {
            CotizacionScreen(
                cotizacionViewModel = cotizacionViewModel,
                asistenteViewModel = asistenteViewModel,
                onNavigateBack = { navController.popBackStack() },
                onCompartirWhatsApp = { texto ->
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, texto)
                        setPackage("com.whatsapp")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // WhatsApp no instalado — compartir con cualquier app
                        val intentGeneral = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, texto)
                        }
                        context.startActivity(android.content.Intent.createChooser(intentGeneral, "Compartir cotización"))
                    }
                },
                onGenerarPDF = { texto ->
                    // por implementar
                }
            )
        }

        // Ruta
        composable(Pantalla.Sync.ruta) {
            SyncScreen(
                syncViewModel = syncViewModel,
                asistenteViewModel = asistenteViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}