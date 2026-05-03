package com.example.trilogic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.trilogic.navigation.AppNavigation
import com.example.trilogic.ui.theme.TrilogicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrilogicTheme {
                // Aquí llamamos a la navegación principal
                AppNavigation(
                    onCloseApp = ::finalitzarAplicacio
                )
            }
        }
    }

    // Función para cerrar la app (como hace tu profe)
    private fun finalitzarAplicacio() {
        this.finish()
    }
}