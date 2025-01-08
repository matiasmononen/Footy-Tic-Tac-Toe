package com.mraximentertainment.balliq

import com.mraximentertainment.balliq.database.DatabaseHelper
import org.junit.Test
import org.junit.Assert.*
import com.mraximentertainment.balliq.helpers.formatGuess

class UnitTest {

    @Test
    fun selectTeams_worldMap_returnsCorrectSize() {
        val teams = DatabaseHelper.selectTeams("world")

        // Verify that 6 unique teams are selected (3 vertical + 3 horizontal)
        assertEquals(6, teams.size)
    }

    @Test
    fun selectTeams_englandMap_returnsCorrectTeams() {
        val teams = DatabaseHelper.selectTeams("england")

        // Verify the size and ensure teams are selected correctly
        assertEquals(6, teams.size)
        assertTrue("No team names should be blank", teams.none { it.isBlank() })
    }

    @Test
    fun formatGuess_normalizesStringCorrectly() {
        val input1 = "Pierre-Emerick Aubameyang"
        val expected1 = "Aubameyang"
        assertEquals(expected1, formatGuess(input1))

        val input2 = "Frenkie de Jong"
        val expected2 = "de Jong"
        assertEquals(expected2, formatGuess(input2))

        val input3 = "Robert Lewandowski"
        val expected3 = "Lewandowski"
        assertEquals(expected3, formatGuess(input3))

        val input4 = "Cristiano Ronaldo"
        val expected4 = "Ronaldo"
        assertEquals(expected4, formatGuess(input4))
    }

}
