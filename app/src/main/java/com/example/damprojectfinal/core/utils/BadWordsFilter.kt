package com.example.damprojectfinal.core.utils

object BadWordsFilter {
    private val badWords = setOf(
        "merde", "putain", "connard", "salaud", "salope", "enculé", "enculer",
        "connasse", "con", "conne", "bordel", "chier", "foutre", "bite", "couille",
        "pute", "batard", "bâtard", "niquer", "nique", "pd", "fdp", "ntm",
        // English
        "fuck", "shit", "bitch", "ass", "asshole", "bastard", "dick", "pussy"
    )

    fun moderate(text: String): String {
        var moderatedText = text
        // Use a regex to find words (case insensitive) and check if they are in our bad words list
        for (badWord in badWords) {
            val regex = Regex("\\b$badWord\\b", RegexOption.IGNORE_CASE)
            if (regex.containsMatchIn(moderatedText)) {
                moderatedText = regex.replace(moderatedText) { matchResult ->
                    "*".repeat(matchResult.value.length)
                }
            }
        }
        return moderatedText
    }
}
