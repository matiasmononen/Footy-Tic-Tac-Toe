package com.mraximentertainment.balliq.helpers

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import com.mraximentertainment.balliq.database.getTeamDescription
import com.mraximentertainment.balliq.databinding.GuessDialogBinding

/**
 * An interface to relay guess data from the dialog to the activity.
 */
interface MyDialogCallback {
    fun onDataReceived(data: String)
}
/**
 * A custom dialog for guessing teams based on input and suggestions.
 *
 * @param callback The callback to send the guessed data back to the parent.
 * @param activity The activity context to operate within.
 * @param team1 The first team's identifier.
 * @param team2 The second team's identifier.
 * @param suggestions A list of team suggestions for the autocomplete.
 * @param suggestionsWithNormalized A list of pairs mapping suggestions to their normalized forms for filtering.
 */
class GuessDialog(
    private val callback: MyDialogCallback,
    private val activity: Activity,
    private val team1: String,
    private val team2: String,
    private val suggestions: MutableList<String>,
    private val suggestionsWithNormalized: List<Pair<String, String>>
) : Dialog(activity) {

    private lateinit var binding: GuessDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = GuessDialogBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupAutocomplete()
        setupButtons()
        setupTeamDisplay()
    }

    /**
     * Configures the autocomplete functionality for the search input.
     */
    private fun setupAutocomplete() {
        val adapter = object :
            ArrayAdapter<String>(activity, android.R.layout.simple_dropdown_item_1line, suggestions) {

            // Filter based on the normalized form of the input
            override fun getFilter(): Filter {
                return object : Filter() {
                    override fun performFiltering(constraint: CharSequence?): FilterResults {
                        val normalizedQuery = constraint?.let {
                            removeDiacriticalMarks(it.toString()).lowercase()
                        } ?: ""

                        // Retrieve the suggestions that match the normalized form of the constraint.
                        val filteredSuggestions = suggestionsWithNormalized
                            .filter { it.second.contains(normalizedQuery) }
                            .map { it.first }

                        return FilterResults().apply {
                            values = filteredSuggestions
                            count = filteredSuggestions.size
                        }
                    }

                    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                        clear()
                        if (results?.values is List<*>) {
                            addAll(results.values as List<String>)
                        }
                        if ((results?.count ?: 0) > 0) {
                            notifyDataSetChanged()
                        } else {
                            notifyDataSetInvalidated()
                        }
                    }
                }
            }
        }

        binding.search.apply {
            setAdapter(adapter)
            setOnItemClickListener { _, _, _, _ -> hideKeyboard() }
        }
    }

    /**
     * Configures the behavior of the dialog's buttons.
     */
    private fun setupButtons() {
        binding.btnMakeGuess.setOnClickListener {
            val answer = binding.search.text.toString()
            callback.onDataReceived(answer)
            dismiss()
        }

        binding.closer.setOnClickListener { dismiss() }
    }

    /**
     * Configures the display for the selected teams, including descriptions and images.
     */
    private fun setupTeamDisplay() {
        val team1Description = getTeamDescription(team1)
        val team2Description = getTeamDescription(team2.split(" ")[0])

        binding.tvHorizontal.text = team1Description
        binding.tvVertical.text = team2Description

        setTeamImage(binding.ivHorizontalGuess, team1)
        setTeamImage(binding.ivVerticalGuess, team2.split(" ")[0])

        adjustImageWidthIfNecessary(team2.split(" ")[0])
    }

    /**
     * Sets the image resource for a given team.
     *
     * @param view The ImageView to set the image on.
     * @param teamKey The identifier for the team's image resource.
     */
    private fun setTeamImage(view: ImageView, teamKey: String) {
        val resourceId = context.resources.getIdentifier("p$teamKey", "drawable", context.packageName)
        view.setImageResource(resourceId)
    }

    /**
     * Adjusts the width of the team image to represent flags correctly.
     *
     * @param teamKey The identifier for the team to check.
     */
    private fun adjustImageWidthIfNecessary(teamKey: String) {
        if (teamKey.length < 4) {
            val dpValue = 60
            val pxValue = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpValue.toFloat(),
                context.resources.displayMetrics
            ).toInt()
            binding.ivHorizontalGuess.layoutParams.width = pxValue
            binding.ivHorizontalGuess.requestLayout()
        }
    }

    /**
     * Hides the keyboard for a cleaner user experience.
     */
    private fun hideKeyboard() {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.search.windowToken, 0)
    }
}
