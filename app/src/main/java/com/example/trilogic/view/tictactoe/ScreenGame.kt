package com.example.trilogic.view.tictactoe

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trilogic.model.*
import com.example.trilogic.viewmodel.TicTacToeViewModel

@Composable
fun ScreenGame(
    viewModel: TicTacToeViewModel = viewModel(),
    onBackClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Colors
    val gameBg = Color(0xFFF5F5F5)
    val accentBlue = Color(0xFF1976D2)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            android.util.Log.d("ScreenGame", "Lifecycle Event: $event")
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> { 
                    // Solo reanudamos automáticamente si el juego NO estaba pausado por el usuario o por haber salido
                    if (!viewModel.uiState.value.isPaused) {
                        viewModel.onEvent(TicTacToeEvent.OnResume) 
                    }
                }
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> { 
                    viewModel.onEvent(TicTacToeEvent.OnPause) 
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize().background(gameBg)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Ultimate Tic-Tac-Toe", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = accentBlue)

            // Timer display
            val minutes = state.timeElapsed / 60
            val seconds = state.timeElapsed % 60
            Text(
                text = "Tiempo: ${"%02d".format(minutes)}:${"%02d".format(seconds)}",
                fontSize = 18.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Text(
                state.message, 
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = if (state.isGameOver) Color.Red else Color.DarkGray, 
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Board Container
            Box(
                modifier = Modifier
                    .size(360.dp)
                    .border(2.dp, Color.Black)
            ) {
                // Main Grid (Visible when no zoom or background of zoom)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false
                ) {
                    items(9) { idx ->
                        val isPlayableSubBoard = state.nextSubBoardIndex == null || state.nextSubBoardIndex == idx
                        SubBoardItem(
                            data = state.mainBoard[idx],
                            isHighlighted = state.zoomedSubBoardIndex == null && isPlayableSubBoard,
                            onClick = { viewModel.onEvent(TicTacToeEvent.OnSubBoardClick(idx)) }
                        )
                    }
                }

                // Zoomed Overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = state.zoomedSubBoardIndex != null,
                    enter = scaleIn(animationSpec = tween(400)) + fadeIn(),
                    exit = scaleOut(animationSpec = tween(400)) + fadeOut()
                ) {
                    state.zoomedSubBoardIndex?.let { idx ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                                .border(3.dp, accentBlue)
                        ) {
                            SubBoardZoomed(
                                data = state.mainBoard[idx]
                            ) { cellIdx -> viewModel.onEvent(TicTacToeEvent.OnCellClick(idx, cellIdx)) }
                        }
                    }
                }
            }

            // Outside the board: Zoom control and Navigation
            Spacer(modifier = Modifier.height(16.dp))
            
            AnimatedVisibility(visible = state.zoomedSubBoardIndex != null) {
                Button(
                    onClick = { viewModel.onEvent(TicTacToeEvent.OnZoomOut) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Cerrar Zoom", color = Color.White)
                }
            }

            Button(
                onClick = { onBackClick() },
                colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
            ) {
                Text("Volver al inicio", color = Color.White) 
            }
        }

        // Pause Overlay
        if (state.isPaused && !state.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(enabled = false) {}, // Intercept clicks
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Juego Pausado", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.onEvent(TicTacToeEvent.OnResume) },
                            colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                        ) {
                            Text("Reanudar", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubBoardItem(data: SubBoardData, isHighlighted: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        if (data.winner != Player.NONE) data.winner.color.copy(0.3f)
        else if (data.isDrawn) Color.Gray.copy(0.3f)
        else if (isHighlighted) Color.Yellow.copy(0.2f) // Más visible si se puede jugar
        else Color.Black.copy(0.05f), // Muy apagado si está bloqueado
        label = "bg"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(1.dp, if (isHighlighted) Color.Black else Color.LightGray)
            .background(bgColor)
            .clickable(enabled = isHighlighted) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (data.winner != Player.NONE) {
            Text(
                text = data.winner.symbol,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = data.winner.color
            )
        } else if (data.isDrawn) {
            Text("=", fontSize = 40.sp, color = Color.Gray)
        } else {
            // MINI PREVIEW OF CELLS (Improved Visibility)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                userScrollEnabled = false,
                modifier = Modifier.padding(2.dp)
            ) {
                items(9) { cIdx ->
                    val cell = data.cells[cIdx]
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .border(0.2.dp, Color.Gray.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (cell.symbol.isNotEmpty()) {
                            Text(
                                text = cell.symbol,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = cell.color
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubBoardZoomed(data: SubBoardData, onCellClick: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
        ) {
            items(9) { idx ->
                val cell = data.cells[idx]
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .border(1.dp, Color.Gray)
                        .clickable { onCellClick(idx) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cell.symbol,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = cell.color
                    )
                }
            }
        }
    }
}
