package com.mraximentertainment.balliq.helpers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.mraximentertainment.balliq.BuildConfig

// Ad Unit ID sourced from BuildConfig for flexibility across environments (e.g., production, debug).
const val AD_UNIT_ID = BuildConfig.AD_UNIT_ID

/**
 * A singleton class for managing interstitial advertisements using Google Mobile Ads SDK.
 * Handles ad initialization, loading, and displaying with fallback mechanisms.
 */
object AdManager {

    private var interstitialAd: InterstitialAd? = null
    private var adIsLoading: Boolean = false

    /**
     * Initializes the Google Mobile Ads SDK. Should be called during application startup.
     *
     * @param context The application or activity context.
     */
    fun initializeAds(context: Context) {
        MobileAds.initialize(context) {}
        loadAd(context)
    }

    /**
     * Loads an interstitial advertisement if one is not already loaded.
     * Automatically sets up the ad to be reused or loaded again after display.
     *
     * @param context The application or activity context.
     */
    fun loadAd(context: Context) {
        if (interstitialAd == null && !adIsLoading) {
            adIsLoading = true
            val adRequest = AdRequest.Builder().build()

            InterstitialAd.load(
                context,
                AD_UNIT_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d("AdManager", "Failed to load ad: ${adError.message}")
                        interstitialAd = null
                        adIsLoading = false
                    }

                    override fun onAdLoaded(ad: InterstitialAd) {
                        Log.d("AdManager", "Ad loaded successfully.")
                        interstitialAd = ad
                        adIsLoading = false
                    }
                }
            )
        }
    }

    /**
     * Displays the interstitial ad if it is loaded. If no ad is ready, a fallback mechanism is triggered.
     *
     * @param context The activity context required for showing ads.
     * @param onDismiss Callback invoked when the ad is dismissed or unavailable.
     * @param data Optional data to be passed to the onDismiss callback.
     */
    fun showInterstitial(context: Context, onDismiss: (data: String) -> Unit, data: String) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("AdManager", "Ad dismissed by the user.")
                    interstitialAd = null
                    loadAd(context) // Preload the next ad.
                    onDismiss(data)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d("AdManager", "Failed to show ad: ${adError.message}")
                    interstitialAd = null
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d("AdManager", "Ad successfully displayed.")
                }
            }
            if (context is Activity && !context.isFinishing) {
                interstitialAd?.show(context)
            }
        } else {
            Log.d("AdManager", "Ad not ready. Executing fallback logic.")
            onDismiss(data)

            if (!adIsLoading) {
                adIsLoading = true
                loadAd(context)
            }
        }
    }
}
