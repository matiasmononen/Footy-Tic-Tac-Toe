package com.mraximentertainment.balliq.singleplayer

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mraximentertainment.balliq.databinding.ActivityRecordBinding

/**
 * RecordActivity is responsible for displaying the player's records for different regions.
 */
class RecordActivity : AppCompatActivity() {

    // Binding for activity layout
    private lateinit var binding: ActivityRecordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using ViewBinding
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load records from SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val records = loadRecords(sharedPreferences)

        // Update UI with the retrieved records
        updateUI(records)
    }

    /**
     * Data class to encapsulate score and time for a record.
     */
    private data class Record(val score: Int, val time: Float)

    /**
     * Retrieves records for different regions from SharedPreferences.
     *
     * @param sharedPreferences The SharedPreferences instance.
     * @return A map of region names to their corresponding records.
     */
    private fun loadRecords(sharedPreferences: SharedPreferences): Map<String, Record> {
        return mapOf(
            "World" to Record(
                score = sharedPreferences.getInt("World", 0),
                time = sharedPreferences.getFloat("World time", DEFAULT_TIME)
            ),
            "England" to Record(
                score = sharedPreferences.getInt("England", 0),
                time = sharedPreferences.getFloat("England time", DEFAULT_TIME)
            ),
            "Spain" to Record(
                score = sharedPreferences.getInt("Spain", 0),
                time = sharedPreferences.getFloat("Spain time", DEFAULT_TIME)
            ),
            "Germany" to Record(
                score = sharedPreferences.getInt("Italy", 0), // Potential bug: Key should likely be "Germany"
                time = sharedPreferences.getFloat("Italy time", DEFAULT_TIME) // Same as above
            )
        )
    }

    /**
     * Updates the UI elements with record data.
     *
     * @param records A map containing records for different regions.
     */
    private fun updateUI(records: Map<String, Record>) {
        binding.worldRecord.text = formatRecordMessage(records["World"] ?: Record(0, DEFAULT_TIME))
        binding.englandRecord.text = formatRecordMessage(records["England"] ?: Record(0, DEFAULT_TIME))
        binding.spainRecord.text = formatRecordMessage(records["Spain"] ?: Record(0, DEFAULT_TIME))
        binding.germanyRecord.text = formatRecordMessage(records["Germany"] ?: Record(0, DEFAULT_TIME))
    }

    /**
     * Formats a record into a user-friendly message.
     *
     * @param record The record to format.
     * @return A string representation of the record.
     */
    private fun formatRecordMessage(record: Record): String {
        return if (record.time > DEFAULT_TIME && record.score == 0) {
            "N/A"
        } else {
            "${record.score}/9 - %.2f S".format(record.time)
        }
    }

    companion object {
        // Default value for time if no record exists
        private const val DEFAULT_TIME = 10000000F
    }
}
