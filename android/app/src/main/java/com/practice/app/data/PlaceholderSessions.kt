package com.practice.app.data

import java.time.LocalDate
import java.time.LocalTime

/** Bump when adding/replacing seed sessions (merge/replace seed ids only). */
const val SessionSeedVersion = 5

/**
 * Demo bookings spread across placeholder clients — mix of upcoming, completed
 * (paid via Cash / Tapped), and a few cancelled. Dates are relative to [today]
 * so the mix stays meaningful over time.
 */
fun buildPlaceholderSessions(today: LocalDate = LocalDate.now()): List<Session> {
    val clients = PlaceholderClients.filter { it.isActive }
    if (clients.isEmpty()) return emptyList()

    val durations = listOf(50, 60, 90)
    // Varied demo fee amounts for paid sessions ($95–$220).
    val amounts = listOf(
        9_500, 11_000, 12_000, 13_500, 14_000, 15_000,
        16_500, 17_000, 18_000, 19_500, 20_000, 22_000,
    )
    val sessions = mutableListOf<Session>()
    var index = 0

    fun add(
        client: Client,
        daysOffset: Int,
        hour: Int,
        minute: Int,
        status: SessionStatus,
        payment: PaymentStatus,
        notes: String = "",
        amountCents: Int? = null,
        method: PaymentMethod? = null,
    ) {
        index++
        val resolvedAmount = when (payment) {
            PaymentStatus.Paid -> amountCents ?: amounts[index % amounts.size]
            PaymentStatus.Pending -> null
        }
        val resolvedMethod = when (payment) {
            PaymentStatus.Paid -> method ?: if (index % 2 == 0) PaymentMethod.Cash else PaymentMethod.Tapped
            PaymentStatus.Pending -> null
        }
        sessions += Session(
            id = "seed-session-${index.toString().padStart(3, '0')}",
            clientId = client.id,
            clientName = client.fullName,
            date = today.plusDays(daysOffset.toLong()),
            time = LocalTime.of(hour, minute),
            durationMinutes = durations[index % durations.size],
            notes = notes,
            status = status,
            paymentStatus = payment,
            paymentAmountCents = resolvedAmount,
            paymentMethod = resolvedMethod,
        )
    }

    clients.forEachIndexed { clientIndex, client ->
        val amount = amounts[clientIndex % amounts.size]
        val altAmount = amounts[(clientIndex + 3) % amounts.size]
        when (clientIndex % 6) {
            0 -> {
                add(
                    client, -18, 10, 0, SessionStatus.Completed, PaymentStatus.Paid,
                    "Follow-up next month.", amount, PaymentMethod.Cash,
                )
                add(client, 2, 14, 30, SessionStatus.Scheduled, PaymentStatus.Pending)
            }
            1 -> {
                add(
                    client, -9, 9, 0, SessionStatus.Completed, PaymentStatus.Paid,
                    amountCents = amount, method = PaymentMethod.Tapped,
                )
                add(client, 7, 11, 0, SessionStatus.Scheduled, PaymentStatus.Pending)
            }
            2 -> {
                add(
                    client, -25, 15, 30, SessionStatus.Completed, PaymentStatus.Paid,
                    amountCents = amount, method = PaymentMethod.Cash,
                )
                add(
                    client, -4, 16, 0, SessionStatus.Completed, PaymentStatus.Paid,
                    amountCents = altAmount, method = PaymentMethod.Tapped,
                )
                add(client, 12, 10, 0, SessionStatus.Scheduled, PaymentStatus.Pending)
            }
            3 -> {
                add(client, 1, 9, 30, SessionStatus.Scheduled, PaymentStatus.Pending)
                add(
                    client, -12, 13, 0, SessionStatus.Completed, PaymentStatus.Paid,
                    amountCents = amount, method = PaymentMethod.Cash,
                )
            }
            4 -> {
                add(client, -6, 17, 0, SessionStatus.Cancelled, PaymentStatus.Pending, "Client rescheduled.")
                add(client, 4, 10, 0, SessionStatus.Scheduled, PaymentStatus.Pending)
                add(
                    client, -15, 11, 30, SessionStatus.Completed, PaymentStatus.Paid,
                    amountCents = amount, method = PaymentMethod.Tapped,
                )
            }
            else -> {
                add(
                    client, -2, 14, 0, SessionStatus.Completed, PaymentStatus.Paid,
                    amountCents = amount, method = PaymentMethod.Cash,
                )
                add(client, 5, 15, 30, SessionStatus.Scheduled, PaymentStatus.Pending)
                add(
                    client, -20, 9, 0, SessionStatus.Completed, PaymentStatus.Paid,
                    amountCents = altAmount, method = PaymentMethod.Tapped,
                )
            }
        }
    }

    return sessions
}
