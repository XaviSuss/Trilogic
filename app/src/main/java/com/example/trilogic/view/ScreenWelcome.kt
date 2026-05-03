package com.example.trilogic.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScreenWelcome(
    msgWelcome: String,
    onLogoutClick: () -> Unit,
    onCloseApp: () -> Unit,
    onStartGame: () -> Unit,
) {
    // Custom Palette
    val lightCyan = Color(0xFFE0F7FA)
    val deepBlue = Color(0xFF1A237E)
    val accentCoral = Color(0xFFFF5722)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lightCyan)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = msgWelcome,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(deepBlue)
                .padding(20.dp)
                .fillMaxWidth()
        )

        Button(
            onClick = onStartGame,
            modifier = Modifier
                .padding(vertical = 32.dp)
                .fillMaxWidth(0.8f),
            colors = ButtonDefaults.buttonColors(containerColor = accentCoral)
        ) {
            Text(
                text = "Jugar al TicTacToe",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = onLogoutClick,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text("Cambiar Usuario")
        }

        Button(
            onClick = onCloseApp,
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("Cerrar")
        }
    }
}
