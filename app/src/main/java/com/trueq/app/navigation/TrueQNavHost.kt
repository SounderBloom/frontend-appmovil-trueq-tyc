package com.trueq.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.trueq.app.AppViewModel
import com.trueq.app.di.appViewModel
import com.trueq.app.ui.screens.auth.LoginScreen
import com.trueq.app.ui.screens.auth.RegisterScreen
import com.trueq.app.ui.screens.chat.ChatConversationScreen
import com.trueq.app.ui.screens.chat.ChatListScreen
import com.trueq.app.ui.screens.home.HomeScreen
import com.trueq.app.ui.screens.notifications.NotificationsScreen
import com.trueq.app.ui.screens.product.NewProductScreen
import com.trueq.app.ui.screens.product.ProductDetailScreen
import com.trueq.app.ui.screens.profile.ProfileScreen
import com.trueq.app.ui.screens.search.SearchScreen

@Composable
fun TrueQNavHost(navController: NavHostController = rememberNavController()) {

    val appViewModel = appViewModel { AppViewModel(it.api, it.tokenManager) }
    val estaAutenticado by appViewModel.estaAutenticado.collectAsState()
    val noLeidas by appViewModel.noLeidas.collectAsState()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route?.substringBefore("?")?.substringBefore("/{")

    // Espera a saber si hay sesión activa (equivalente al guard del router
    // en el sitio web) antes de decidir la pantalla inicial.
    if (estaAutenticado == null) return

    NavHost(
        navController = navController,
        startDestination = if (estaAutenticado == true) Routes.HOME else Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginExitoso = {
                    appViewModel.refrescarSesion()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onIrARegistro = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegistroExitoso = { navController.popBackStack() },
                onIrALogin = { navController.popBackStack() }
            )
        }

        composable(Routes.HOME) {
            LaunchedEffect(Unit) { appViewModel.refrescarNoLeidas() }
            HomeScreen(
                rutaActual = rutaActual ?: Routes.HOME,
                noLeidas = noLeidas,
                onNavegar = { ruta -> navegarDesdeTab(navController, ruta) },
                onAbrirNotificaciones = { navController.navigate(Routes.NOTIFICACIONES) },
                onAbrirProducto = { id -> navController.navigate(Routes.detalleProducto(id)) },
                onBuscar = { termino -> navController.navigate("${Routes.BUSCAR}?q=$termino") }
            )
        }

        composable(
            "${Routes.BUSCAR}?q={q}",
            arguments = listOf(navArgument("q") { type = NavType.StringType; defaultValue = "" })
        ) { entry ->
            SearchScreen(
                rutaActual = rutaActual ?: Routes.BUSCAR,
                terminoInicial = entry.arguments?.getString("q") ?: "",
                onVolver = { navController.popBackStack() },
                onNavegar = { ruta -> navegarDesdeTab(navController, ruta) },
                onAbrirProducto = { id -> navController.navigate(Routes.detalleProducto(id)) }
            )
        }

        composable(Routes.MENSAJES) {
            LaunchedEffect(Unit) { appViewModel.refrescarNoLeidas() }
            ChatListScreen(
                rutaActual = rutaActual ?: Routes.MENSAJES,
                noLeidas = noLeidas,
                onNavegar = { ruta -> navegarDesdeTab(navController, ruta) },
                onAbrirNotificaciones = { navController.navigate(Routes.NOTIFICACIONES) },
                onAbrirChat = { chatId -> navController.navigate(Routes.chat(chatId)) }
            )
        }

        composable(
            Routes.CHAT_PATTERN,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("proponer") { type = NavType.StringType; defaultValue = "0" }
            )
        ) { entry ->
            ChatConversationScreen(
                chatId = entry.arguments?.getString("chatId") ?: "",
                autoAbrirOferta = entry.arguments?.getString("proponer") == "1",
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Routes.NOTIFICACIONES) {
            NotificationsScreen(
                rutaActual = rutaActual ?: Routes.NOTIFICACIONES,
                onVolver = { navController.popBackStack() },
                onNavegar = { ruta -> navegarDesdeTab(navController, ruta) },
                onAbrirChat = { chatId -> navController.navigate(Routes.chat(chatId)) },
                onNotificacionesActualizadas = { appViewModel.refrescarNoLeidas() }
            )
        }

        composable(Routes.PERFIL) {
            LaunchedEffect(Unit) { appViewModel.refrescarNoLeidas() }
            ProfileScreen(
                rutaActual = rutaActual ?: Routes.PERFIL,
                noLeidas = noLeidas,
                onNavegar = { ruta -> navegarDesdeTab(navController, ruta) },
                onAbrirNotificaciones = { navController.navigate(Routes.NOTIFICACIONES) },
                onAbrirProducto = { id -> navController.navigate(Routes.detalleProducto(id)) },
                onSesionCerrada = {
                    appViewModel.refrescarSesion()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.NUEVO_PRODUCTO) {
            NewProductScreen(
                rutaActual = rutaActual ?: Routes.NUEVO_PRODUCTO,
                onVolver = { navController.popBackStack() },
                onNavegar = { ruta -> navegarDesdeTab(navController, ruta) },
                onPublicado = { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } }
            )
        }

        composable(
            Routes.DETALLE_PRODUCTO_PATTERN,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            ProductDetailScreen(
                productoId = entry.arguments?.getString("id") ?: "",
                onVolver = { navController.popBackStack() },
                onIrAlChat = { chatId, proponer -> navController.navigate(Routes.chat(chatId, proponer)) }
            )
        }
    }
}

/** Navegación entre las pestañas de la barra inferior, evitando apilar pantallas repetidas. */
private fun navegarDesdeTab(navController: NavHostController, ruta: String) {
    navController.navigate(ruta) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
