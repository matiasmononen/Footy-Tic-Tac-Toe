package com.mraximentertainment.balliq.navigation

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import com.mraximentertainment.balliq.databinding.TimeTrialAboutDialogBinding

class TimeTrialAboutDialog(activity: Activity) : Dialog(activity){

    private lateinit var binding: TimeTrialAboutDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = TimeTrialAboutDialogBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.closer.setOnClickListener {
            dismiss()
        }
    }
}