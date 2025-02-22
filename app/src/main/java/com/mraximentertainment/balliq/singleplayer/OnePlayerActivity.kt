package com.mraximentertainment.balliq.singleplayer

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mraximentertainment.balliq.database.DatabaseHelper
import com.mraximentertainment.balliq.databinding.ActivityOnePlayerBinding
import com.mraximentertainment.balliq.helpers.GuessDialog
import com.mraximentertainment.balliq.helpers.MyDialogCallback
import com.mraximentertainment.balliq.helpers.formatGuess
import com.mraximentertainment.balliq.helpers.isActivityRunning
import com.mraximentertainment.balliq.helpers.setImage
import java.util.*
import kotlin.concurrent.timerTask

/**
 * Activity for the single-player game mode. This activity handles the game logic,
 * including displaying team logos, accepting guesses, timing the game, and showing the game-over dialog.
 */
class OnePlayerActivity : AppCompatActivity(), MyDialogCallback {

    private lateinit var binding: ActivityOnePlayerBinding
    private var timer: Timer? = null
    private var elapsedTime = 0.0
    private lateinit var selectedImageView: ImageView
    private lateinit var selectedTextView: TextView
    private lateinit var suggestionsWithNormalized: List<Pair<String, String>>

    lateinit var answerList: List<String>
    val completedBoxes = mutableListOf<String>()
    private lateinit var playerShirt: String
    private var teamNames: List<String>? = null
    private lateinit var mapName: String
    var selectedBoxIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializePlayerShirt()
        initializeGameData()
        setupTimer()
        setupImageClickListeners()
        setupButtonListeners()
    }

    /**
     * Initializes the player's shirt based on the shared preferences.
     */
    private fun initializePlayerShirt() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        playerShirt = sharedPreferences.getString("equipped", "basic1").orEmpty()
        setImage(binding.playerShirt, "p$playerShirt", this)
    }

    /**
     * Initializes the game data, including map name, team names, and suggestions.
     */
    private fun initializeGameData() {
        mapName = intent.extras?.getString("map").orEmpty().ifEmpty { "default" }
        teamNames = DatabaseHelper.selectTeams(mapName.lowercase())
        suggestionsWithNormalized = DatabaseHelper.getSuggestions(this)
        setupTeamLogos()
    }

    /**
     * Sets up the team logos by retrieving the team names from the database and assigning
     * the corresponding logos to the image views.
     */
    private fun setupTeamLogos() {
        val logoImageViews = listOf(
            binding.key1, binding.key2, binding.key3,
            binding.key4, binding.key5, binding.key6
        )
        teamNames?.forEachIndexed { index, team ->
            val logo = team.split(" ")[0]
            setImage(logoImageViews[index], "p$logo", this)
        }
    }

    /**
     * Sets up the timer to update the elapsed time every 10 milliseconds.
     */
    private fun setupTimer() {
        timer = Timer().apply {
            schedule(timerTask {
                runOnUiThread {
                    elapsedTime += TIMER_INTERVAL
                    binding.tvCountdown.text = String.format(Locale.US, "%.2f", elapsedTime)
                }
            }, 0, TIMER_PERIOD_MS)
        }
    }

    /**
     * Sets up the listeners for the action buttons.
     */
    private fun setupButtonListeners() {
        binding.btnGiveUp.setOnClickListener { endGame() }
    }

    /**
     * Sets up the listeners for image clicks to allow the player to make guesses.
     */
    private fun setupImageClickListeners() {
        val imageTextPairs = listOf(
            binding.image1 to binding.tvName1,
            binding.image2 to binding.tvName2,
            binding.image3 to binding.tvName3,
            binding.image4 to binding.tvName4,
            binding.image5 to binding.tvName5,
            binding.image6 to binding.tvName6,
            binding.image7 to binding.tvName7,
            binding.image8 to binding.tvName8,
            binding.image9 to binding.tvName9
        )

        // Sets listeners to show guess dialog corresponding to the boxes teams
        imageTextPairs.forEachIndexed { index, (imageView, textView) ->
            imageView.setOnClickListener {
                if (!completedBoxes.contains(index.toString())) {
                    val (team1, team2) = getTeamsForIndex(index)
                    selectedBoxIndex = index
                    showGuessDialog(team1, team2, imageView, textView)
                }
            }
        }
    }

    /**
     * Retrieves the team names corresponding to the given index.
     *
     * @param index Index of the box
     * @return Returns a pair containing the teams corresponding to the index
     */
    private fun getTeamsForIndex(index: Int): Pair<String?, String?> {
        val teamIndex1 = index / 3
        val teamIndex2 = 3 + (index % 3)
        return teamNames?.getOrNull(teamIndex1) to teamNames?.getOrNull(teamIndex2)
    }

    /**
     * Displays the guess dialog where the player can input their guess for the selected team pair.
     */
    private fun showGuessDialog(
        team1: String?,
        team2: String?,
        imageView: ImageView,
        textView: TextView
    ) {
        selectedImageView = imageView
        selectedTextView = textView

        answerList = DatabaseHelper.getAnswers(team1, team2, this)
        if (team1 != null && team2 != null) {
            val guessDialog = GuessDialog(
                this, this, team1, team2,
                answerList.toMutableList(), suggestionsWithNormalized
            )
            if (isActivityRunning()) {
                guessDialog.show()
            }
        }
    }

    /**
     * Ends the game, cancels the timer, and displays the game-over dialog with the final score and time.
     */
    private fun endGame() {
        timer?.cancel()
        val finalTime = String.format(Locale.US, "%.2f", elapsedTime).toFloat()
        val score = completedBoxes.size
        val gameOverDialog = GameOverDialog(this, score, finalTime, mapName)
        gameOverDialog.setCancelable(false)
        if (isActivityRunning()) {
            gameOverDialog.show()
        }
    }

    /**
     * Handles the received guess and updates the UI if the guess is correct.
     *
     * @param guess The guess received from the callback
     */
    override fun onDataReceived(guess: String) {
        if (guess in answerList) {
            completedBoxes.add(selectedBoxIndex.toString())
            selectedTextView.text = formatGuess(guess)
            setImage(selectedImageView, "p$playerShirt", this)
        }
        // End the game if the grid is completed
        if (completedBoxes.size == MAX_BOXES) endGame()
    }

    override fun onBackPressed() {
        // Disable back button functionality
    }

    companion object {
        private const val TIMER_INTERVAL = 0.01
        private const val TIMER_PERIOD_MS = 10L
        private const val MAX_BOXES = 9
    }
}
