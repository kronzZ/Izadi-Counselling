package com.practice.app.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID

enum class SessionStatus(val label: String) {
    Scheduled("Scheduled"),
    Completed("Completed"),
    Cancelled("Cancelled"),
}

enum class PaymentStatus(val label: String) {
    /** Booked, session not completed yet. */
    Pending("Pending"),
    /** Session completed and paid (cash or tap). */
    Paid("Paid"),
}

enum class PaymentMethod(val label: String) {
    Cash("Cash"),
    Tapped("Tapped"),
}

data class Session(
    val id: String = UUID.randomUUID().toString(),
    val clientId: String,
    val clientName: String,
    val date: LocalDate,
    val time: LocalTime,
    val durationMinutes: Int,
    val notes: String = "",
    val status: SessionStatus = SessionStatus.Scheduled,
    val paymentStatus: PaymentStatus = PaymentStatus.Pending,
    /** Amount paid, in cents. */
    val paymentAmountCents: Int? = null,
    /** How the client paid — set when marked complete. */
    val paymentMethod: PaymentMethod? = null,
) {
    val startsAt: LocalDateTime
        get() = LocalDateTime.of(date, time)

    val endsAt: LocalDateTime
        get() = startsAt.plusMinutes(durationMinutes.toLong())

    val formattedPaymentAmount: String?
        get() = paymentAmountCents?.let { formatAud(it) }

    /** e.g. "Cash - $150.00" for paid payment cards. */
    val formattedPaidSummary: String?
        get() {
            val amount = formattedPaymentAmount ?: return null
            val method = paymentMethod?.label ?: return amount
            return "$method - $amount"
        }

    val friendlyDate: String
        get() {
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            val day = "${date.dayOfMonth}${ordinalSuffix(date.dayOfMonth)}"
            val month = date.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            return "$dayName, $day $month ${date.year}"
        }

    val friendlyTime: String
        get() = time.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH))

    val shortDate: String
        get() {
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            val month = date.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            return "$dayName ${date.dayOfMonth} $month"
        }
}

/**
 * Scheduled sessions still to finish — kept after the time slot so wrap-up
 * (payment / complete) can happen after the appointment ends.
 */
fun upcomingSessions(
    sessions: List<Session>,
    limit: Int = 5,
): List<Session> {
    return sessions
        .filter { it.status == SessionStatus.Scheduled }
        .sortedBy { it.startsAt }
        .take(limit)
}

/** Scheduled session whose end time (start + duration) has already passed. */
fun isSessionAwaitingWrapUp(
    session: Session,
    now: LocalDateTime = LocalDateTime.now(),
): Boolean {
    return session.status == SessionStatus.Scheduled && session.endsAt.isBefore(now)
}

fun isPastSession(session: Session): Boolean {
    return session.status == SessionStatus.Completed ||
        session.status == SessionStatus.Cancelled
}

fun manageUpcomingSessions(sessions: List<Session>): List<Session> {
    return sessions
        .filter { !isPastSession(it) }
        .sortedBy { it.startsAt }
}

fun managePastSessions(sessions: List<Session>): List<Session> {
    return sessions
        .filter { isPastSession(it) }
        .sortedByDescending { it.startsAt }
}

/** Placeholder average fee used for upcoming revenue estimates. */
const val EstimatedSessionFeeDollars = 130

/** Pending payments — session booked, not yet completed. */
fun upcomingPayments(sessions: List<Session>): List<Session> {
    return sessions
        .filter { it.status != SessionStatus.Cancelled }
        .filter { it.paymentStatus == PaymentStatus.Pending }
        .sortedBy { it.startsAt }
}

fun paidPayments(sessions: List<Session>): List<Session> {
    return sessions
        .filter { it.status != SessionStatus.Cancelled }
        .filter { it.paymentStatus == PaymentStatus.Paid }
        .sortedByDescending { it.startsAt }
}

fun estimatedUpcomingRevenueDollars(upcomingCount: Int): Int {
    return upcomingCount * EstimatedSessionFeeDollars
}

fun collectedPaymentsCents(paidSessions: List<Session>): Int {
    return paidSessions.sumOf { it.paymentAmountCents ?: 0 }
}

fun ordinalSuffix(day: Int): String {
    return when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
}

fun formatAud(cents: Int): String {
    val absolute = kotlin.math.abs(cents)
    val dollars = absolute / 100
    val remainder = absolute % 100
    val sign = if (cents < 0) "-" else ""
    return "%s$%d.%02d".format(Locale.US, sign, dollars, remainder)
}

/** Parses a dollars string like "120", "120.5", or "120.50" into cents. */
fun parseDollarsToCents(raw: String): Int? {
    val cleaned = raw.trim()
        .removePrefix("$")
        .replace(",", "")
        .trim()
    if (cleaned.isEmpty()) return null
    val match = Regex("""^(\d+)(?:\.(\d{0,2}))?$""").matchEntire(cleaned) ?: return null
    val dollars = match.groupValues[1].toIntOrNull() ?: return null
    val fraction = match.groupValues[2]
    val cents = when (fraction.length) {
        0 -> 0
        1 -> fraction.toInt() * 10
        else -> fraction.toInt()
    }
    return dollars * 100 + cents
}
