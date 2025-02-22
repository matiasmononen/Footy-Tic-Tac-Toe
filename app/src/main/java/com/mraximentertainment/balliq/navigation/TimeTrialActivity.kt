package com.mraximentertainment.balliq.navigation

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.MobileAds
import com.mraximentertainment.balliq.helpers.AdManager
import com.mraximentertainment.balliq.R
import com.mraximentertainment.balliq.databinding.ActivityTimeTrialBinding
import com.mraximentertainment.balliq.singleplayer.OnePlayerActivity
import com.mraximentertainment.balliq.singleplayer.RecordActivity

/**
 * Activity that handles the time trial game mode. This includes functionality
 * for selecting a map, playing the game, viewing records, and showing ads.
 */
class TimeTrialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimeTrialBinding
    private var map: String = "world"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimeTrialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SharedPreferences to check if the user has a premium account
        val sharedPreferences: SharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isPremium: Boolean = sharedPreferences.getBoolean("isPremium", false)

        // Button click listener to start the game
        binding.btnPlay.setOnClickListener {
            handlePlayButtonClick(isPremium)
        }

        // Initialize Google Mobile Ads
        MobileAds.initialize(this) {}

        // Button click listener to view records
        binding.btnRecords.setOnClickListener {
            navigateToRecordsActivity()
        }

        // Info button click listener to display Time Trial About dialog
        binding.ivInfo.setOnClickListener {
            showTimeTrialInfoDialog()
        }

        // Set up Spinner with map selection options
        setupMapSelectionSpinner()

        // Load ads
        AdManager.loadAd(this)
    }

    /**
     * Handles the action when the Play button is clicked.
     * If the user is not premium, it will show an interstitial ad before starting the game.
     * If the user is premium, it starts the game directly.
     *
     * @param isPremium True if user is premium, false otherwise
     */
    private fun handlePlayButtonClick(isPremium: Boolean) {
        if (isPremium) {
            startGame("")
        } else {
            AdManager.showInterstitial(this, ::startGame, "")
        }
    }

    /**
     * Starts the OnePlayerActivity with the selected map.
     */
    private fun startGame(data: String) {
        val intent = Intent(this, OnePlayerActivity::class.java).apply {
            putExtra("map", map)
        }
        startActivity(intent)
    }

    /**
     * Navigates to the RecordActivity to view game records.
     */
    private fun navigateToRecordsActivity() {
        val intent = Intent(this, RecordActivity::class.java)
        startActivity(intent)
    }

    /**
     * Shows the TimeTrialAboutDialog that provides information about the time trial mode.
     */
    private fun showTimeTrialInfoDialog() {
        val winDialog = TimeTrialAboutDialog(this)
        winDialog.setCancelable(false)
        winDialog.show()
    }

    /**
     * Sets up the map selection Spinner with the list of available maps.
     */
    private fun setupMapSelectionSpinner() {
        val spinner: Spinner = binding.spinner

        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.spinner_items,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Apply the adapter to the spinner
        spinner.adapter = adapter

        // Set up an item selection listener for the spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                map = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                map = "world"  // Default map if nothing is selected
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AdManager.loadAd(this)
    }
}
