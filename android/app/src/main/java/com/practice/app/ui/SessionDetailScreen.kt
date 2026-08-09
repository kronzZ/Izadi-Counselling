package com.practice.app.ui

import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.practice.app.data.PaymentMethod
import com.practice.app.data.PaymentStatus
import com.practice.app.data.Session
import com.practice.app.data.SessionStatus
import com.practice.app.data.capitalizeFirstLetter
import com.practice.app.data.parseDollarsToCents
import com.practice.app.payments.SquarePosPayments
import com.practice.app.ui.theme.IzadiFoam
import com.practice.app.ui.theme.IzadiInk
import com.practice.app.ui.theme.IzadiInkSoft
import com.practice.app.ui.theme.IzadiRose
import com.practice.app.ui.theme.IzadiRoseDeep
import com.practice.app.ui.theme.IzadiSage
import com.practice.app.ui.theme.IzadiSageSoft
import com.practice.app.ui.theme.SoftScreenBackground

private enum class CompletePaymentChoice {
    Cash,
    TapNow,
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionDetailScreen(
    session: Session,
    onBack: () -> Unit,
    onSessionChange: (Session) -> Unit,
) {
    val context = LocalContext.current
    val availableDates = remember(session.date) {
        val rolling = rollingDates()
        if (session.date in rolling) rolling else listOf(session.date) + rolling
    }

    val clock = remember(session.id, session.time) { clockPartsFrom(session.time) }
    var hour by remember(session.id) { mutableIntStateOf(clock.hour12) }
    var minute by remember(session.id) { mutableIntStateOf(clock.minute) }
    var isPm by remember(session.id) { mutableStateOf(clock.isPm) }
    var showPaymentPrompt by remember { mutableStateOf(false) }
    var pendingChoice by remember { mutableStateOf<CompletePaymentChoice?>(null) }
    var pendingTapAmountCents by remember { mutableStateOf<Int?>(null) }
    var amountInput by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }

    val canActOn = session.status == SessionStatus.Scheduled
    val showAmountPrompt = pendingChoice != null
    val scrollState = rememberScrollState()
    val notesBringIntoView = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    val squareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val amountCents = pendingTapAmountCents
        pendingTapAmountCents = null
        val data = result.data
        if (amountCents == null) return@rememberLauncherForActivityResult

        if (data == null) {
            Toast.makeText(context, "Square didn’t return a result. Try again.", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        if (SquarePosPayments.isSuccessResult(result.resultCode)) {
            onSessionChange(
                session.copy(
                    status = SessionStatus.Completed,
                    paymentStatus = PaymentStatus.Paid,
                    paymentAmountCents = amountCents,
                    paymentMethod = PaymentMethod.Tapped,
                ),
            )
            Toast.makeText(context, "Payment received in Square.", Toast.LENGTH_SHORT).show()
        } else {
            val error = runCatching { SquarePosPayments.parseError(context, data) }.getOrNull()
            val message = error?.debugDescription?.takeIf { it.isNotBlank() }
                ?: "Payment cancelled in Square."
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    SoftScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = IzadiSage,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Session",
                    style = MaterialTheme.typography.titleLarge,
                )

                Text(
                    text = session.status.label.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = IzadiSage,
                )

                Text(
                    text = "CLIENT",
                    style = MaterialTheme.typography.labelLarge,
                    color = IzadiSageSoft,
                )
                Text(
                    text = session.clientName,
                    style = MaterialTheme.typography.titleMedium,
                    color = IzadiInk,
                )

                Spacer(modifier = Modifier.height(4.dp))

                DateDropdownField(
                    selectedDate = session.date,
                    dates = availableDates,
                    onDateSelected = { date ->
                        onSessionChange(session.copy(date = date))
                    },
                )

                SimpleTimePicker(
                    hour = hour,
                    minute = minute,
                    isPm = isPm,
                    onHourChange = {
                        hour = it
                        onSessionChange(session.copy(time = toLocalTime(it, minute, isPm)))
                    },
                    onMinuteChange = {
                        minute = it
                        onSessionChange(session.copy(time = toLocalTime(hour, it, isPm)))
                    },
                    onIsPmChange = {
                        isPm = it
                        onSessionChange(session.copy(time = toLocalTime(hour, minute, it)))
                    },
                )

                DurationDropdownField(
                    durationMinutes = session.durationMinutes,
                    onDurationSelected = {
                        onSessionChange(session.copy(durationMinutes = it))
                    },
                )

                if (session.paymentStatus == PaymentStatus.Paid &&
                    session.formattedPaidSummary != null
                ) {
                    Text(
                        text = "PAYMENT",
                        style = MaterialTheme.typography.labelLarge,
                        color = IzadiSageSoft,
                    )
                    Text(
                        text = session.formattedPaidSummary.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        color = IzadiInk,
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(notesBringIntoView),
                ) {
                    Text(
                        text = "NOTES / COMMENTS",
                        style = MaterialTheme.typography.labelLarge,
                        color = IzadiSageSoft,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = session.notes,
                        onValueChange = {
                            onSessionChange(session.copy(notes = capitalizeFirstLetter(it)))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .onFocusEvent { focusState ->
                                if (focusState.isFocused) {
                                    scope.launch {
                                        // Wait for the keyboard inset, then keep the whole notes block in view.
                                        delay(280)
                                        notesBringIntoView.bringIntoView()
                                    }
                                }
                            },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = IzadiInk),
                        placeholder = {
                            Text(
                                text = "Add session notes…",
                                color = IzadiInkSoft.copy(alpha = 0.55f),
                            )
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = schedulingFieldColors(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                        ),
                    )
                }

                if (canActOn) {
                    Spacer(modifier = Modifier.height(8.dp))

                    SoftActionButton(
                        label = "Mark as complete",
                        onClick = { showPaymentPrompt = true },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SoftActionButton(
                        label = "Cancel session",
                        onClick = {
                            onSessionChange(session.copy(status = SessionStatus.Cancelled))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = IzadiRose,
                        contentColor = IzadiRoseDeep,
                        rippleColor = IzadiRoseDeep.copy(alpha = 0.25f),
                    )
                } else if (session.status == SessionStatus.Cancelled ||
                    session.status == SessionStatus.Completed
                ) {
                    SoftActionButton(
                        label = "Restore to scheduled",
                        onClick = {
                            onSessionChange(
                                session.copy(
                                    status = SessionStatus.Scheduled,
                                    paymentStatus = PaymentStatus.Pending,
                                    paymentAmountCents = null,
                                    paymentMethod = null,
                                ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }

    if (showPaymentPrompt) {
        AlertDialog(
            onDismissRequest = { showPaymentPrompt = false },
            containerColor = IzadiFoam,
            title = {
                Text(
                    text = "Complete session",
                    style = MaterialTheme.typography.titleMedium,
                    color = IzadiInk,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "How is ${session.clientName} paying for this session?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = IzadiInkSoft,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SoftActionButton(
                        label = "Cash",
                        onClick = {
                            showPaymentPrompt = false
                            amountInput = ""
                            amountError = false
                            pendingChoice = CompletePaymentChoice.Cash
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SoftActionButton(
                        label = "Tap now",
                        onClick = {
                            showPaymentPrompt = false
                            amountInput = ""
                            amountError = false
                            pendingChoice = CompletePaymentChoice.TapNow
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPaymentPrompt = false }) {
                    Text("Cancel", color = IzadiInkSoft)
                }
            },
        )
    }

    if (showAmountPrompt) {
        val choice = pendingChoice
        val helper = when (choice) {
            CompletePaymentChoice.Cash -> "Enter the amount paid in cash."
            CompletePaymentChoice.TapNow -> "Enter the amount, then Square will open for tap to pay."
            null -> ""
        }

        AlertDialog(
            onDismissRequest = {
                pendingChoice = null
                amountInput = ""
                amountError = false
            },
            containerColor = IzadiFoam,
            title = {
                Text(
                    text = "Amount",
                    style = MaterialTheme.typography.titleMedium,
                    color = IzadiInk,
                )
            },
            text = {
                Column {
                    Text(
                        text = helper,
                        style = MaterialTheme.typography.bodyLarge,
                        color = IzadiInkSoft,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = {
                            amountInput = it
                            amountError = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        prefix = { Text("$", color = IzadiInkSoft) },
                        placeholder = {
                            Text("0.00", color = IzadiInkSoft.copy(alpha = 0.55f))
                        },
                        isError = amountError,
                        supportingText = if (amountError) {
                            { Text("Enter a valid amount", color = IzadiRoseDeep) }
                        } else {
                            null
                        },
                        textStyle = MaterialTheme.typography.titleMedium.copy(color = IzadiInk),
                        shape = RoundedCornerShape(18.dp),
                        colors = schedulingFieldColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cents = parseDollarsToCents(amountInput)
                        if (cents == null || cents <= 0) {
                            amountError = true
                            return@TextButton
                        }
                        when (choice) {
                            CompletePaymentChoice.Cash -> {
                                pendingChoice = null
                                amountInput = ""
                                amountError = false
                                onSessionChange(
                                    session.copy(
                                        status = SessionStatus.Completed,
                                        paymentStatus = PaymentStatus.Paid,
                                        paymentAmountCents = cents,
                                        paymentMethod = PaymentMethod.Cash,
                                    ),
                                )
                            }
                            CompletePaymentChoice.TapNow -> {
                                if (!SquarePosPayments.isConfigured()) {
                                    Toast.makeText(
                                        context,
                                        "Add your Square Application ID in SquareConfig.kt first.",
                                        Toast.LENGTH_LONG,
                                    ).show()
                                    return@TextButton
                                }
                                try {
                                    val intent = SquarePosPayments.createChargeIntent(
                                        context = context,
                                        amountCents = cents,
                                        note = session.clientName,
                                        requestMetadata = session.id,
                                    )
                                    pendingChoice = null
                                    amountInput = ""
                                    amountError = false
                                    pendingTapAmountCents = cents
                                    squareLauncher.launch(intent)
                                } catch (_: ActivityNotFoundException) {
                                    Toast.makeText(
                                        context,
                                        "Install Square Point of Sale to take tap payments.",
                                        Toast.LENGTH_LONG,
                                    ).show()
                                    SquarePosPayments.openPlayStoreListing(context)
                                }
                            }
                            null -> Unit
                        }
                    },
                ) {
                    Text(
                        text = if (choice == CompletePaymentChoice.TapNow) "Open Square" else "Complete",
                        color = IzadiSage,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingChoice = null
                        amountInput = ""
                        amountError = false
                    },
                ) {
                    Text("Cancel", color = IzadiInkSoft)
                }
            },
        )
    }
}
