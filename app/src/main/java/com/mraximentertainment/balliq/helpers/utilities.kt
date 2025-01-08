package com.mraximentertainment.balliq.helpers

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import java.text.Normalizer

/**
 * Formats a guess string to ensure it does not exceed a certain length.
 * If the guess exceeds the max length, it trims the string to the nearest word.
 *
 * @param guess The guess string to format.
 * @return The formatted guess string.
 */
fun formatGuess(guess: String): String {
    val maxLength = 11
    val words = guess.split(" ") // Split the string into individual words
    var result = ""

    // Iterate backward through the words and build the result
    for (i in words.indices.reversed()) {
        val newWord = words[i]
        result = if ((newWord.length + result.length + 1) <= maxLength) {
            if (result.isEmpty()) newWord else "$newWord $result"
        } else {
            if (result.isEmpty()) newWord else result
        }
    }

    return result
}


/**
 * Removes diacritical marks (accents, tilde, etc.) from a string to make it more uniform.
 *
 * @param string The input string to normalize.
 * @return A new string without diacritical marks.
 */
fun removeDiacriticalMarks(string: String): String {
    return Normalizer.normalize(string, Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "") // Remove all non-spacing marks (diacritics)
}

/**
 * Extension function to check if an activity is currently running and not destroyed or finishing.
 *
 * @return True if the context is an activity and it is running, false otherwise.
 */
fun Context?.isActivityRunning(): Boolean {
    return this is Activity && !this.isDestroyed && !this.isFinishing
}

/**
 * Sets an image resource for the given ImageView based on a resource key.
 *
 * @param iv The ImageView to update.
 * @param key The key corresponding to the drawable resource.
 * @param context The context used to access resources.
 */
fun setImage(iv: ImageView, key: String, context: Context) {
    val resourceId = context.resources.getIdentifier(key, "drawable", context.packageName)
    iv.setImageResource(resourceId)
}
