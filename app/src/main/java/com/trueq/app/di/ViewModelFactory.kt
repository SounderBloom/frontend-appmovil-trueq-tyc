package com.trueq.app.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

/**
 * Crea un ViewModel pasándole las dependencias del AppContainer (api,
 * tokenManager), sin necesidad de Hilt/Dagger.
 */
@Composable
inline fun <reified VM : ViewModel> appViewModel(crossinline creator: (AppContainer) -> VM): VM {
    val container = LocalAppContainer.current
    return viewModel(
        factory = viewModelFactory {
            initializer { creator(container) }
        }
    )
}
