package com.practice.app.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.practice.app.R
import com.practice.app.data.Session
import com.practice.app.data.WelcomeSmsFirstNameToken
import com.practice.app.data.capitalizeFirstLetter
import com.practice.app.data.dailyQuote
import com.practice.app.data.isSessionAwaitingWrapUp
import com.practice.app.data.renderWelcomeSms
import com.practice.app.data.upcomingSessions
import com.practice.app.ui.theme.IzadiBloom
import com.practice.app.ui.theme.IzadiButter
import com.practice.app.ui.theme.IzadiFoam
import com.practice.app.ui.theme.IzadiInk
import com.practice.app.ui.theme.IzadiInkSoft
import com.practice.app.ui.theme.IzadiRoseDeep
import com.practice.app.ui.theme.IzadiSage
import com.practice.app.ui.theme.IzadiSageSoft
import com.practice.app.ui.theme.SoftScreenBackground
import java.time.LocalDateTime
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    sessions: List<Session>,
    welcomeSmsTemplate: String,
    onWelcomeSmsTemplateChange: (String) -> Unit,
    onOpenClients: () -> Unit,
    onOpenSessions: () -> Unit,
    onOpenPayments: () -> Unit,
    onOpenSession: (Session) -> Unit,
) {
    val context = LocalContext.current
    var showHero by remember { mutableStateOf(false) }
    var showNav by remember { mutableStateOf(false) }
    var showWelcomeSmsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showHero = true
        delay(90)
        showNav = true
    }

    val heroAlpha by animateFloatAsState(
        targetValue = if (showHero) 1f else 0f,
        animationSpec = tween(600),
        label = "heroAlpha",
    )
    val heroOffset by animateFloatAsState(
        targetValue = if (showHero) 0f else 14f,
        animationSpec = tween(650),
        label = "heroOffset",
    )

    SoftScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .elasticBounce()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp, bottom = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(heroAlpha)
                    .offset(y = heroOffset.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Izadi",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 52.sp,
                                lineHeight = 56.sp,
                            ),
                        )
                        Text(
                            text = "Counselling",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = IzadiSage,
                                letterSpacing = 0.4.sp,
                            ),
                        )
                    }

                    Image(
                        painter = painterResource(R.drawable.izadi_logo),
                        contentDescription = "Izadi Counselling",
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(96.dp)
                            .offset(y = (-4).dp),
                        contentScale = ContentScale.Fit,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = remember { dailyQuote() },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontStyle = FontStyle.Italic,
                    ),
                    modifier = Modifier.fillMaxWidth(0.85f),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .alpha(heroAlpha),
            ) {
                Text(
                    text = "Your next 5 sessions",
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(modifier = Modifier.height(10.dp))

                UpcomingSessionsPanel(
                    sessions = sessions,
                    onOpenSession = onOpenSession,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HomeNavButton(
                    visible = showNav,
                    delayMillis = 0,
                    title = "Clients",
                    subtitle = "People you see",
                    icon = Icons.Outlined.PeopleOutline,
                    onClick = onOpenClients,
                )
                HomeNavButton(
                    visible = showNav,
                    delayMillis = 80,
                    title = "Manage sessions",
                    subtitle = "Make, delete or edit bookings",
                    icon = Icons.Outlined.CalendarMonth,
                    onClick = onOpenSessions,
                )
                HomeNavButton(
                    visible = showNav,
                    delayMillis = 160,
                    title = "Payments",
                    subtitle = "Past and future payments",
                    icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                    onClick = onOpenPayments,
                )
                HomeNavButton(
                    visible = showNav,
                    delayMillis = 240,
                    title = "Send welcome SMS",
                    subtitle = "Welcome note and pricing guide",
                    icon = Icons.Outlined.Sms,
                    onClick = { showWelcomeSmsDialog = true },
                )
            }
        }
    }

    if (showWelcomeSmsDialog) {
        WelcomeSmsDialog(
            template = welcomeSmsTemplate,
            onDismiss = { showWelcomeSmsDialog = false },
            onTemplateChange = onWelcomeSmsTemplateChange,
            onSend = { firstName, mobile ->
                showWelcomeSmsDialog = false
                openWelcomeSms(context, welcomeSmsTemplate, firstName, mobile)
            },
        )
    }
}

@Composable
private fun WelcomeSmsDialog(
    template: String,
    onDismiss: () -> Unit,
    onTemplateChange: (String) -> Unit,
    onSend: (firstName: String, mobile: String) -> Unit,
) {
    var firstName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var showTemplateEditor by remember { mutableStateOf(false) }
    val canSend = firstName.isNotBlank() && mobile.filter { it.isDigit() }.length >= 8

    if (showTemplateEditor) {
        WelcomeSmsTemplateDialog(
            initialTemplate = template,
            onDismiss = { showTemplateEditor = false },
            onSave = { updated ->
                onTemplateChange(updated)
                showTemplateEditor = false
            },
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IzadiFoam,
        title = {
            Text(
                text = "Send welcome SMS",
                style = MaterialTheme.typography.titleMedium,
                color = IzadiInk,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Enter their details and we’ll open your SMS app with the welcome message ready to send.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = IzadiInkSoft,
                )
                OutlinedTextField(
                    value = firstName,
                    onValueChange = {
                        firstName = capitalizeFirstLetter(it)
                        showError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("First name") },
                    textStyle = MaterialTheme.typography.titleMedium.copy(color = IzadiInk),
                    shape = RoundedCornerShape(18.dp),
                    colors = softFieldColors(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                    ),
                )
                OutlinedTextField(
                    value = mobile,
                    onValueChange = {
                        mobile = it
                        showError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Mobile") },
                    textStyle = MaterialTheme.typography.titleMedium.copy(color = IzadiInk),
                    shape = RoundedCornerShape(18.dp),
                    colors = softFieldColors(),
                    isError = showError,
                    supportingText = if (showError) {
                        { Text("Enter a valid mobile number", color = IzadiRoseDeep) }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )
                Spacer(modifier = Modifier.height(4.dp))
                SoftActionButton(
                    label = "Open SMS",
                    onClick = {
                        if (!canSend) {
                            showError = true
                            return@SoftActionButton
                        }
                        onSend(firstName.trim(), mobile.trim())
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                SoftActionButton(
                    label = "Edit template",
                    onClick = { showTemplateEditor = true },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = IzadiInkSoft)
            }
        },
    )
}

@Composable
private fun WelcomeSmsTemplateDialog(
    initialTemplate: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var draft by remember(initialTemplate) { mutableStateOf(initialTemplate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IzadiFoam,
        title = {
            Text(
                text = "Edit welcome template",
                style = MaterialTheme.typography.titleMedium,
                color = IzadiInk,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Use $WelcomeSmsFirstNameToken where their first name should appear.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = IzadiInkSoft,
                )
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = IzadiInk),
                    shape = RoundedCornerShape(18.dp),
                    colors = softFieldColors(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                )
                SoftActionButton(
                    label = "Save template",
                    onClick = { onSave(draft) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = IzadiInkSoft)
            }
        },
    )
}

private fun openWelcomeSms(
    context: Context,
    template: String,
    firstName: String,
    mobile: String,
) {
    val digits = buildString {
        mobile.forEach { ch ->
            if (ch.isDigit() || (ch == '+' && isEmpty())) append(ch)
        }
    }
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("smsto:$digits")
        putExtra("sms_body", renderWelcomeSms(template, firstName))
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No SMS app found on this device.", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun UpcomingSessionsPanel(
    sessions: List<Session>,
    onOpenSession: (Session) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Read list contents during composition so SnapshotStateList updates recompose this.
    val upcoming = upcomingSessions(sessions.toList())
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = LocalDateTime.now()
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(IzadiFoam.copy(alpha = 0.55f))
            .padding(8.dp),
    ) {
        if (upcoming.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "No upcoming sessions",
                    style = MaterialTheme.typography.titleMedium,
                    color = IzadiSage,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your next five bookings will show here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = IzadiInkSoft,
                )
            }
        } else {
            // Equal-height cards fill the panel so leftover space isn't empty foam.
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                upcoming.forEach { session ->
                    UpcomingSessionCard(
                        session = session,
                        awaitingWrapUp = isSessionAwaitingWrapUp(session, now),
                        onClick = { onOpenSession(session) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingSessionCard(
    session: Session,
    awaitingWrapUp: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardColor = if (awaitingWrapUp) IzadiButter else IzadiFoam.copy(alpha = 0.95f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = IzadiSageSoft.copy(alpha = 0.35f)),
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.clientName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = IzadiInk,
                    fontSize = 16.sp,
                ),
            )
            Text(
                text = "${session.shortDate} · ${session.friendlyTime}",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = IzadiInkSoft,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = "${session.durationMinutes} min",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = IzadiSage,
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}

@Composable
private fun HomeNavButton(
    visible: Boolean,
    delayMillis: Int,
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500, delayMillis = delayMillis),
        label = "navAlpha-$title",
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 12f,
        animationSpec = tween(550, delayMillis = delayMillis),
        label = "navOffset-$title",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .offset(y = offsetY.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(IzadiFoam.copy(alpha = 0.92f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = IzadiSageSoft.copy(alpha = 0.35f)),
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(IzadiBloom),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = IzadiSage,
                modifier = Modifier.size(22.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = IzadiInkSoft,
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
            contentDescription = null,
            tint = IzadiSageSoft,
            modifier = Modifier.size(18.dp),
        )
    }
}
