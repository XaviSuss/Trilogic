package com.example.trilogic.model

import androidx.compose.ui.graphics.Color

enum class Player(val symbol: String, val color: Color) {
    X("X", Color.Red),
    O("O", Color.Blue),
    NONE("", Color.Transparent)
}

data class CellData(
    val symbol: String = "",
    val color: Color = Color.Black,
    val isWinCell: Boolean = false
)

data class SubBoardData(
    val cells: List<CellData> = List(9) { CellData() },
    val winner: Player = Player.NONE,
    val isPlayable: Boolean = true,
    val isDrawn: Boolean = false
)

data class UltimateTicTacToeUiState(
    val mainBoard: List<SubBoardData> = List(9) { SubBoardData() },
    val currentPlayer: Player = Player.X,
    val gameWinner: Player = Player.NONE,
    val message: String = "Turno de X",
    val nextSubBoardIndex: Int? = null,
    val isGameOver: Boolean = false,
    val zoomedSubBoardIndex: Int? = null, // Sub-board currently in focus (zoomed)
    val isAiTurn: Boolean = false,
    val difficulty: Difficulty = Difficulty.EASY,
    val timeElapsed: Long = 0, // Time in seconds
    val isPaused: Boolean = false
)

enum class Difficulty {
    EASY, MEDIUM, HARD
}

sealed interface TicTacToeEvent {
    data class OnCellClick(val subBoardIndex: Int, val cellIndex: Int) : TicTacToeEvent
    data class OnSubBoardClick(val subBoardIndex: Int) : TicTacToeEvent // For zooming in
    object OnZoomOut : TicTacToeEvent
    object OnRestartShake : TicTacToeEvent
    object OnBackClick : TicTacToeEvent
    object OnPause : TicTacToeEvent
    object OnResume : TicTacToeEvent
}
