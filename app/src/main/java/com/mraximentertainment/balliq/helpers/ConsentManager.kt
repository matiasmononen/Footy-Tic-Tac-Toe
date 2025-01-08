package com.mraximentertainment.balliq.helpers

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * A utility object for managing user consent using Google’s User Messaging Platform (UMP).
 * Handles consent initialization and form display as required.
 */
object ConsentManager {

    /**
     * Initializes the User Messaging Platform (UMP) and handles the consent process.
     * Requests user consent information and, if necessary, displays the consent form.
     *
     * @param context The activity context required for showing the consent form.
     * @param onConsentComplete Callback invoked once the consent process is complete.
     */
    fun initialize(context: Context, onConsentComplete: () -> Unit) {
        // Retrieve consent information instance
        val consentInformation = UserMessagingPlatform.getConsentInformation(context)

        // Set up request parameters for consent information
        val params = ConsentRequestParameters.Builder().build()

        // Request an update to the user's consent information
        consentInformation.requestConsentInfoUpdate(
            context as Activity, // Must be an Activity context
            params,
            {
                // Load and display the consent form if required
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    context,
                    ConsentForm.OnConsentFormDismissedListener {
                        // Trigger callback once the consent form is dismissed
                        onConsentComplete()
                    }
                )
            },
            {}
        )
    }
}
