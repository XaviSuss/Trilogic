package com.example.trilogic.model
import androidx.compose.ui.graphics.Color

enum class BoardCell(val symbol: String, val color: Color) {
    X("X", Color.Red),
    O("O", Color.Blue),
    EMPTY("", Color.Transparent)
}