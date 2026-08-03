package com.example.tibuapp.ui.navigation

sealed class Pantalla(val ruta: String) {
    object Login : Pantalla("login")
    object MenuPrincipal : Pantalla("menu_principal")
    object Catalogo : Pantalla("catalogo") {
        fun crearRuta(codigoBarras: String? = null) =
            if (codigoBarras != null) "$ruta?codigoBarras=$codigoBarras" else ruta
    }
    object AgregarProducto : Pantalla("agregar_producto")
    object EditarProducto : Pantalla("editar_producto/{productoId}") {
        fun crearRuta(productoId: Int) = "editar_producto/$productoId"
    }
    object Escaner : Pantalla("escaner")
    object Carrito : Pantalla("carrito")
    object Cotizacion : Pantalla("cotizacion")
    object HistorialVentas : Pantalla("historial_ventas")
    object GestionUsuarios : Pantalla("gestion_usuarios")
    object OcrListas : Pantalla("ocr_listas")
    object Sync : Pantalla("sync")
    object Ajustes : Pantalla("ajustes")

}