package com.example.trilogic.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trilogic.model.*
import android.util.Log
import com.example.trilogic.network.GameApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class TicTacToeViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val _uiState = MutableStateFlow(UltimateTicTacToeUiState())
    val uiState = _uiState.asStateFlow()

    // S06: Multimedia control variables
    private var mediaPlayer: MediaPlayer? = null
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<Int, Int>()

    // S05: Hardware/Sensor variables
    private var sensorManagerApp: SensorManager? = null
    private var accelerometer: Sensor? = null
    
    // Triple-shake detection variables
    private var shakeCount = 0
    private var lastShakeTimestamp: Long = 0
    private var lastDirection: Float = 0f
    private val shakeThreshold = 12f // Acceleration threshold for left/right
    private val shakeTimeWindow = 1500L // Time window to complete 3 shakes (ms)
    private var timerJob: kotlinx.coroutines.Job? = null

    private val apiService = GameApiService.create()

    init {
        inicialitzarAudio()
        // S05: Prepare sensor manager safely
        inicialitzarSensors()
        startTimer()
        fetchWelcomeMessage()
    }

    private fun fetchWelcomeMessage() {
        viewModelScope.launch {
            try {
                val response = apiService.getWelcomeMessage()
                _uiState.value = _uiState.value.copy(message = response.message)
                Log.d("TicTacToeVM", "API Response: ${response.message}")
            } catch (e: Exception) {
                Log.e("TicTacToeVM", "Error en la conexión API: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    message = "Modo Offline: No se pudo conectar al servidor"
                )
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_uiState.value.isPaused && !_uiState.value.isGameOver) {
                    _uiState.value = _uiState.value.copy(timeElapsed = _uiState.value.timeElapsed + 1)
                }
            }
        }
    }

    private fun inicialitzarSensors() {
        // a) Obtenim el gestor de sensors fent servir el context segur de l'Application
        sensorManagerApp = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // b) Demanem el sensor d'acceleròmetre
        accelerometer = sensorManagerApp?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private fun inicialitzarAudio() {
        val context = getApplication<Application>()
        val res = context.resources
        val pkg = context.packageName

        // A. Background music with MediaPlayer
        // Using getIdentifier to avoid compile errors if files are missing
        val musicId = res.getIdentifier("wii", "raw", pkg)
        if (musicId != 0) {
            try {
                mediaPlayer = MediaPlayer.create(context, musicId)
                mediaPlayer?.isLooping = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // B. Sound effects with SoundPool
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // C. Load sounds and map them safely
        soundPool?.let { sp ->
            val s01 = res.getIdentifier("s01", "raw", pkg)
            if (s01 != 0) soundMap[1] = sp.load(context, s01, 1)

            val s02 = res.getIdentifier("s02", "raw", pkg)
            if (s02 != 0) soundMap[2] = sp.load(context, s02, 1)

            val s03 = res.getIdentifier("s03", "raw", pkg)
            if (s03 != 0) soundMap[3] = sp.load(context, s03, 1)

            val s04 = res.getIdentifier("s04", "raw", pkg)
            if (s04 != 0) soundMap[4] = sp.load(context, s04, 1)
        }
    }

    private fun reproduirSo(id: Int) {
        val soundId = soundMap[id]
        if (soundId != null && soundId != 0) {
            soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)
        }
    }

    private fun controlarMusicaFons(play: Boolean) {
        if (play) {
            if (mediaPlayer?.isPlaying == false) mediaPlayer?.start()
        } else {
            if (mediaPlayer?.isPlaying == true) mediaPlayer?.pause()
        }
    }

    fun onEvent(event: TicTacToeEvent) {
        when (event) {
            is TicTacToeEvent.OnCellClick -> handleCellClick(event.subBoardIndex, event.cellIndex)
            is TicTacToeEvent.OnSubBoardClick -> zoomIn(event.subBoardIndex)
            TicTacToeEvent.OnZoomOut -> zoomOut()
            TicTacToeEvent.OnRestartShake -> restartGame()
            TicTacToeEvent.OnBackClick -> {
                controlarMusicaFons(false)
            }
            TicTacToeEvent.OnPause -> pauseGame()
            TicTacToeEvent.OnResume -> resumeGame()
        }
    }

    private fun pauseGame() {
        Log.d("TicTacToeVM", "Pausando juego (isPaused = true)")
        _uiState.value = _uiState.value.copy(isPaused = true)
        controlarMusicaFons(false)
        disableSensor()
    }

    private fun resumeGame() {
        Log.d("TicTacToeVM", "Reanudando juego (isPaused = false)")
        _uiState.value = _uiState.value.copy(isPaused = false)
        controlarMusicaFons(true)
        enableSensor()
    }

    private fun zoomIn(index: Int) {
        if (_uiState.value.isGameOver) return
        reproduirSo(3) // Zoom In sound
        _uiState.value = _uiState.value.copy(zoomedSubBoardIndex = index)
    }

    private fun zoomOut() {
        reproduirSo(4) // Zoom Out sound
        _uiState.value = _uiState.value.copy(zoomedSubBoardIndex = null)
    }

    private fun handleCellClick(subIndex: Int, cellIndex: Int) {
        val currentState = _uiState.value
        if (currentState.isGameOver || currentState.isAiTurn || currentState.isPaused) return

        // Restriction: check if the move is in the required sub-board
        if (currentState.nextSubBoardIndex != null && subIndex != currentState.nextSubBoardIndex) {
            _uiState.value = currentState.copy(message = "Debes jugar en el tablero ${currentState.nextSubBoardIndex + 1}")
            return
        }

        val subBoard = currentState.mainBoard[subIndex]
        if (subBoard.winner != Player.NONE || subBoard.isDrawn || subBoard.cells[cellIndex].symbol.isNotEmpty()) return

        executeMove(subIndex, cellIndex)
        
        if (!_uiState.value.isGameOver) {
            viewModelScope.launch {
                delay(600)
                aiMove()
            }
        }
    }

    private fun executeMove(subIndex: Int, cellIndex: Int) {
        val currentState = _uiState.value
        val subBoard = currentState.mainBoard[subIndex]
        
        reproduirSo(1) // Click sound

        val newCells = subBoard.cells.toMutableList()
        newCells[cellIndex] = CellData(
            symbol = currentState.currentPlayer.symbol,
            color = currentState.currentPlayer.color,
        )

        val subBoardWinner = checkWin(newCells.map { it.symbol })
        val updatedSubBoard = subBoard.copy(
            cells = newCells,
            winner = if (subBoardWinner) currentState.currentPlayer else Player.NONE,
            isDrawn = !subBoardWinner && newCells.none { it.symbol.isEmpty() }
        )

        val newMainBoard = currentState.mainBoard.toMutableList()
        newMainBoard[subIndex] = updatedSubBoard

        val gameWinnerSymbol = checkWin(newMainBoard.map { it.winner.symbol })
        val isGameOver = gameWinnerSymbol || newMainBoard.none { it.winner == Player.NONE && !it.isDrawn }

        val nextPlayer = if (currentState.currentPlayer == Player.X) Player.O else Player.X

        // Restriction Logic: Determine the next valid sub-board
        val nextTargetSubBoard = newMainBoard[cellIndex]
        val canPlayInNext = nextTargetSubBoard.winner == Player.NONE && !nextTargetSubBoard.isDrawn
        val forcedSubBoardIndex = if (canPlayInNext) cellIndex else null

        _uiState.value = currentState.copy(
            mainBoard = newMainBoard,
            currentPlayer = nextPlayer,
            gameWinner = if (gameWinnerSymbol) currentState.currentPlayer else Player.NONE,
            isGameOver = isGameOver,
            message = if (isGameOver) "¡Ganador: ${currentState.currentPlayer.symbol}!" else "Turno de ${nextPlayer.symbol}",
            isAiTurn = !isGameOver && nextPlayer == Player.O,
            nextSubBoardIndex = forcedSubBoardIndex
        )

        if (subBoardWinner || gameWinnerSymbol) {
            reproduirSo(2) // Win sound
        }

        if (!isGameOver) {
            controlarMusicaFons(true)
        } else {
            controlarMusicaFons(false)
        }
    }

    private fun aiMove() {
        val currentState = _uiState.value
        if (currentState.isGameOver || !currentState.isAiTurn || currentState.isPaused) return

        // Use restriction logic for AI as well
        val targetSubBoardIndex = if (currentState.nextSubBoardIndex != null) {
            currentState.nextSubBoardIndex
        } else {
            currentState.mainBoard.indices.filter {
                currentState.mainBoard[it].winner == Player.NONE && !currentState.mainBoard[it].isDrawn
            }.randomOrNull()
        } ?: return

        val targetSubBoard = currentState.mainBoard[targetSubBoardIndex]
        val availableCells = targetSubBoard.cells.indices.filter { targetSubBoard.cells[it].symbol.isEmpty() }

        if (availableCells.isEmpty()) return

        val chosenCellIndex = when (currentState.difficulty) {
            Difficulty.EASY -> availableCells.random()
            Difficulty.MEDIUM -> findBestMove(targetSubBoard.cells, availableCells, Player.O) ?: availableCells.random()
            Difficulty.HARD -> findBestMove(targetSubBoard.cells, availableCells, Player.O) 
                              ?: findBestMove(targetSubBoard.cells, availableCells, Player.X) 
                              ?: availableCells.random()
        }

        executeMove(targetSubBoardIndex, chosenCellIndex)
    }

    private fun findBestMove(cells: List<CellData>, available: List<Int>, player: Player): Int? {
        for (index in available) {
            val tempCells = cells.toMutableList()
            tempCells[index] = CellData(symbol = player.symbol)
            if (checkWin(tempCells.map { it.symbol })) return index
        }
        return null
    }

    private fun checkWin(symbols: List<String>): Boolean {
        val patterns = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        return patterns.any { p ->
            symbols[p[0]].isNotEmpty() && symbols[p[0]] == symbols[p[1]] && symbols[p[1]] == symbols[p[2]]
        }
    }

    fun restartGame() {
        _uiState.value = UltimateTicTacToeUiState()
        controlarMusicaFons(true)
    }

    private fun resetCurrentSubBoard() {
        val currentState = _uiState.value
        val zoomedIndex = currentState.zoomedSubBoardIndex ?: return

        reproduirSo(4) // Zoom Out/Reset sound

        val newMainBoard = currentState.mainBoard.toMutableList()
        newMainBoard[zoomedIndex] = SubBoardData() // Reset only this sub-board

        _uiState.value = currentState.copy(
            mainBoard = newMainBoard,
            message = "Sub-tablero reiniciado"
        )
    }

    // S05: Sensor control methods
    fun enableSensor() {
        // Registrem l'escoltador només si el sensor existeix (operador ?.let)
        accelerometer?.let {
            sensorManagerApp?.registerListener(
                this, // qui escolta
                it,   // a qui s'escolta
                SensorManager.SENSOR_DELAY_UI // cada quant s'escolta
            )
        }
    }

    fun disableSensor() {
        // Parem d'escoltar per estalviar bateria
        sensorManagerApp?.unregisterListener(this)
    }

    fun resumeMusic() { controlarMusicaFons(true) }
    fun pauseMusic() { controlarMusicaFons(false) }

    fun setDifficulty(difficulty: Difficulty) {
        _uiState.value = _uiState.value.copy(difficulty = difficulty)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        // Obtenim l'acceleració en l'eix X (esquerra - dreta)
        val x = event.values[0]
        val now = System.currentTimeMillis()

        // Detectem un canvi de direcció brusc en l'eix X (sacsejada lateral)
        if (Math.abs(x) > shakeThreshold) {
            // Verifiquem si és una direcció diferent a l'anterior per comptar-la com a sacsejada nova
            // (Si x és positiu és un costat, si és negatiu és l'altre)
            val currentDirection = if (x > 0) 1f else -1f

            if (currentDirection != lastDirection) {
                // Si ha passat massa temps des de l'última sacsejada, reiniciem el comptador
                if (now - lastShakeTimestamp > shakeTimeWindow) {
                    shakeCount = 0
                }

                shakeCount++
                lastShakeTimestamp = now
                lastDirection = currentDirection

                // Si arribem a 3 sacsejades (esquerra-dreta-esquerra o viceversa)
                if (shakeCount >= 3) {
                    shakeCount = 0 // Reiniciem per a la següent vegada

                    if (_uiState.value.zoomedSubBoardIndex != null) {
                        resetCurrentSubBoard()
                    } else {
                        restartGame()
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(s: Sensor?, a: Int) {}

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
        soundPool?.release()
        soundPool = null
        disableSensor()
    }
}
