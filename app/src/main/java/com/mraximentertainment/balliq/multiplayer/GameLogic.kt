package com.mraximentertainment.balliq.multiplayer

import com.google.firebase.database.DatabaseReference
import kotlin.random.Random

/**
 * Handles the core game logic for managing turns, determining winners, and
 * interacting with Firebase for a multiplayer game.
 *
 * @property database Firebase database reference for game data storage.
 * @property uiHelper Helper class for updating the game's UI.
 */
class GameLogic(val database: DatabaseReference, private val uiHelper: UiHelper) {

    var connectionId = "0"
    var turn = ""
    var gameOver = false
    var opponentFound = false

    private var doneGuesses = 0
    var opponentId = "0"
    var playerShirt = ""
    var opponentShirt = ""
    var playerId = "${System.currentTimeMillis()}_${Random.nextInt()}"

    private val boxesSelectedBy = Array(9) { "" } // Tracks who selected each box (player or opponent)
    val doneBoxes = mutableListOf("0")       // Tracks the boxes that have already been played

    /**
     * Processes the player's guess and updates the game state.
     *
     * @param name The name entered for the guess.
     * @param boxPosition The board position selected (1-9).
     * @param player The ID of the player making the guess.
     */
    fun handleGuess(name: String, boxPosition: Int, player: String) {
        // Increment the number of guesses made
        doneGuesses += 1
        val index = boxPosition - 1

        // Switch turns after processing the current guess
        switchTurn()

        // Update the board state and UI for the guess
        if (boxPosition != 0 && player == playerId) {
            // Player's guess
            boxesSelectedBy[index] = playerId
            uiHelper.updateBoxImage(boxPosition, playerShirt, name)
            doneBoxes.add(boxPosition.toString())
        } else if (boxPosition != 0) {
            // Opponent's guess
            boxesSelectedBy[index] = opponentId
            uiHelper.updateBoxImage(boxPosition, opponentShirt, name)
            doneBoxes.add(boxPosition.toString())
        }

        // Check if the current guess leads to a win or draw
        if (checkPlayerWin(player)) {
            updateGameWin(player)
        } else if (isDraw()) {
            updateGameWin("draw")
        }

        // Update the UI to reflect the new turn
        uiHelper.updateTurnUI(turn == playerId)
    }

    /**
     * Sends a player's guess to Firebase.
     *
     * @param selectedBox The board position selected by the player (1-9).
     * @param player The ID of the player making the guess.
     * @param name The name associated with the guess.
     */
    fun makeGuess(selectedBox: String, player: String, name: String) {
        val guessData = mapOf(
            "box_position" to selectedBox,
            "player_id" to player,
            "name" to name
        )
        // Save the guess in Firebase under the current game session
        database.child("turns").child(connectionId).child((doneGuesses + 1).toString()).setValue(guessData)
    }

    /**
     * Checks whether the specified player has won the game.
     *
     * @param player The ID of the player to check for a win.
     * @return True if the player has won, false otherwise.
     */
    fun checkPlayerWin(player: String): Boolean {
        // Define all possible winning combinations on the board
        val winningCombinations = listOf(
            Triple(0, 1, 2), Triple(3, 4, 5), Triple(6, 7, 8), // Rows
            Triple(0, 3, 6), Triple(1, 4, 7), Triple(2, 5, 8), // Columns
            Triple(0, 4, 8), Triple(2, 4, 6)                  // Diagonals
        )

        // Check if any combination is fulfilled by the player
        return winningCombinations.any { combination ->
            boxesSelectedBy[combination.first] == player &&
            boxesSelectedBy[combination.second] == player &&
            boxesSelectedBy[combination.third] == player
        }
    }

    /**
     * Updates Firebase with the result of the game.
     *
     * @param winner The ID of the winning player, or "draw" if it's a tie.
     */
    fun updateGameWin(winner: String) {
        database.child("won").child(connectionId).child("player_id").setValue(winner)
    }

    /**
     * Checks if the game is a draw (all boxes filled without a winner).
     *
     * @return True if the game is a draw, false otherwise.
     */
    private fun isDraw(): Boolean = doneBoxes.size == 10

    /**
     * Switches the turn to the other player.
     */
    fun switchTurn() {
        turn = if (turn == playerId) opponentId else playerId
    }
}
