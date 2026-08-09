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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.practice.app.data.Client
import com.practice.app.data.Session
import com.practice.app.data.isSessionAwaitingWrapUp
import com.practice.app.data.managePastSessions
import com.practice.app.data.manageUpcomingSessions
import com.practice.app.ui.theme.IzadiBloom
import com.practice.app.ui.theme.IzadiButter
import com.practice.app.ui.theme.IzadiFoam
import com.practice.app.ui.theme.IzadiInk
import com.practice.app.ui.theme.IzadiInkSoft
import com.practice.app.ui.theme.IzadiSage
import com.practice.app.ui.theme.IzadiSageSoft
import com.practice.app.ui.theme.SoftScreenBackground
import java.time.LocalDateTime
import kotlinx.coroutines.delay

private enum class ClientSessionsTab(val label: String) {
    Upcoming("Upcoming"),
    Past("Past"),
}

@Composable
fun ClientSessionsScreen(
    client: Client,
    sessions: List<Session>,
    onBack: () -> Unit,
    onOpenSession: (Session) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = ClientSessionsTab.entries
    val clientSessions = sessions.filter { it.clientId == client.id }
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = LocalDateTime.now()
        }
    }
    val visibleSessions = when (tabs[selectedTab]) {
        ClientSessionsTab.Upcoming -> manageUpcomingSessions(clientSessions)
        ClientSessionsTab.Past -> managePastSessions(clientSessions)
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
                text = "Sessions",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = client.fullName,
                style = MaterialTheme.typography.bodyLarge,
                color = IzadiInkSoft,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(18.dp))

            ClientSessionsTabs(
                tabs = tabs,
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it },
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (visibleSessions.isEmpty()) {
                Text(
                    text = when (tabs[selectedTab]) {
                        ClientSessionsTab.Upcoming -> "No upcoming sessions for this client."
                        ClientSessionsTab.Past -> "No past sessions for this client."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = IzadiInkSoft,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(
                        items = visibleSessions,
                        key = { it.id },
                    ) { session ->
                        ClientSessionCard(
                            session = session,
                            awaitingWrapUp = tabs[selectedTab] == ClientSessionsTab.Upcoming &&
                                isSessionAwaitingWrapUp(session, now),
                            onClick = { onOpenSession(session) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientSessionsTabs(
    tabs: List<ClientSessionsTab>,
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
private fun ClientSessionCard(
    session: Session,
    awaitingWrapUp: Boolean,
    onClick: () -> Unit,
) {
    val cardColor = if (awaitingWrapUp) IzadiButter else IzadiFoam.copy(alpha = 0.92f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(cardColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = IzadiSageSoft.copy(alpha = 0.35f)),
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${session.friendlyDate} · ${session.friendlyTime}",
                style = MaterialTheme.typography.titleMedium,
                color = IzadiInk,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${session.status.label} · ${session.durationMinutes} min",
                style = MaterialTheme.typography.bodyMedium,
                color = IzadiSage,
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
            contentDescription = null,
            tint = IzadiSageSoft,
        )
    }
}
