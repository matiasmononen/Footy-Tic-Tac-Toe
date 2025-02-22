package com.mraximentertainment.balliq.multiplayer

import android.content.Context
import android.os.CountDownTimer
import android.os.Vibrator
import android.view.View
import android.widget.LinearLayout
import com.mraximentertainment.balliq.helpers.GuessDialog
import com.mraximentertainment.balliq.databinding.ActivityFootytttBinding
import com.mraximentertainment.balliq.helpers.isActivityRunning
import com.mraximentertainment.balliq.helpers.setImage

/**
 * A utility class to manage and update the UI elements in the FootytttActivity.
 *
 * @property context The application or activity context.
 * @property binding The view binding for the activity layout.
 * @property activity The parent activity instance.
 */
class UiHelper(private val context: Context, private val binding: ActivityFootytttBinding, val activity: FootytttActivity) {

    var teamsList: List<String>? = null
    lateinit var gameLogic: GameLogic
    private lateinit var guessDialog: GuessDialog

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private val shirtIVs = arrayOf(           // ImageViews for player shirt images
        binding.image1, binding.image2, binding.image3,
        binding.image4, binding.image5, binding.image6,
        binding.image7, binding.image8, binding.image9
    )
    private val nameTVs = arrayOf(            // TextViews for displaying names in boxes
        binding.tvName1, binding.tvName2, binding.tvName3,
        binding.tvName4, binding.tvName5, binding.tvName6,
        binding.tvName7, binding.tvName8, binding.tvName9
    )
    private val logoIvs = arrayOf(            // ImageViews for team logos
        binding.key1, binding.key2, binding.key3,
        binding.key4, binding.key5, binding.key6
    )
    private val timerTextView = binding.tvCountdown
    private val loadingDialog = binding.loading

    // Timers

    // Timer to automatically pass turn if no guess is made
    private val countDownTimer = object : CountDownTimer(60000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            // Update the timer display every second
            val seconds = (millisUntilFinished / 1000).toInt()
            val timeLeftFormatted = String.format("%02d", seconds % 60)
            binding.tvCountdown.text = timeLeftFormatted
        }

        override fun onFinish() {
            // Handle timeout for the player's turn
            if (::guessDialog.isInitialized && guessDialog.isShowing) {
                guessDialog.dismiss()
            }
            gameLogic.makeGuess("0", gameLogic.playerId, "")
        }
    }

    // Timer to automatically grant player the win if opponent is unresponsive
    val opponentTimer = object : CountDownTimer(80000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            // Opponent's turn timer, no UI update needed
        }

        override fun onFinish() {
            gameLogic.updateGameWin(gameLogic.playerId)
        }
    }

    /**
     * Starts the player's turn timer if the opponent is found.
     */
    fun startTimer() {
        if (gameLogic.opponentFound) {
            countDownTimer.start()
            timerTextView.visibility = View.VISIBLE
        }
    }

    /**
     * Stops the player's turn timer and hides the timer display.
     */
    fun stopTimer() {
        countDownTimer.cancel()
        timerTextView.visibility = View.GONE
    }

    /**
     * Initializes the UI by setting up the player's shirt and showing a loading dialog.
     *
     * @param playerShirt The player's chosen shirt design.
     */
    fun initializeUi(playerShirt: String) {
        loadingDialog.contentDescription = "Waiting for an Opponent"
        loadingDialog.visibility = View.VISIBLE
        setImage(binding.playerShirt, "p$playerShirt", context)
    }

    /**
     * Updates the image and name for a specific box on the game board.
     *
     * @param boxNumber The number of the box to update (1-9).
     * @param shirt The player's shirt design to display.
     * @param name The name to display in the box.
     */
    fun updateBoxImage(boxNumber: Int, shirt: String, name: String) {
        val index = boxNumber - 1
        val iv = shirtIVs[index]
        val tv = nameTVs[index]
        val resourceId = context.resources.getIdentifier("p$shirt", "drawable", context.packageName)
        iv.setImageResource(resourceId)
        tv.text = name
    }

    /**
     * Updates the UI to reflect whose turn it is (player or opponent).
     *
     * @param isPlayerTurn True if it's the player's turn, false otherwise.
     */
    fun updateTurnUI(isPlayerTurn: Boolean) {
        if (!isPlayerTurn) {
            stopTimer()
            opponentTimer.start()
            vibrator.vibrate(50) // Notify the player via vibration
            setBackgroundResource(binding.llPlayer1, "unroundback")
            setBackgroundResource(binding.llPlayer2, "round_back")
        } else {
            startTimer()
            opponentTimer.cancel()
            setBackgroundResource(binding.llPlayer1, "round_back")
            setBackgroundResource(binding.llPlayer2, "unroundback")
        }
    }

    /**
     * Sets the background resource for a given layout.
     *
     * @param layout The layout to update.
     * @param key The key for the drawable resource.
     */
    private fun setBackgroundResource(layout: LinearLayout, key: String) {
        val resource = context.resources.getIdentifier(key, "drawable", context.packageName)
        layout.setBackgroundResource(resource)
    }

    /**
     * Sets up click listeners for the game board squares.
     *
     * @param showSearch A callback to display the search dialog for a selected box.
     */
    fun initializeSquares(showSearch: (String?, String?, String) -> Unit) {
        shirtIVs.forEachIndexed { index, iv ->
            val box = index + 1
            val teamIndex1 = index / 3
            val teamIndex2 = 3 + (index % 3)
            iv.setOnClickListener {
                if (!gameLogic.doneBoxes.contains("$box") && gameLogic.turn == gameLogic.playerId && gameLogic.opponentFound) {
                    showSearch(teamsList?.get(teamIndex1), teamsList?.get(teamIndex2), "$box")
                }
            }
        }
    }

    /**
     * Updates the logos displayed in the UI.
     */
    fun setUpLogos() {
        logoIvs.forEachIndexed { index, iv ->
            val logo = teamsList!![index].split(" ")[0]
            setImage(iv, "p$logo", context)
        }
    }

    /**
     * Updates the opponent's shirt image in the UI.
     *
     * @param key The key for the drawable resource of the opponent's shirt.
     */
    private fun setOpponentShirt(key: String) {
        setImage(binding.opponentShirt, key, context)
    }

    /**
     * Displays the game over dialog with a custom message.
     *
     * @param text The message to display in the dialog.
     */
    fun showGameOverDialog(text: String) {
        val dialog = WinDialog(activity, text)
        dialog.setCancelable(false)
        // Wait for two seconds for player to see the final guess
        Thread.sleep(2000)
        if (context.isActivityRunning()) {
            dialog.show()
        }
    }

    /**
     * Starts the game by hiding the loading dialog and initializing timers.
     */
    fun startGame() {
        if (loadingDialog.visibility == View.VISIBLE) {
            loadingDialog.visibility = View.GONE
            if (gameLogic.turn == gameLogic.playerId) {
                startTimer()
            }
        }
        setOpponentShirt("p${gameLogic.opponentShirt}")
    }

    /**
     * Displays the game code in the UI.
     *
     * @param code The game code to display.
     */
    fun showCode(code: String) {
        binding.tvCode.text = "CODE: $code"
    }
}
