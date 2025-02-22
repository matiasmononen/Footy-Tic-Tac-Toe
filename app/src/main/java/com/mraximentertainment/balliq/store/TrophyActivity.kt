package com.mraximentertainment.balliq.store

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.mraximentertainment.balliq.databinding.ActivityTrophyBinding

class TrophyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrophyBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var trophies = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrophyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize shared preferences and load the current trophy count
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        trophies = sharedPreferences.getInt("trophies", 20)
        binding.tvTrophies.text = trophies.toString()

        // Map of trophy value to corresponding button
        val btnMap = createTrophyButtonMap()

        // Initialize Google Billing and set up product queries
        val productMap = createProductMap(btnMap)
        GoogleBilling.initializeBilling(this, productMap)

        // Set up button click listeners for purchasing trophies
        setupButtonClickListeners(btnMap)
    }

    /**
     * Creates a map of trophy values to buttons.
     *
     * @return A map with the name of the trophy bundle mapped to the corresponding button
     */
    private fun createTrophyButtonMap(): Map<String, Button> {
        return mapOf(
            "50" to binding.btntrophy50,
            "100" to binding.btntrophy100,
            "250" to binding.btntrophy250,
            "500" to binding.btntrophy500,
            "1000" to binding.btntrophy1000
        )
    }

    /**
     * Creates a map for Google Billing, linking trophy values to their query handlers.
     *
     * @param btnMap A map with the name of the trophy bundle mapped to the corresponding button
     * @return A map with the name of the product as a key corresponding to a
     * pair of functions. First one being the function called after the products have
     * been queried and the second one being the function called after a successful
     * purchase of the product.
     */
    private fun createProductMap(btnMap: Map<String, Button>): Map<String, Pair<(ProductDetails) -> Unit, (Purchase) -> Unit>> {
        return btnMap.map { (key, btn) ->
            key to Pair(getOnQuery(btn), ::onComplete)
        }.toMap()
    }

    /**
     * Sets up the click listeners for each trophy purchase button.
     *
     * @param btnMap A map with the name of the trophy bundle mapped to the corresponding button
     */
    private fun setupButtonClickListeners(btnMap: Map<String, Button>) {
        btnMap.forEach { (trophyValue, btn) ->
            btn.setOnClickListener {
                GoogleBilling.launchPurchaseFlow(this, trophyValue)
            }
        }
    }

    /**
     * Creates the onQuery function for displaying the product details (price) on the button.
     *
     * @param btn Button corresponding to the purchase
     * @return A callback function invoked on a successful Google Billing query.
     * Updates the UI to match the retrieved product details
     */
    private fun getOnQuery(btn: Button): (ProductDetails) -> Unit {
        return { productDetails: ProductDetails ->
            val currency = productDetails.oneTimePurchaseOfferDetails?.priceCurrencyCode ?: ""
            val priceMicros = productDetails.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0
            val priceReadable = "%.2f".format(priceMicros / 1_000_000.0)
            btn.text = "$priceReadable $currency"
        }
    }

    /**
     * Handles the completion of a trophy purchase.
     *
     * @param purchase completed purchase
     */
    private fun onComplete(purchase: Purchase) {
        runOnUiThread {
            // Update the trophy count
            val purchasedAmount = purchase.products.first().toIntOrNull() ?: 0
            trophies += purchasedAmount
            editor.putInt("trophies", trophies).apply()

            // Update the UI
            binding.tvTrophies.text = trophies.toString()

            // Consume the purchase to allow for future purchases
            GoogleBilling.consumePurchase(purchase)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        GoogleBilling.release()
    }
}
