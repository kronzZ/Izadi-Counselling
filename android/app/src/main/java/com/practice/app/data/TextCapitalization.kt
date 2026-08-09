package com.practice.app.data

/**
 * Forces the first letter in [text] to uppercase (leaves digits/symbols before it alone).
 * Used for names, notes, and other prose fields — not phones or dates.
 */
fun capitalizeFirstLetter(text: String): String {
    val index = text.indexOfFirst { it.isLetter() }
    if (index < 0) return text
    val letter = text[index]
    if (!letter.isLowerCase()) return text
    return text.replaceRange(index, index + 1, letter.titlecase())
}
