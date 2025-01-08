package com.mraximentertainment.balliq.store

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.android.billingclient.api.*
import com.mraximentertainment.balliq.navigation.MultiplayerActivity
import com.mraximentertainment.balliq.navigation.TimeTrialActivity
import com.mraximentertainment.balliq.databinding.ActivityShirtBinding

class ShirtActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShirtBinding
    private lateinit var editor: SharedPreferences.Editor
    private var boughtShirt = ""
    private var trophies = 0
    private var equippedShirt = "basic1"
    private lateinit var shirtMap: Map<String, Shirt>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShirtBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences: SharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        equippedShirt = sharedPreferences.getString("equipped", "basic1").toString()
        // Map shirts to corresponding UI elements.
        val shirtViewMap = mapOf(
            "basic1" to Triple(binding.btnbasic1, binding.tvbasic1, null),
            "basic2" to Triple(binding.btnbasic2, binding.tvbasic2, null),
            "shirt1" to Triple(binding.btnshirt1, binding.tvshirt1, binding.ivshirt1),
            "shirt2" to Triple(binding.btnshirt2, binding.tvshirt2, binding.ivshirt2),
            "shirt3" to Triple(binding.btnshirt3, binding.tvshirt3, binding.ivshirt3),
            "shirt5" to Triple(binding.btnshirt5, binding.tvshirt5, binding.ivshirt5),
            "shirt6" to Triple(binding.btnshirt6, binding.tvshirt6, binding.ivshirt6),
            "shirt8" to Triple(binding.btnshirt8, binding.tvshirt8, binding.ivshirt8),
            "shirt9" to Triple(binding.btnshirt9, binding.tvshirt9, binding.ivshirt9),
            "shirt10" to Triple(binding.btnshirt10, binding.tvshirt10, binding.ivshirt10),
            "shirt11" to Triple(binding.btnshirt11, binding.tvshirt11, binding.ivshirt11),
            "shirt12" to Triple(binding.btnshirt12, binding.tvshirt12, binding.ivshirt12),
            "shirt13" to Triple(binding.btnshirt13, binding.tvshirt13, binding.ivshirt13),
            "shirt14" to Triple(binding.btnshirt14, binding.tvshirt14, binding.ivshirt14),
            "shirt15" to Triple(binding.btnshirt15, binding.tvshirt15, binding.ivshirt15),
            "isPremium" to Triple(binding.btnpremshirt1, binding.tvpremshirt1, null),
            "premshirt2" to Triple(binding.btnpremshirt2, binding.tvpremshirt2, null),
            "premshirt3" to Triple(binding.btnpremshirt3, binding.tvpremshirt3, null),
            "scoreshirt" to Triple(binding.btnscoreshirt1, binding.tvscoreshirt1, null),
            "winshirt" to Triple(binding.btnwinshirt1, binding.tvwinshirt1, null)
        )

        trophies = sharedPreferences.getInt("trophies", 20)
        binding.tvTrophies.text = trophies.toString()

        // Create a shirt object for each key
        shirtMap = shirtViewMap.map { (key, triple) ->
            var default = key.contains("basic")
            if (key == "winsshirt") default = sharedPreferences.getInt("wins", 0) > 99
            key to Shirt(
                key,
                sharedPreferences.getBoolean(key, default),
                triple.second,
                triple.first,
                triple.third
            )
        }.toMap()


        val prem2 = shirtMap.getOrDefault("premshirt2", Shirt("", true, binding.tvTrophies, binding.btnshirt15, null))
        val prem3 = shirtMap.getOrDefault("premshirt3", Shirt("", true, binding.tvTrophies, binding.btnshirt15, null))

        val productMap = mapOf("premshirt2" to Pair(getOnQuery(prem2), ::onComplete),
            "premshirt3" to Pair(getOnQuery(prem3), ::onComplete))

        GoogleBilling.initializeBilling(this, productMap)

        shirtMap.forEach { (s, shirt) ->
            setShirt(shirt.btn, shirt.tv, shirt.iv, shirt.owned, shirt.name, equippedShirt)
            shirtOnClick(shirt)
        }
    }

    // Function that sets up the UI for a single shirt
    private fun setShirt (button: Button, textView: TextView, iv: ImageView?, owned: Boolean, name: String, equippedShirt: String) {
        if (owned && name != equippedShirt) {
            button.text = "EQUIP"
            textView.text = "OWNED"
            if (iv != null) {
                iv.visibility = View.GONE
            }
        }
        else if (name == equippedShirt) {
            button.text = "EQUIPPED"
            textView.text = "OWNED"
            if (iv != null) {
                iv.visibility = View.GONE
            }
            button.isEnabled = false
        }
    }

    // Unequips a shirt in the UI
    private fun unequip (name: String){
        val shirt = shirtMap[name]
        if (shirt != null) {
            shirt.btn.isEnabled = true
            shirt.btn.text = "Equip"
        }
    }

    // Handles when the equip/buy button is clicked for a shirt
    private fun shirtOnClick(shirt: Shirt) {
        shirt.btn.setOnClickListener {
            when (shirt.name) {
                "premshirt1" -> handlePremiumShirt(shirt,  false)
                "premshirt2" -> handlePremiumShirt(shirt,  true)
                "premshirt3" -> handlePremiumShirt(shirt, true)
                "scoreshirt" -> handleSpecialShirt(shirt, TimeTrialActivity::class.java)
                "winshirt" -> handleSpecialShirt(shirt, MultiplayerActivity::class.java)
                else -> handleDefaultShirt(shirt)
            }
        }
    }

    private fun handlePremiumShirt(
        shirt: Shirt,
        isBuyable: Boolean
    ) {
        if (shirt.owned) {
            equipShirt(shirt)
        } else {
            if (isBuyable) {
                boughtShirt = shirt.name
                GoogleBilling.launchPurchaseFlow(this, shirt.name)
            } else {
                val preDialog = PremiumDialog(this@ShirtActivity)
                preDialog.setCancelable(false)
                preDialog.show()
            }
        }
    }

    // Equips the shirt in the UI
    private fun equipShirt(shirt: Shirt) {
        unequip(equippedShirt.toString())
        equippedShirt = shirt.name
        shirt.btn.isEnabled = false
        shirt.btn.text = "EQUIPPED"
        editor.putString("equipped", shirt.name)
        editor.apply()
    }

    private fun handleSpecialShirt(
        shirt: Shirt,
        activityClass: Class<*>,
    ) {
        if (getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getInt("wins", 0) > 99) {
            getShirt(shirt)
        } else {
            val intent = Intent(this@ShirtActivity, activityClass)
            startActivity(intent)
        }
    }

    private fun handleDefaultShirt(shirt: Shirt) {
        if (shirt.owned) {
            equipShirt(shirt)
        }
        else {
            val cost = shirt.tv.text.toString().toInt()
            if (trophies >= cost) {
                trophies -= cost
                editor.putInt("trophies", trophies)
                editor.apply()
                getShirt(shirt)
                binding.tvTrophies.text = trophies.toString()
            }
            else{
                val dialog = NoTrophiesDialog(this@ShirtActivity)
                dialog.setCancelable(false)
                dialog.show()
            }
        }
    }


    // Function for Google Billing
    private fun onComplete(purchase: Purchase) {
        shirtMap[purchase.products.first()]?.let { getShirt(it) }
    }

    private fun getOnQuery(shirt: Shirt): (ProductDetails) -> Unit {
        return { productDetails: ProductDetails ->
            val currency = productDetails.oneTimePurchaseOfferDetails?.priceCurrencyCode ?: ""
            val price1 = productDetails.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0
            val price1Readable = "%.2f".format(price1 / 1_000_000.0)
            shirt.tv.text = "$price1Readable $currency"
        }
    }
    // Class to express a single shirt.
    private class Shirt(val name: String, var owned: Boolean, val tv: TextView, val btn: Button, val iv: ImageView?)

    // Gives the user ownership of the shirt
    private fun getShirt(shirt: Shirt) {
        editor.putBoolean("premshirt2", true)
        editor.apply()
        shirt.owned = true
        if (shirt.iv != null) {
            shirt.iv.visibility = View.GONE
        }
        shirt.tv.width  = 300
        shirt.tv.text = "Owned"
        shirt.btn.text = "EQUIP"
    }
    override fun onResume() {
        super.onResume()
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        trophies = sharedPreferences.getInt("trophies", 20)
        binding.tvTrophies.text = trophies.toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        GoogleBilling.release()
    }
}