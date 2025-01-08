package com.mraximentertainment.balliq.multiplayer

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.*
import com.mraximentertainment.balliq.BuildConfig
import com.mraximentertainment.balliq.database.DatabaseHelper
import com.mraximentertainment.balliq.helpers.GuessDialog
import com.mraximentertainment.balliq.helpers.MyDialogCallback
import com.mraximentertainment.balliq.databinding.ActivityFootytttBinding
import com.mraximentertainment.balliq.helpers.formatGuess
import com.mraximentertainment.balliq.helpers.isActivityRunning

/**
 * Activity for managing a multiplayer trivia game.
 * Handles UI interactions, game logic, and communication with Firebase.
 */
class FootytttActivity : AppCompatActivity(), MyDialogCallback {

    // View binding for accessing UI elements
    private lateinit var binding: ActivityFootytttBinding

    // Context for accessing resources and services
    private lateinit var context: Context

    // Helper classes for game logic and UI management
    private lateinit var gameLogic: GameLogic
    private lateinit var uiHelper: UiHelper
    private lateinit var listeners: GameRepository

    // Firebase database reference for communication with Firebase
    private val database = FirebaseDatabase.getInstance().getReferenceFromUrl(BuildConfig.FIREBASE_ADDRESS)

    // Game state variables
    private lateinit var suggestionsWithNormalized: List<Pair<String, String>>
    private var suggestionNameList = mutableListOf<String>()
    private var answerList = mutableListOf<String>()
    private var selectedBox = ""
    private var type = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFootytttBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = this

        // Extract game type and private code from intent
        val privateCode = intent.getStringExtra("code") ?: "d"
        type = if (privateCode == "d") "connections" else "private_connections"

        // Initialize helper classes and set up game state
        initializeHelpers(privateCode)
        initializeGameState()

        // Attach Firebase listeners for real-time updates
        listeners.attachConnectionEventListener()

        // Set up UI button listeners for game actions
        setButtonListeners()
    }

    /**
     * Initializes helper classes and their dependencies.
     */
    private fun initializeHelpers(privateCode: String) {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        uiHelper = UiHelper(this, binding, this)
        gameLogic = GameLogic(database, uiHelper)
        uiHelper.gameLogic = gameLogic
        listeners = GameRepository(gameLogic, uiHelper, type, privateCode, this)

        // Set player shirt based on shared preferences
        gameLogic.playerShirt = sharedPreferences.getString("equipped", "basic1").toString()
    }

    /**
     * Initializes the game state and UI components.
     */
    private fun initializeGameState() {
        uiHelper.initializeUi(gameLogic.playerShirt)
        suggestionsWithNormalized = DatabaseHelper.getSuggestions(this)
    }

    /**
     * Sets up listeners for UI buttons to handle game actions.
     */
    private fun setButtonListeners() {
        // Button to cancel the game if opponent is not found
        binding.btnCancel.setOnClickListener {
            if (!gameLogic.opponentFound) finish()
        }

        // Button to pass the turn to the opponent
        binding.btnPassTurn.setOnClickListener {
            if (gameLogic.turn == gameLogic.playerId) {
                gameLogic.makeGuess("0", gameLogic.playerId, "")
            }
        }

        // Button to give up and update the game state as lost
        binding.btnGiveUp.setOnClickListener {
            if (gameLogic.opponentFound && context.isActivityRunning()) {
                gameLogic.updateGameWin(gameLogic.opponentId)
                uiHelper.showGameOverDialog("You lost due to leaving")
            }
        }

        // Initialize game squares with listener for showing search dialog
        uiHelper.initializeSquares { team1, team2, box ->
            showSearch(team1, team2, box)
        }
    }

    /**
     * Displays a search dialog for a selected game square.
     */
    private fun showSearch(team1: String?, team2: String?, box: String) {
        selectedBox = box
        answerList = DatabaseHelper.getAnswers(team1, team2, this)

        val guessDialog = GuessDialog(
            this, this, team1.orEmpty(), team2.orEmpty(),
            suggestionNameList, suggestionsWithNormalized
        )
        if (context.isActivityRunning()) guessDialog.show()
    }

    /**
     * Handles the callback when a guess is received from the user.
     */
    override fun onDataReceived(guess: String) {
        if (guess in answerList) {
            val formattedGuess = formatGuess(guess)
            gameLogic.makeGuess(selectedBox, gameLogic.playerId, formattedGuess)
        } else {
            gameLogic.makeGuess("0", gameLogic.playerId, "")
        }
    }

    override fun onBackPressed() {
        // Disable the back button to prevent accidental exits
    }

    override fun onPause() {
        // Clean up listeners and game state when the activity is paused
        listeners.cleanUpListeners()
        handleGameOnPause()

        if (type == "connections" && !gameLogic.opponentFound) {
            listeners.cleanUpGame()
            finish()
        }
        super.onPause()
    }

    /**
     * Handles the game state when the activity is paused.
     */
    private fun handleGameOnPause() {
        if (!gameLogic.gameOver && gameLogic.opponentFound) {
            gameLogic.updateGameWin(gameLogic.opponentId)
            uiHelper.showGameOverDialog("You lost due to leaving")
            uiHelper.stopTimer()
        }
    }

    override fun onDestroy() {
        // Clean up listeners and remove database references when the activity is destroyed
        if (type != "connections") {
            listeners.cleanUpListeners()
            if (!gameLogic.opponentFound) {
                database.child(type).child(gameLogic.connectionId).removeValue()
                listeners.removeConnectionEventListener(type)
            } else if (!gameLogic.gameOver) {
                gameLogic.updateGameWin(gameLogic.opponentId)
            }
        }
        super.onDestroy()
    }
}
