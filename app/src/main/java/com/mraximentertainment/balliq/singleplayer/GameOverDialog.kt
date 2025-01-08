package com.mraximentertainment.balliq.singleplayer

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import com.mraximentertainment.balliq.helpers.AdManager
import com.mraximentertainment.balliq.databinding.GameOverDialogBinding

/**
 * A custom dialog class to display the "Game Over" screen with options to view results,
 * check high scores, and start a new game.
 *
 * @param activity The activity that creates this dialog.
 * @param score The player's score in the current game session.
 * @param time The time taken by the player to achieve the score.
 * @param map The current game map or mode (e.g., "World" or custom map).
 */
class GameOverDialog(
    private val activity: Activity,
    private val score: Int,
    private val time: Float,
    private var map: String
) : Dialog(activity) {

    private lateinit var binding: GameOverDialogBinding

    // SharedPreferences for storing and retrieving high scores and game settings
    private val sharedPreferences: SharedPreferences by lazy {
        activity.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = GameOverDialogBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize the Ads SDK
        AdManager.initializeAds(activity)

        // Setup the dialog UI and functionality
        setupDialog()
    }

    /**
     * Configures the dialog UI and initializes data for display.
     */
    private fun setupDialog() {
        val isPremium = sharedPreferences.getBoolean("isPremium", false) // Check if the user has a premium account
        val (recordScore, recordTime) = getRecordData() // Fetch existing high score and time
        val isNewRecord = updateRecordIfNeeded(recordScore, recordTime) // Update records if needed
        val (updatedRecordScore, updatedRecordTime) = getRecordData() // Fetch updated high score and time

        displayResults(updatedRecordScore, updatedRecordTime, isNewRecord) // Display results on the dialog
        setupButtonListeners(isPremium) // Set up listeners for button interactions
    }

    /**
     * Retrieves the current high score and record time for the selected map.
     *
     * @return A pair containing the high score and record time.
     */
    private fun getRecordData(): Pair<Int, Float> {
        val recordScore = sharedPreferences.getInt(map, 0)
        val recordTime = sharedPreferences.getFloat("$map time", Float.MAX_VALUE)

        return recordScore to recordTime
    }

    /**
     * Updates the high score and record time if the player's current performance exceeds them.
     *
     * @param recordScore The existing high score.
     * @param recordTime The existing record time.
     * @return True if a new record was set, false otherwise.
     */
    private fun updateRecordIfNeeded(recordScore: Int, recordTime: Float): Boolean {
        val editor = sharedPreferences.edit()
        var isNewRecord = false

        // Check if the current score or time is better than the recorded values
        if (score > recordScore || (score == recordScore && time < recordTime)) {
            editor.putInt(map, score)
            editor.putFloat("$map time", time)
            editor.apply()
            isNewRecord = true
        }

        return isNewRecord
    }

    /**
     * Displays the current score, high score, and record time in the dialog.
     *
     * @param recordScore The high score for the map.
     * @param recordTime The record time for the map.
     * @param isNewRecord Indicates whether a new record was set in this session.
     */
    private fun displayResults(recordScore: Int, recordTime: Float, isNewRecord: Boolean) {
        binding.tvDialog.text = if (isNewRecord) "New Record!" else "Game Over"
        binding.tvresult.text = "$score/9 - $time s"
        binding.tvrecord.text = "$recordScore/9 - $recordTime s"
    }

    /**
     * Sets up the listeners for "Menu" and "New Game" buttons.
     *
     * @param isPremium Indicates whether the user has a premium account.
     */
    private fun setupButtonListeners(isPremium: Boolean) {
        binding.btnMenu.setOnClickListener {
            dismiss() // Close the dialog
            activity.finish() // End the current activity
        }

        binding.btnNewGame.setOnClickListener {
            if (isPremium) {
                startNewGame() // Start a new game directly for premium users
            } else {
                AdManager.showInterstitial(
                    activity,
                    ::startNewGame,
                    ""
                ) // Show an ad before starting a new game
            }
        }
    }

    /**
     * Starts a new game by launching the appropriate activity.
     *
     * @param data Optional data passed from the ad manager callback.
     */
    private fun startNewGame(data: String = "") {
        dismiss() // Close the dialog
        activity.finish() // End the current activity
        val intent = Intent(activity, OnePlayerActivity::class.java).apply {
            putExtra("map", if (map == "World") "World" else map)
        }
        activity.startActivity(intent) // Launch the new game activity
    }

    /**
     * Ensures the dialog is properly dismissed when the activity is stopped.
     */
    override fun onStop() {
        super.onStop()
        dismiss()
    }
}
