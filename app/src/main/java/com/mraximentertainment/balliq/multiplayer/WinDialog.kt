package com.mraximentertainment.balliq.multiplayer

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import com.mraximentertainment.balliq.databinding.WinDialogBinding
import com.mraximentertainment.balliq.helpers.AdManager
import com.mraximentertainment.balliq.singleplayer.OnePlayerActivity

/**
 * A custom dialog that displays a message when a player wins the game.
 * It includes functionality to track the number of wins and start a new game.
 *
 * @param activity The current activity context to interact with.
 * @param message The win message to display (e.g., "You Won").
 */
class WinDialog(activity: Activity, private val message: String) : Dialog(activity) {

    private lateinit var binding: WinDialogBinding
    private val mainActivity = activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WinDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences: SharedPreferences = mainActivity.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Retrieve win count and check if the player is premium
        val isPremium: Boolean = sharedPreferences.getBoolean("isPremium", false)
        var wins: Int = sharedPreferences.getInt("wins", 0)

        // Increment win count if the player won
        if (message == "You Won") {
            wins += 1
            editor.putInt("wins", wins)
            editor.apply()
        }

        // Update the UI with the number of wins
        binding.tvWins.text = "Wins: $wins"
        binding.tvDialog.text = message

        // Set up the button actions
        setupButtonActions(isPremium)
    }

    /**
     * Set up the action listeners for the menu and new game buttons.
     */
    private fun setupButtonActions(isPremium: Boolean) {
        // Menu button dismisses the dialog and finishes the activity
        binding.btnMenu.setOnClickListener {
            dismiss()
            mainActivity.finish()
        }

        // New Game button either shows an ad or starts a new game based on premium status
        binding.btnNewGame.setOnClickListener {
            if (!isPremium) {
                AdManager.showInterstitial(mainActivity, ::startNewGame, "")
            } else {
                startNewGame("")
            }
        }
    }

    /**
     * Starts a new game by launching the FootytttActivity.
     * Closes the current activity and dismisses the dialog.
     */
    private fun startNewGame(data: String) {
        dismiss()
        mainActivity.finish()
        mainActivity.startActivity(Intent(mainActivity, FootytttActivity::class.java))
    }

    /**
     * Ensures the dialog is dismissed when the activity stops.
     */
    override fun onStop() {
        super.onStop()
        dismiss()
    }
}
