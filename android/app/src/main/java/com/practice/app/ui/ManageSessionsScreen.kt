package com.practice.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private enum class SessionsTab(val label: String) {
    Upcoming("Upcoming"),
    Past("Past"),
}

@Composable
fun ManageSessionsScreen(
    sessions: List<Session>,
    onBack: () -> Unit,
    onNewBooking: () -> Unit,
    onOpenSession: (Session) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = SessionsTab.entries
    val snapshot = sessions.toList()
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = LocalDateTime.now()
        }
    }
    val visibleSessions = when (tabs[selectedTab]) {
        SessionsTab.Upcoming -> manageUpcomingSessions(snapshot)
        SessionsTab.Past -> managePastSessions(snapshot)
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
                text = "Manage sessions",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(18.dp))

            SessionTabs(
                tabs = tabs,
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it },
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (visibleSessions.isEmpty()) {
                Text(
                    text = when (tabs[selectedTab]) {
                        SessionsTab.Upcoming -> "No upcoming sessions."
                        SessionsTab.Past -> "No past sessions yet."
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
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(
                        items = visibleSessions,
                        key = { it.id },
                    ) { session ->
                        SessionListCard(
                            session = session,
                            awaitingWrapUp = tabs[selectedTab] == SessionsTab.Upcoming &&
                                isSessionAwaitingWrapUp(session, now),
                            onClick = { onOpenSession(session) },
                        )
                    }
                }
            }

            SoftActionButton(
                label = "New booking",
                onClick = onNewBooking,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp, bottom = 20.dp),
            )
        }
    }
}

@Composable
private fun SessionTabs(
    tabs: List<SessionsTab>,
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
private fun SessionListCard(
    session: Session,
    awaitingWrapUp: Boolean,
    onClick: () -> Unit,
) {
    val cardColor = if (awaitingWrapUp) IzadiButter else IzadiFoam.copy(alpha = 0.95f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardColor)
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
fun SoftActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = IzadiBloom,
    contentColor: Color = IzadiSage,
    rippleColor: Color = IzadiSageSoft.copy(alpha = 0.35f),
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = rippleColor),
                onClick = onClick,
            )
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}
