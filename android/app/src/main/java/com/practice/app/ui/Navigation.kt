package com.practice.app.ui

object Destinations {
    const val Home = "home"
    const val Clients = "clients"
    const val ClientDetail = "clients/{clientId}"
    const val ClientSessions = "clients/{clientId}/sessions"
    const val Sessions = "sessions"
    const val SessionDetail = "sessions/detail/{sessionId}"
    const val NewBookingChoice = "sessions/new"
    const val BookingPickClient = "sessions/new/existing"
    const val NewClient = "clients/new"
    const val BookSession = "sessions/book/{clientId}"
    const val Payments = "payments"

    fun clientDetail(clientId: String) = "clients/$clientId"
    fun clientSessions(clientId: String) = "clients/$clientId/sessions"
    fun bookSession(clientId: String) = "sessions/book/$clientId"
    fun sessionDetail(sessionId: String) = "sessions/detail/$sessionId"
}
