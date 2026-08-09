package com.practice.app.data

import java.time.LocalDate

/**
 * Quiet lines for the home screen — one per calendar day, in this order, then repeat.
 */
private val QuietDailyQuotes = listOf(
    "A calm space, one session at a time.",
    "Small, steady care still counts.",
    "Your clients don’t need perfect — they need you there.",
    "One honest hour can change a week.",
    "Breathe before you begin.",
    "Your steadiness is a kindness.",
    "The day can be soft and still be full.",
    "Tend the practice. Tend yourself.",
    "Good work doesn’t always look loud.",
    "Listen fully. Rest honestly.",
)

/** Stable for the calendar day — same quote all day; advances one step tomorrow. */
fun dailyQuote(today: LocalDate = LocalDate.now()): String {
    val index = today.toEpochDay().mod(QuietDailyQuotes.size)
    return QuietDailyQuotes[index]
}
