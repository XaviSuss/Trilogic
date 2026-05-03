package com.example.trilogic.view.tictactoe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trilogic.model.Difficulty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenChoice(
    onSideSelected: (String) -> Unit,
    onDifficultySelected: (Difficulty) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.EASY) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Configuración de Partida",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Dropdown for Difficulty
        Text(
            text = "Dificultad:",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp),
            color = Color.DarkGray
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(bottom = 48.dp)
        ) {
            OutlinedTextField(
                value = selectedDifficulty.name,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                Difficulty.values().forEach { difficulty ->
                    DropdownMenuItem(
                        text = { Text(text = difficulty.name) },
                        onClick = {
                            selectedDifficulty = difficulty
                            onDifficultySelected(difficulty)
                            expanded = false
                        }
                    )
                }
            }
        }

        Text(
            text = "Elige tu símbolo",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Button X
            Button(
                onClick = { onSideSelected("X") },
                modifier = Modifier.size(100.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Text(text = "X", fontSize = 48.sp, color = Color.Red, fontWeight = FontWeight.Black)
            }

            // Button O
            Button(
                onClick = { onSideSelected("O") },
                modifier = Modifier.size(100.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Text(text = "O", fontSize = 48.sp, color = Color.Blue, fontWeight = FontWeight.Black)
            }
        }
    }
}
