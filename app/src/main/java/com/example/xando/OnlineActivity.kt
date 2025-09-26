package com.example.xando

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class OnlineActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var gridLayout: GridLayout
    private lateinit var resetButton: Button
    private lateinit var statusText: TextView
    private lateinit var playerTurnText: TextView

    private var gameId: String = ""
    private var playerSymbol = "X"
    private var isPlayerTurn = false
    private var gameState = Array(9) { "" }
    private var gameActive = false
    private var playerName = "Player_${UUID.randomUUID().toString().substring(0, 5)}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online)

        initializeViews()
        initializeBoard()
        setupFirebase()
        setupClickListeners()
    }

    private fun initializeViews() {
        gridLayout = findViewById(R.id.gridLayout)
        resetButton = findViewById(R.id.resetButton)
        statusText = findViewById(R.id.statusText)
        playerTurnText = findViewById(R.id.playerTurnText)
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
                isEnabled = false
            }
            gridLayout.addView(button)
        }
    }

    private fun setupFirebase() {
        dbRef = FirebaseDatabase.getInstance().reference
        createOrJoinGame()
    }

    private fun createOrJoinGame() {
        statusText.text = "Finding game..."

        dbRef.child("games").orderByChild("player2").equalTo("").limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Join existing game
                        snapshot.children.first().let { gameSnapshot ->
                            gameId = gameSnapshot.key!!
                            joinGame(gameId)
                        }
                    } else {
                        // Create new game
                        createNewGame()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    statusText.text = "Error finding game"
                }
            })
    }

    private fun createNewGame() {
        gameId = UUID.randomUUID().toString()
        playerSymbol = "X"
        isPlayerTurn = true

        val gameData = mapOf(
            "player1" to playerName,
            "player2" to "",
            "currentTurn" to "X",
            "board" to gameState.toList(),
            "status" to "waiting"
        )

        dbRef.child("games").child(gameId).setValue(gameData)
        statusText.text = "Waiting for opponent..."
        setupGameListeners()
    }

    private fun joinGame(gameId: String) {
        this.gameId = gameId
        playerSymbol = "O"
        isPlayerTurn = false

        dbRef.child("games").child(gameId).child("player2").setValue(playerName)
        dbRef.child("games").child(gameId).child("status").setValue("active")
        statusText.text = "Game started! You are O"
        setupGameListeners()
    }

    private fun setupGameListeners() {
        dbRef.child("games").child(gameId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                val board = snapshot.child("board").getValue(List::class.java)
                val currentTurn = snapshot.child("currentTurn").getValue(String::class.java) ?: "X"
                val status = snapshot.child("status").getValue(String::class.java) ?: "active"

                // Update board
                if (board != null && board.size == 9) {
                    for (i in board.indices) {
                        gameState[i] = board[i] as? String ?: ""
                        val button = gridLayout.getChildAt(i) as Button
                        button.text = gameState[i]
                    }
                }

                // Update turn
                isPlayerTurn = (currentTurn == playerSymbol)
                gameActive = status == "active"

                updateUI()
                checkGameStatus()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OnlineActivity, "Database error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupClickListeners() {
        for (i in 0 until gridLayout.childCount) {
            val button = gridLayout.getChildAt(i) as Button
            button.setOnClickListener {
                if (gameActive && isPlayerTurn && gameState[i].isEmpty()) {
                    makeMove(i)
                }
            }
        }

        resetButton.setOnClickListener {
            resetGame()
        }
    }

    private fun makeMove(index: Int) {
        gameState[index] = playerSymbol
        updateFirebaseBoard()
    }

    private fun updateFirebaseBoard() {
        val nextTurn = if (playerSymbol == "X") "O" else "X"

        dbRef.child("games").child(gameId).updateChildren(mapOf(
            "board" to gameState.toList(),
            "currentTurn" to nextTurn
        ))
    }

    private fun checkGameStatus() {
        val winner = checkWinner()

        if (winner != null) {
            gameActive = false
            statusText.text = "Player $winner wins!"
            resetButton.visibility = View.VISIBLE
            dbRef.child("games").child(gameId).child("status").setValue("finished")
        } else if (gameState.none { it.isEmpty() }) {
            gameActive = false
            statusText.text = "It's a draw!"
            resetButton.visibility = View.VISIBLE
            dbRef.child("games").child(gameId).child("status").setValue("finished")
        }
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
        for (i in 0 until gridLayout.childCount) {
            val button = gridLayout.getChildAt(i) as Button
            button.isEnabled = gameActive && isPlayerTurn && gameState[i].isEmpty()
        }

        playerTurnText.text = if (isPlayerTurn) "Your turn ($playerSymbol)" else "Opponent's turn"
    }

    private fun resetGame() {
        gameState.fill("")
        isPlayerTurn = (playerSymbol == "X")
        gameActive = true
        resetButton.visibility = View.INVISIBLE

        dbRef.child("games").child(gameId).updateChildren(mapOf(
            "board" to gameState.toList(),
            "currentTurn" to "X",
            "status" to "active"
        ))

        statusText.text = "Game reset!"
        updateUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up game when activity is destroyed
        dbRef.child("games").child(gameId).removeValue()
    }
}