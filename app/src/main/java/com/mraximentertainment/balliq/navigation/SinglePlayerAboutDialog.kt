package com.mraximentertainment.balliq.navigation

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import com.mraximentertainment.balliq.databinding.AbootDialogBinding

class SinglePlayerAboutDialog(activity: Activity) : Dialog(activity){

    private lateinit var binding: AbootDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = AbootDialogBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.closer.setOnClickListener {
            dismiss()
        }
    }
}