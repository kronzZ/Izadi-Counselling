package com.practice.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.practice.app.data.PaymentStatus
import com.practice.app.data.Session
import com.practice.app.data.collectedPaymentsCents
import com.practice.app.data.estimatedUpcomingRevenueDollars
import com.practice.app.data.paidPayments
import com.practice.app.data.upcomingPayments
import com.practice.app.ui.theme.IzadiBloom
import com.practice.app.ui.theme.IzadiFoam
import com.practice.app.ui.theme.IzadiInk
import com.practice.app.ui.theme.IzadiInkSoft
import com.practice.app.ui.theme.IzadiSage
import com.practice.app.ui.theme.IzadiSageSoft
import com.practice.app.ui.theme.SoftScreenBackground
import java.util.Locale

private enum class PaymentTab(val label: String) {
    Upcoming("Upcoming"),
    Paid("Paid"),
}

@Composable
fun PaymentsScreen(
    sessions: List<Session>,
    onBack: () -> Unit,
    onOpenSession: (Session) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = PaymentTab.entries
    val snapshot = sessions.toList()
    val upcoming = remember(snapshot) { upcomingPayments(snapshot) }
    val paid = remember(snapshot) { paidPayments(snapshot) }
    val visiblePayments = when (tabs[selectedTab]) {
        PaymentTab.Upcoming -> upcoming
        PaymentTab.Paid -> paid
    }
    val upcomingRevenue = remember(upcoming) {
        estimatedUpcomingRevenueDollars(upcoming.size)
    }
    val collectedDollars = remember(paid) {
        collectedPaymentsCents(paid) / 100
    }

    SoftScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
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

            Text(
                text = "Payments",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (tabs[selectedTab]) {
                    PaymentTab.Upcoming -> {
                        val amount = "%,d".format(Locale.US, upcomingRevenue)
                        "Approximately $$amount of future booking revenue"
                    }
                    PaymentTab.Paid -> {
                        val amount = "%,d".format(Locale.US, collectedDollars)
                        "Payments for completed sessions total $$amount"
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                color = IzadiInkSoft,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(18.dp))

            PaymentTabs(
                tabs = tabs,
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it },
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (visiblePayments.isEmpty()) {
                Text(
                    text = when (tabs[selectedTab]) {
                        PaymentTab.Upcoming -> "No upcoming payments."
                        PaymentTab.Paid -> "No payments yet."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = IzadiInkSoft,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .weight(1f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(
                        items = visiblePayments,
                        key = { it.id },
                    ) { session ->
                        PaymentCard(
                            session = session,
                            showAmount = tabs[selectedTab] != PaymentTab.Upcoming,
                            onClick = { onOpenSession(session) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentTabs(
    tabs: List<PaymentTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(IzadiBloom.copy(alpha = 0.65f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            Text(
                text = tab.label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) IzadiInk else IzadiInkSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) IzadiFoam else IzadiBloom.copy(alpha = 0f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = IzadiSageSoft.copy(alpha = 0.3f)),
                        onClick = { onSelect(index) },
                    )
                    .padding(vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun PaymentCard(
    session: Session,
    showAmount: Boolean,
    onClick: () -> Unit,
) {
    val statusColor = when (session.paymentStatus) {
        PaymentStatus.Paid -> IzadiSage
        PaymentStatus.Pending -> IzadiInkSoft
    }
    val trailingLabel = when {
        showAmount && session.formattedPaidSummary != null -> session.formattedPaidSummary
        showAmount -> "—"
        else -> "Pending"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(IzadiFoam.copy(alpha = 0.95f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = IzadiSageSoft.copy(alpha = 0.35f)),
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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
            text = trailingLabel.orEmpty(),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = statusColor,
            modifier = Modifier.padding(horizontal = 10.dp),
        )

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
            contentDescription = null,
            tint = IzadiSageSoft,
        )
    }
}
