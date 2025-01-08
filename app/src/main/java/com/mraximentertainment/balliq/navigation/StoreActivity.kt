package com.mraximentertainment.balliq.navigation

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mraximentertainment.balliq.databinding.ActivityStoreBinding
import com.mraximentertainment.balliq.store.PremiumDialog
import com.mraximentertainment.balliq.store.ShirtActivity
import com.mraximentertainment.balliq.store.TrophyActivity

class StoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityStoreBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val sharedPreferences: SharedPreferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isPremium: Boolean = sharedPreferences.getBoolean("isPremium", false)
        val trophies: Int = sharedPreferences.getInt("trophies", 20)

        binding.tvTrophies.text = trophies.toString()

        if (isPremium){
            binding.premium.isEnabled = false
        }

        binding.premium.setOnClickListener {
            val preDialog = PremiumDialog(this@StoreActivity)
            preDialog.setCancelable(false)
            preDialog.show()
        }

        binding.btnkits.setOnClickListener {
            Intent(this, ShirtActivity::class.java).also{
                startActivity(it)
            }
        }
        binding.btnTrophies.setOnClickListener {
            Intent(this, TrophyActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val trophies = sharedPreferences.getInt("trophies", 20)
        binding.tvTrophies.text = trophies.toString()
    }
}