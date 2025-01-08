package com.mraximentertainment.balliq.store

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import com.mraximentertainment.balliq.databinding.NoTrophiesDialogBinding


class NoTrophiesDialog (activity: Activity) : Dialog(activity) {

    private val realActivity = activity
    private lateinit var binding: NoTrophiesDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = NoTrophiesDialogBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnBuyMore.setOnClickListener {

            val intent = Intent(realActivity, TrophyActivity::class.java)
            realActivity.startActivity(intent)
            dismiss()
        }
        binding.closer.setOnClickListener {
            dismiss()
        }
    }
}