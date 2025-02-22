package com.mraximentertainment.balliq.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.mraximentertainment.balliq.helpers.removeDiacriticalMarks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * **DatabaseHelper**
 *
 * A utility class for managing and querying a pre-packaged SQLite database.
 * Provides methods for initializing the database, running queries, and handling predefined data structures.
 */
object DatabaseHelper {

    private const val DATABASE_NAME = "tictac.db"
    private const val DATABASE_VERSION = 7

    /**
     * Executes a raw SQL query and retrieves results as a list of strings.
     * This method assumes the query selects a column named "name".
     *
     * @param query The SQL query string.
     * @param selectionArgs Optional query arguments.
     * @param context The application context.
     * @return A list of results as strings.
     */
    private fun executeQuery(query: String, selectionArgs: Array<String>? = null, context: Context): List<String> {
        val dbPath = context.getDatabasePath(DATABASE_NAME).absolutePath
        val results = mutableListOf<String>()

        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
        val cursor = db.rawQuery(query, selectionArgs)

        if (cursor.moveToFirst()) {
            do {
                val columnIndex = cursor.getColumnIndexOrThrow("name")
                results.add(cursor.getString(columnIndex))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return results
    }

    /**
     * Fetches unique suggestions by ordering results based on their presence in wage
     * table to ensure the most relevant players appear first.
     *
     * @param context The application context.
     * @return A list of suggestions as pairs of raw and normalized strings.
     */
    fun getSuggestions(context: Context): List<Pair<String, String>> {
        val query = """
            SELECT DISTINCT t1.name FROM actualAnswers t1
            LEFT JOIN wage t2 ON t1.name = t2.name
            ORDER BY CASE WHEN t2.name IS NOT NULL THEN 0 ELSE 1 END, t1.name
        """
        val suggestionList = executeQuery(query, null, context)
        return suggestionList.map { Pair(it, removeDiacriticalMarks(it).lowercase()) }
    }

    /**
     * Retrieves answers by determining the players that have played for both given teams.
     *
     * @param team1 The first team (table name).
     * @param team2 The second team (table name or nation filter condition).
     * @param context The application context.
     * @return A list of answers as strings.
     */
    fun getAnswers(team1: String?, team2: String?, context: Context): MutableList<String> {
        val result = mutableListOf<String>()

        if (team1.isNullOrBlank() || team2.isNullOrBlank()) return result

        val (query, selectionArgs) = if (team2.indexOf(" ") == -1) {
            // Simple join query
            "SELECT $team1.name FROM $team2 INNER JOIN $team1 ON $team2.name = $team1.name" to null
        } else {
            // Query with nationality filter
            "SELECT $team1.name FROM $team1 WHERE nationality = ?" to arrayOf(team2)
        }

        result.addAll(executeQuery(query, selectionArgs, context))
        return result
    }

    /**
     * Selects distinct vertical and horizontal teams from predefined lists based on the map.
     *
     * @param map The selected region or category.
     * @return A list of selected team names.
     */
    fun selectTeams(map: String): List<String> {
        val verticalTeams = when (map) {
            "italy" -> italianTeams
            "spain" -> spanishTeams
            "england" -> englishTeams
            else -> regularTeams
        }
        val horizontalTeams = nations + verticalTeams

        val shuffledVerticalTeams = verticalTeams.shuffled()
        val vertical = shuffledVerticalTeams.take(3)

        val shuffledHorizontalTeams = horizontalTeams.shuffled()
        val unusedHorizontalTeams = shuffledHorizontalTeams.filter { it !in vertical }
        val horizontal = unusedHorizontalTeams.take(3)

        return vertical + horizontal
    }

    /**
     * Copies the database from assets if the current version is outdated.
     *
     * @param context The application context.
     * @param databaseName The name of the database file.
     * @param onComplete A callback triggered when the operation is completed.
     */
    fun copyDatabaseIfNeeded(context: Context, databaseName: String, onComplete: () -> Unit) {
        val prefs = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val oldVersion = prefs.getInt("DatabaseVersion", 0)

        if (oldVersion < DATABASE_VERSION) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    copyDatabaseFromAssets(context, databaseName)
                    prefs.edit().putInt("DatabaseVersion", DATABASE_VERSION).apply()
                    withContext(Dispatchers.Main) { onComplete() }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else {
            onComplete()
        }
    }

    /**
     * Copies the database file from the assets folder to the app's database directory.
     *
     * @param context The application context.
     * @param databaseName The name of the database file.
     * @throws IOException If an error occurs during file operations.
     */
    @Throws(IOException::class)
    private fun copyDatabaseFromAssets(context: Context, databaseName: String) {
        val outputFile = File(context.getDatabasePath(databaseName).path)
        if (outputFile.exists()) outputFile.delete()

        context.assets.open(databaseName).use { inputStream ->
            FileOutputStream(outputFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    // Predefined team lists
    private val nations = listOf(
        "nl NED", "it ITA", "uy URU", "ar ARG", "br BRA", "pt POR", "co COL", "wcup",
        "pweuro", "eng ENG", "fr FRA", "be BEL", "es ESP", "de GER"
    )
    private val regularTeams = listOf(
        "monaco", "barcelona", "real_madrid", "chelsea", "arsenal", "manchester_city", "xere",
        "xmls", "juventus", "tottenham", "manchester_utd", "inter", "paris_sg", "liverpool",
        "wclwin", "weleag", "bayern_munich", "dortmund", "milan", "roma", "xsaudi"
    )
    private val englishTeams = listOf(
        "chelsea", "arsenal", "manchester_city", "tottenham", "manchester_utd",
        "liverpool", "everton", "brighton", "newcastle_utd", "aston_villa",
        "west_ham", "fulham", "wolves", "crystal_palace", "bournemouth"
    )
    private val spanishTeams = listOf(
        "barcelona", "real_madrid", "sevilla", "valencia", "villarreal",
        "celta_vigo", "betis", "athletic_club", "real_sociedad"
    )
    private val italianTeams = listOf(
        "juventus", "roma", "napoli", "milan", "inter", "atalanta",
        "lazio", "udinese", "fiorentina"
    )
}
