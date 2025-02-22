package com.mraximentertainment.balliq.multiplayer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.database.*
import com.mraximentertainment.balliq.BuildConfig
import com.mraximentertainment.balliq.database.DatabaseHelper
import com.mraximentertainment.balliq.navigation.MultiplayerActivity
import kotlin.random.Random

/**
 * Repository class for managing the game's connection, logic, and Firebase database interactions.
 * Handles player matchmaking, game state, and event listeners.
 *
 * @param gameLogic Core game logic.
 * @param uiHelper Helper for updating UI components.
 * @param type The game type (e.g., "connections", "private_connections").
 * @param privateCode Private code for private game connections.
 * @param context The application context for accessing resources and launching activities.
 */
class GameRepository(
    private val gameLogic: GameLogic,
    private val uiHelper: UiHelper,
    private val type: String,
    private val privateCode: String,
    private val context: Context
) {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl(
        BuildConfig.FIREBASE_ADDRESS
    )

    private var turnEventListener: ChildEventListener? = null
    private var connectionEventListener: ValueEventListener? = null
    private var wonEventListener: ValueEventListener? = null
    private var status: String = "connecting"

    /**
     * Attaches a listener for connections to find opponents or manage matchmaking.
     */
    fun attachConnectionEventListener() {
        connectionEventListener = database.child(type).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!gameLogic.opponentFound) {
                    processConnections(snapshot)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseListener", "Error: ${error.message}", error.toException())
            }
        })
    }

    /**
     * Processes connections in the database and sets up a match if possible.
     *
     * @param snapshot Snapshot containing the connections of the games type
     */
    private fun processConnections(snapshot: DataSnapshot) {
        for (connection in snapshot.children) {
            if (!gameLogic.opponentFound) {
                val connectionId = connection.key ?: continue
                val playerCount = connection.childrenCount.toInt()

                when {
                    playerCount == 2 && status == "waiting" -> setupConnection(connection, connectionId, isTwoPlayer = true)
                    playerCount == 1 && status != "waiting" && (connectionId.takeLast(6) == privateCode || type == "connections") ->
                        setupConnection(connection, connectionId, isTwoPlayer = false)
                }
            }
        }

        // If no suitable connection found, create a new connection
        if (!gameLogic.opponentFound && status != "waiting") {
            if (type == "connections" || privateCode.length > 6) {
                createNewConnection()
            } else if (privateCode.length == 6) {
                handleFailedMatchmaking()
            }
        }
    }

    /**
     * Adds a player to an existing match or completes match making in a full game
     * if the user is part of the game.
     *
     * @param connection The database connection being processed
     * @param connectionId ID of the connection
     * @param isTwoPlayer Indicates if the connection has currently two players
     */
    private fun setupConnection(connection: DataSnapshot, connectionId: String, isTwoPlayer: Boolean) {
        if (isTwoPlayer) {
            var playerFound = false
            // Iterate through players
            for (player in connection.children) {
                val playerId = player.key ?: continue
                if (playerId == gameLogic.playerId) {
                    playerFound = true
                } else if (playerFound) {
                    // If there is another player with the user match making can be completed.
                    initializeOpponent(player, connectionId)
                    return
                }
            }
        } else {
            // Add player to the game
            database.child(type).child(connectionId).child(gameLogic.playerId)
                .child("shirt").setValue(gameLogic.playerShirt)
            val player = connection.children.first()
            // Retrieve team list from the connection and complete match making.
            uiHelper.teamsList = player.child("teams").getValue(object : GenericTypeIndicator<List<String>>() {})
            initializeOpponent(player, connectionId)
        }
    }

    /**
     * Initializes the opponent's data and starts the game.
     *
     * @param player Data snapshot containing the opponents information
     * @param connectionId ID corresponding to the games connection
     */
    private fun initializeOpponent(player: DataSnapshot, connectionId: String) {
        gameLogic.opponentId = player.key ?: return
        gameLogic.connectionId = connectionId
        gameLogic.opponentFound = true
        gameLogic.opponentShirt = resolveShirtConflict(player.child("shirt").value.toString())

        uiHelper.setUpLogos()

        gameLogic.turn = if (status == "waiting") gameLogic.playerId else gameLogic.opponentId
        uiHelper.updateTurnUI(gameLogic.turn == gameLogic.playerId)

        attachTurnEventListener()
        attachWonEventListener()
        uiHelper.startGame()

        removeConnectionEventListener(type)
    }

    /**
     * Creates a new connection in the database for matchmaking.
     */
    private fun createNewConnection() {
        val newConnectionId = generateConnectionId()
        gameLogic.connectionId = newConnectionId
        uiHelper.teamsList = DatabaseHelper.selectTeams("world")
        uiHelper.setUpLogos()

        if (type == "private_connections") {
            uiHelper.showCode(newConnectionId.takeLast(6))
        }

        database.child(type).child(newConnectionId).child(gameLogic.playerId).apply {
            child("teams").setValue(uiHelper.teamsList)
            child("shirt").setValue(gameLogic.playerShirt)
        }
        status = "waiting"
    }

    /**
     * Resolves shirt conflicts by assigning an alternate shirt if needed.
     */
    private fun resolveShirtConflict(opponentShirt: String): String {
        return if (opponentShirt == gameLogic.playerShirt) {
            if (opponentShirt == "basic1") "basic2" else "basic1"
        } else {
            opponentShirt
        }
    }

    /**
     * Generates a unique connection ID using the current time and a random value.
     */
    private fun generateConnectionId(): String {
        return "${System.currentTimeMillis()}_${Random.nextInt()}"
    }

    /**
     * Attaches a listener for game turns.
     */
    fun attachTurnEventListener() {
        if (turnEventListener == null) {
            turnEventListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.childrenCount == 3L) {
                        val playerName = snapshot.child("name").getValue(String::class.java) ?: ""
                        val boxPosition = snapshot.child("box_position").getValue(String::class.java)?.toInt()
                        val playerId = snapshot.child("player_id").getValue(String::class.java)

                        // Record guess if it is valid
                        if (boxPosition != null && playerId != null) {
                            uiHelper.updateTurnUI(gameLogic.turn == gameLogic.playerId)
                            gameLogic.handleGuess(playerName, boxPosition, playerId)
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Log.e("TurnListener", "Error: ${error.message}", error.toException())
                }
            }
            database.child("turns").child(gameLogic.connectionId).addChildEventListener(turnEventListener!!)
        }
    }

    /**
     * Attaches a listener for game victory conditions.
     */
    private fun attachWonEventListener() {
        if (wonEventListener == null) {
            wonEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild("player_id")) {
                        val winner = snapshot.child("player_id").getValue(String::class.java)
                        val resultText = when (winner) {
                            gameLogic.playerId -> "You Won!"
                            gameLogic.opponentId -> "You Lost!"
                            else -> "It is a draw."
                        }
                        uiHelper.showGameOverDialog(resultText)
                        gameLogic.gameOver = true
                        cleanUpGame()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("WonListener", "Error: ${error.message}", error.toException())
                }
            }
            database.child("won").child(gameLogic.connectionId).addValueEventListener(wonEventListener!!)
        }
    }

    /**
     * Removes the turn event listener.
     */
    private fun removeTurnEventListener() {
        turnEventListener?.let {
            database.child("turns").child(gameLogic.connectionId).removeEventListener(it)
            turnEventListener = null
        }
    }

    fun removeConnectionEventListener(type: String) {
        connectionEventListener?.let {
            database.child(type).removeEventListener(it)
            connectionEventListener = null
        }
    }

    private fun removeWonEventListener() {
        wonEventListener?.let {
            database.child("won").child(gameLogic.connectionId).removeEventListener(it)
            wonEventListener = null
        }
    }

    /**
     * Cleans up game resources and listeners.
     */
    fun cleanUpGame() {
        cleanUpListeners()
        database.child("turns").child(gameLogic.connectionId).removeValue()
        database.child("won").child(gameLogic.connectionId).removeValue()
        database.child(type).child(gameLogic.connectionId).removeValue()

        uiHelper.stopTimer()
        uiHelper.opponentTimer.cancel()
    }

    /**
     * Cleans up all event listeners.
     */
    fun cleanUpListeners() {
        removeWonEventListener()
        removeTurnEventListener()
    }

    /**
     * Handles matchmaking failure by redirecting to a multiplayer activity.
     */
    private fun handleFailedMatchmaking() {
        val overIntent = Intent(context, MultiplayerActivity::class.java).apply {
            putExtra("failed", true)
        }
        removeConnectionEventListener(type)
        context.startActivity(overIntent)

        if (context is Activity) {
            context.finish()
        }
    }
}
