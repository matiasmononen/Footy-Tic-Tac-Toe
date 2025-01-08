package com.mraximentertainment.balliq.navigation

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mraximentertainment.balliq.helpers.AdManager
import com.mraximentertainment.balliq.databinding.ActivityMultiplayerBinding
import com.mraximentertainment.balliq.multiplayer.FootytttActivity
import kotlin.random.Random

/**
 * Activity that manages the multiplayer game flow, including handling game creation,
 * joining a game, and displaying the player's win count.
 */
class MultiplayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMultiplayerBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var isPremium: Boolean = false
    private var wins: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiplayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SharedPreferences and UI
        initSharedPreferences()
        handleIntentExtras()
        setupUI()

        // Initialize ads
        AdManager.initializeAds(this)
    }

    /**
     * Initializes the SharedPreferences to load player status and win count.
     */
    private fun initSharedPreferences() {
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        isPremium = sharedPreferences.getBoolean("isPremium", false)
        wins = sharedPreferences.getInt("wins", 0)
        updateWinsDisplay()
    }

    /**
     * Handles any extras passed with the intent, such as displaying a toast if the game failed.
     */
    private fun handleIntentExtras() {
        val failed = intent.extras?.getBoolean("failed") ?: false
        if (failed) {
            Toast.makeText(this, "Game wasn't found!", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Sets up the UI components such as button clicks and the info dialog.
     */
    private fun setupUI() {
        // Display the current number of wins
        binding.tvWins.text = "Wins: $wins"

        // Info button shows the About dialog
        binding.ivInfo.setOnClickListener {
            AboutDialog(this).apply {
                setCancelable(false)
                show()
            }
        }

        // Play random game button
        binding.btnPlayRandom.setOnClickListener {
            handlePlayClick("play")
        }

        // Create private game button
        binding.btnCreatePrivate.setOnClickListener {
            handlePlayClick("create")
        }

        // Join private game button
        binding.btnJoinPrivate.setOnClickListener {
            handleJoinGame()
        }
    }

    /**
     * Handles the button click for starting a game based on the specified type.
     * Displays ads for non-premium users.
     */
    private fun handlePlayClick(type: String) {
        if (isPremium) {
            beginGame(type)
        } else {
            AdManager.showInterstitial(this, ::beginGame, type)
        }
    }

    /**
     * Handles the joining of a private game. Verifies the game code before proceeding.
     */
    private fun handleJoinGame() {
        val code = binding.etCode.text.toString()
        if (code.length == 6) {
            if (isPremium) {
                beginGame("join")
            } else {
                AdManager.showInterstitial(this, ::beginGame, "join")
            }
        } else {
            Toast.makeText(this, "Invalid game code! Please try again.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Begins a new game by creating an intent to start the FootytttActivity.
     * A game code is passed depending on the game type.
     */
    private fun beginGame(type: String) {
        val intent = when (type) {
            "play" -> Intent(this, FootytttActivity::class.java).apply { putExtra("code", "d") }
            else -> Intent(this, FootytttActivity::class.java).apply {
                val code = if (type == "create") {
                    "${System.currentTimeMillis()}_${Random.nextInt()}"
                } else {
                    binding.etCode.text.toString()
                }
                putExtra("code", code)
            }
        }
        startActivity(intent)
    }

    /**
     * Updates the UI with the current win count from SharedPreferences.
     */
    private fun updateWinsDisplay() {
        binding.tvWins.text = "Wins: $wins"
    }

    /**
     * Refreshes the win count and loads ads when the activity is resumed.
     */
    override fun onResume() {
        super.onResume()
        wins = sharedPreferences.getInt("wins", 0)
        updateWinsDisplay()
        AdManager.loadAd(this)
    }
}
