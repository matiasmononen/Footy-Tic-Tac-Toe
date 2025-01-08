package com.mraximentertainment.balliq.database

/**
 * A map containing team and player attributes mapped to their descriptions.
 * These descriptions can be used to provide human-readable information for UI or display purposes.
 */
val teamDescriptions = mapOf(
    "barcelona" to "Has played for Barcelona",
    "real_madrid" to "Has played for Real Madrid",
    "chelsea" to "Has played for Chelsea",
    "arsenal" to "Has played for Arsenal",
    "manchester_city" to "Has played for Manchester City",
    "manchester_utd" to "Has played for Manchester United",
    "liverpool" to "Has played for Liverpool",
    "tottenham" to "Has played for Spurs",
    "inter" to "Has played for Inter",
    "roma" to "Has played for Roma",
    "milan" to "Has played for AC Milan",
    "juventus" to "Has played for Juventus",
    "napoli" to "Has played for Napoli",
    "atalanta" to "Has played for Atalanta",
    "lazio" to "Has played for Lazio",
    "fiorentina" to "Has played for Fiorentina",
    "udinese" to "Has played for Udinese",
    "paris_sg" to "Has played for PSG",
    "monaco" to "Has played for Monaco",
    "bayern_munich" to "Has played for Bayern Munich",
    "dortmund" to "Has played for BvB",
    "sevilla" to "Has played for Sevilla",
    "valencia" to "Has played for Valencia",
    "villarreal" to "Has played for Villarreal",
    "real_sociedad" to "Has played for Real Sociedad",
    "athletic_club" to "Has played for Athletic Club",
    "celta_vigo" to "Has played for Celta Vigo",
    "betis" to "Has played for Real Betis",
    "everton" to "Has played for Everton",
    "brighton" to "Has played for Brighton",
    "newcastle_utd" to "Has played for Newcastle",
    "aston_villa" to "Has played for Aston Villa",
    "west_ham" to "Has played for West Ham",
    "fulham" to "Has played for Fulham",
    "wolves" to "Has played for Wolves",
    "bournemouth" to "Has played for Bournemouth",
    "crystal_palace" to "Has played for Crystal Palace",
    "xere" to "Has played in the Eredivisie",
    "xmls" to "Has played in the MLS",
    "xsaudi" to "Has played in the Saudi League",
    "wclwin" to "Has won the Champions League",
    "weleag" to "Has won the Europa League",
    "wcup" to "Has won the World Cup",
    "pweuro" to "Has won the Euros",
    "nl" to "Is Dutch",
    "it" to "Is Italian",
    "br" to "Is Brazilian",
    "eng" to "Is English",
    "de" to "Is German",
    "be" to "Is Belgian",
    "co" to "Is Colombian",
    "pt" to "Is Portuguese",
    "uy" to "Is Uruguayan",
    "ar" to "Is Argentinian",
    "fr" to "Is French",
    "es" to "Is Spanish",
    "wls" to "Is Welsh",
    "us" to "Is American"
)

/**
 * Retrieves a description associated with the given key.
 *
 * @param key The unique identifier for a team or attribute.
 * @return A human-readable description for the given key.
 *         If the key is not found, "Is unknown" is returned by default.
 */
fun getTeamDescription(key: String): String {
    return teamDescriptions[key] ?: "Is unknown"
}
