package com.example.trilogic.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trilogic.viewmodel.LoginUiState

@Composable
fun ScreenLogin(
    state: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    // Custom Colors
    val deepBlue = Color(0xFF1A237E)
    val lightCyan = Color(0xFFE0F7FA)
    val buttonGreen = Color(0xFF2E7D32)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lightCyan)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Trilogic Login",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = deepBlue,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (state.message.isNotEmpty()) {
            Text(
                text = state.message,
                color = Color.DarkGray,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.7f))
                    .padding(8.dp)
                    .fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = state.username,
            onValueChange = onUsernameChange,
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = onRegisterClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Registrar")
            }
            
            Button(
                onClick = onLoginClick,
                colors = ButtonDefaults.buttonColors(containerColor = buttonGreen)
            ) {
                Text("Entrar", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onCloseClick) {
            Text("Cerrar Aplicación", color = Color.Red)
        }

        if (state.errorMsg.isNotEmpty()) {
            Text(
                text = state.errorMsg,
                color = Color.White,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .background(Color.Red.copy(alpha = 0.8f))
                    .padding(8.dp)
                    .fillMaxWidth()
            )
        }
    }
}
