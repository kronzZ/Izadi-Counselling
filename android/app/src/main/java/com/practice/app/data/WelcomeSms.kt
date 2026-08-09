package com.practice.app.data

/** Placeholder replaced with the recipient’s first name when opening SMS. */
const val WelcomeSmsFirstNameToken = "{firstName}"

/**
 * Default welcome / pricing SMS template.
 * Keep [WelcomeSmsFirstNameToken] where their name should appear.
 */
fun defaultWelcomeSmsTemplate(): String = """
Hi $WelcomeSmsFirstNameToken,

Welcome to Izadi Counselling — thank you for getting in touch. I’m looking forward to supporting you.

Sessions are typically 50 minutes.
Standard fee: $150 per session.
Payment can be arranged at the end of each session.

If you have any questions before we meet, feel free to reply to this message.

Warm regards,
Izadi Counselling
""".trimIndent()

fun renderWelcomeSms(template: String, firstName: String): String {
    val name = firstName.trim().ifEmpty { "there" }
    return template.replace(WelcomeSmsFirstNameToken, name)
}
