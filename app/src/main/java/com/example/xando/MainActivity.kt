package com.example.xando

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var gridLayout: GridLayout
    private lateinit var resetButton: Button
    private lateinit var playerTurnText: TextView
    private lateinit var gameModeText: TextView

    private var playerTurn = true // true for Player 1 (X), false for Player 2/O
    private var gameState = Array(9) { "" }
    private var gameActive = true
    private var gameMode = "OFFLINE_MULTI"
    private var difficulty = "MEDIUM"
    private var isPlayerTurn = true

    companion object {
        const val PLAYER_X = "X"
        const val PLAYER_O = "O"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupGameMode()
        initializeBoard()
        setupClickListeners()
        updateUI()
    }

    private fun initializeViews() {
        gridLayout = findViewById(R.id.gridLayout)
        resetButton = findViewById(R.id.resetButton)
        playerTurnText = findViewById(R.id.playerTurnText)
        gameModeText = findViewById(R.id.gameModeText)
    }

    private fun setupGameMode() {
        gameMode = intent.getStringExtra("GAME_MODE") ?: "OFFLINE_MULTI"
        difficulty = intent.getStringExtra("DIFFICULTY") ?: "MEDIUM"

        gameModeText.text = when (gameMode) {
            "SOLO" -> "Solo Mode - $difficulty"
            "OFFLINE_MULTI" -> "Two Player Mode"
            else -> "Two Player Mode"
        }
    }

    private fun initializeBoard() {
        gridLayout.removeAllViews()
        for (i in 0 until 9) {
            val button = Button(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    rowSpec = GridLayout.spec(i / 3, 1f)
                    columnSpec = GridLayout.spec(i % 3, 1f)
                }
                textSize = 24f
                text = ""
            }
            gridLayout.addView(button)
        }
        resetGameState()
    }

    private fun setupClickListeners() {
        for (i in 0 until gridLayout.childCount) {
            val button = gridLayout.getChildAt(i) as Button
            button.setOnClickListener {
                if (gameActive) {
                    onCellClick(i, button)
                }
            }
        }

        resetButton.setOnClickListener {
            resetGame()
        }
    }

    private fun onCellClick(index: Int, button: Button) {
        if (gameState[index].isNotEmpty()) {
            Toast.makeText(this, "Cell is already occupied", Toast.LENGTH_SHORT).show()
            return
        }

        when (gameMode) {
            "SOLO" -> handleSoloMove(index, button)
            "OFFLINE_MULTI" -> handleMultiplayerMove(index, button)
        }
    }

    private fun handleSoloMove(index: Int, button: Button) {
        if (!isPlayerTurn) return

        // Player's move
        makeMove(index, PLAYER_X, button)
        if (checkGameStatus()) return

        // AI's move
        isPlayerTurn = false
        updateUI()

        CoroutineScope(Dispatchers.Main).launch {
            delay(500) // Add delay for better UX
            makeAIMove()
            isPlayerTurn = true
            updateUI()
        }
    }

    private fun handleMultiplayerMove(index: Int, button: Button) {
        val currentPlayer = if (playerTurn) PLAYER_X else PLAYER_O
        makeMove(index, currentPlayer, button)
        playerTurn = !playerTurn
        updateUI()
        checkGameStatus()
    }

    private fun makeMove(index: Int, player: String, button: Button) {
        gameState[index] = player
        button.text = player
        button.isEnabled = false
    }

    private fun makeAIMove() {
        if (!gameActive) return

        val move = when (difficulty) {
            "EASY" -> getRandomMove()
            "MEDIUM" -> getMediumAIMove()
            "HARD" -> getHardAIMove()
            else -> getMediumAIMove()
        }

        move?.let { index ->
            val button = gridLayout.getChildAt(index) as Button
            makeMove(index, PLAYER_O, button)
            checkGameStatus()
        }
    }

    private fun getRandomMove(): Int? {
        val emptyCells = mutableListOf<Int>()
        for (i in gameState.indices) {
            if (gameState[i].isEmpty()) {
                emptyCells.add(i)
            }
        }
        return emptyCells.randomOrNull()
    }

    private fun getMediumAIMove(): Int? {
        // Try to win
        for (i in gameState.indices) {
            if (gameState[i].isEmpty()) {
                gameState[i] = PLAYER_O
                if (checkWinner() == PLAYER_O) {
                    gameState[i] = ""
                    return i
                }
                gameState[i] = ""
            }
        }

        // Block player
        for (i in gameState.indices) {
            if (gameState[i].isEmpty()) {
                gameState[i] = PLAYER_X
                if (checkWinner() == PLAYER_X) {
                    gameState[i] = ""
                    return i
                }
                gameState[i] = ""
            }
        }

        return getRandomMove()
    }

    private fun getHardAIMove(): Int? {
        var bestScore = Int.MIN_VALUE
        var bestMove: Int? = null

        for (i in gameState.indices) {
            if (gameState[i].isEmpty()) {
                gameState[i] = PLAYER_O
                val score = minimax(0, false)
                gameState[i] = ""

                if (score > bestScore) {
                    bestScore = score
                    bestMove = i
                }
            }
        }

        return bestMove ?: getRandomMove()
    }

    private fun minimax(depth: Int, isMaximizing: Boolean): Int {
        val winner = checkWinner()

        if (winner == PLAYER_O) return 10 - depth
        if (winner == PLAYER_X) return depth - 10
        if (gameState.none { it.isEmpty() }) return 0

        return if (isMaximizing) {
            var bestScore = Int.MIN_VALUE
            for (i in gameState.indices) {
                if (gameState[i].isEmpty()) {
                    gameState[i] = PLAYER_O
                    val score = minimax(depth + 1, false)
                    gameState[i] = ""
                    bestScore = maxOf(bestScore, score)
                }
            }
            bestScore
        } else {
            var bestScore = Int.MAX_VALUE
            for (i in gameState.indices) {
                if (gameState[i].isEmpty()) {
                    gameState[i] = PLAYER_X
                    val score = minimax(depth + 1, true)
                    gameState[i] = ""
                    bestScore = minOf(bestScore, score)
                }
            }
            bestScore
        }
    }

    private fun checkGameStatus(): Boolean {
        val winner = checkWinner()

        if (winner != null) {
            gameActive = false
            showToast("Player $winner wins!")
            resetButton.visibility = View.VISIBLE
            return true
        } else if (gameState.none { it.isEmpty() }) {
            gameActive = false
            showToast("It's a draw!")
            resetButton.visibility = View.VISIBLE
            return true
        }
        return false
    }

    private fun checkWinner(): String? {
        val winningPositions = arrayOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
        )

        for (positions in winningPositions) {
            if (gameState[positions[0]].isNotEmpty() &&
                gameState[positions[0]] == gameState[positions[1]] &&
                gameState[positions[0]] == gameState[positions[2]]) {
                return gameState[positions[0]]
            }
        }
        return null
    }

    private fun updateUI() {
        when (gameMode) {
            "SOLO" -> {
                playerTurnText.text = if (isPlayerTurn) "Your turn (X)" else "AI thinking..."
            }
            "OFFLINE_MULTI" -> {
                val currentPlayer = if (playerTurn) "X" else "O"
                playerTurnText.text = "Player $currentPlayer's turn"
            }
        }
    }

    private fun resetGameState() {
        gameState.fill("")
        gameActive = true
        playerTurn = true
        isPlayerTurn = true
        resetButton.visibility = View.INVISIBLE

        for (i in 0 until gridLayout.childCount) {
            val button = gridLayout.getChildAt(i) as Button
            button.text = ""
            button.isEnabled = true
        }
    }

    private fun resetGame() {
        resetGameState()
        updateUI()
        showToast("Game reset!")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}