package com.mraximentertainment.balliq.navigation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.mraximentertainment.balliq.helpers.AdManager
import com.mraximentertainment.balliq.helpers.ConsentManager
import com.mraximentertainment.balliq.database.DatabaseHelper
import com.mraximentertainment.balliq.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Ads and Consent
        ConsentManager.initialize(this) {
            AdManager.initializeAds(this)
        }

        // Initialize Database
        DatabaseHelper.copyDatabaseIfNeeded(this, "tictac.db") {
            binding.loading.visibility = View.GONE
            setupUI()
        }
    }

    private fun setupUI() {

        binding.btnMultiPlayer.setOnClickListener {
            startActivity(Intent(this, MultiplayerActivity::class.java))
        }

        binding.btntimetrial.setOnClickListener {
            startActivity(Intent(this, TimeTrialActivity::class.java))
        }

        binding.btnStore.setOnClickListener {
            startActivity(Intent(this, StoreActivity::class.java))
        }
    }
}
