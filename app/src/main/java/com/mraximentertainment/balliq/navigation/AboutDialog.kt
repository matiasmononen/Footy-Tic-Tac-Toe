package com.mraximentertainment.balliq.navigation

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import com.mraximentertainment.balliq.databinding.AboutDialogBinding

class AboutDialog(activity: Activity) : Dialog(activity){

    private lateinit var binding: AboutDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = AboutDialogBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.closer.setOnClickListener {
            dismiss()
        }
    }
}