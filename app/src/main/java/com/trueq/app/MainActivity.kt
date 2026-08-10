package com.trueq.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.trueq.app.di.LocalAppContainer
import com.trueq.app.navigation.TrueQNavHost
import com.trueq.app.ui.theme.TrueQTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as TrueQApplication).container

        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                TrueQTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        TrueQNavHost()
                    }
                }
            }
        }
    }
}
