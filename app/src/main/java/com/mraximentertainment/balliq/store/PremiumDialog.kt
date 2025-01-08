package com.mraximentertainment.balliq.store

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.mraximentertainment.balliq.databinding.PremiumDialogBinding

/**
 * PremiumDialog is a custom dialog for managing premium purchase flows.
 * It integrates with GoogleBilling to handle product purchases and user rewards.
 *
 * @param activity The parent activity that creates the dialog.
 */
class PremiumDialog(private val activity: Activity) : Dialog(activity) {

    private lateinit var binding: PremiumDialogBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var trophies: Int = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout and set it as the dialog's content
        binding = PremiumDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SharedPreferences for storing user data
        sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        // Load initial trophy count
        trophies = sharedPreferences.getInt("trophies", 20)

        // Set up Google Billing and product details
        setupBilling()

        // Set button listeners
        setupUIListeners()
    }

    /**
     * Sets up the GoogleBilling client and initializes the premium product details.
     */
    private fun setupBilling() {
        GoogleBilling.initializeBilling(activity, mapOf("premium" to Pair(::onQuery, ::handlePurchaseComplete)))
    }

    /**
     * Configures UI interactions such as purchase flow and dialog dismissal.
     */
    private fun setupUIListeners() {
        // Handle purchase button click
        binding.btnBuy.setOnClickListener {
            GoogleBilling.launchPurchaseFlow(activity, "premium")
        }

        // Handle close button click
        binding.closer.setOnClickListener {
            dismiss()
        }
    }

    /**
     * Display the product details from Google billing.
     */
    private fun onQuery(productDetails: ProductDetails) {
        activity.runOnUiThread {
            binding.premium.text = productDetails.name
            val currency = productDetails.oneTimePurchaseOfferDetails?.priceCurrencyCode ?: ""
            val priceMicros = productDetails.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0
            val priceReadable = "%.2f".format(priceMicros / 1_000_000.0)
            binding.btnBuy.text = "$priceReadable $currency"
        }
    }

    /**
     * Handles the completion of a successful purchase.
     */
    private fun handlePurchaseComplete(purchase: Purchase) {
        // Update SharedPreferences with premium status and rewards
        editor.putBoolean("isPremium", true)
        trophies += 200
        editor.putInt("trophies", trophies)
        editor.apply()

        // Optional: Provide feedback to the user (e.g., show a toast or dialog)
    }

    /**
     * Releases resources and shuts down the billing client when the dialog is dismissed.
     */
    override fun dismiss() {
        super.dismiss()
        GoogleBilling.release()
    }
}
