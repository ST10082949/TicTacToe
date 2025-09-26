package com.example.xando

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class SoloModeSelectionActivity : AppCompatActivity() {

    private lateinit var difficultyGroup: RadioGroup
    private lateinit var startGameBtn: Button
    private lateinit var backBtn: Button

    private var selectedDifficulty = "MEDIUM"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solo_mode_selection)

        difficultyGroup = findViewById(R.id.difficultyGroup)
        startGameBtn = findViewById(R.id.startGameBtn)
        backBtn = findViewById(R.id.backBtn)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        difficultyGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedDifficulty = when (checkedId) {
                R.id.easyRadio -> "EASY"
                R.id.mediumRadio -> "MEDIUM"
                R.id.hardRadio -> "HARD"
                else -> "MEDIUM"
            }
        }

        startGameBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("GAME_MODE", "SOLO")
                putExtra("DIFFICULTY", selectedDifficulty)
            }
            startActivity(intent)
        }

        backBtn.setOnClickListener {
            finish()
        }
    }
}