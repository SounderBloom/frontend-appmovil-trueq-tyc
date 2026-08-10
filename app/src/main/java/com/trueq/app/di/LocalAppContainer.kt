package com.trueq.app.di

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer no fue provisto. Usa LocalAppContainer.provides en la raíz de la app.")
}
