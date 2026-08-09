package com.practice.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.practice.app.data.Client
import com.practice.app.data.Session
import com.practice.app.data.SessionStatus
import com.practice.app.data.isPastSession
import com.practice.app.ui.theme.IzadiBloom
import com.practice.app.ui.theme.IzadiFoam
import com.practice.app.ui.theme.IzadiInk
import com.practice.app.ui.theme.IzadiInkSoft
import com.practice.app.ui.theme.IzadiSage
import com.practice.app.ui.theme.IzadiSageSoft
import com.practice.app.ui.theme.SoftScreenBackground
import kotlinx.coroutines.delay

private enum class ClientTab(val label: String) {
    Active("Active"),
    Inactive("Inactive"),
}

private enum class ClientSort(val label: String) {
    NewestFirst("Newest to oldest"),
    OldestFirst("Oldest to newest"),
    Alphabetical("A-Z"),
}

@Composable
fun ClientsScreen(
    clients: List<Client>,
    sessions: List<Session> = emptyList(),
    onBack: () -> Unit,
    onOpenClient: (Client) -> Unit,
    title: String = "Clients",
    onNewClient: (() -> Unit)? = null,
    highlightClientId: String? = null,
    onHighlightConsumed: () -> Unit = {},
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var sort by remember { mutableStateOf(ClientSort.NewestFirst) }
    var sortMenuOpen by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchFocus = remember { FocusRequester() }
    // Bumped on each sort pick so the list remounts at the top (keys otherwise keep scroll).
    var listResetKey by remember { mutableIntStateOf(0) }
    val tabs = ClientTab.entries
    val showActive = tabs[selectedTab] == ClientTab.Active
    val visibleClients = remember(clients, showActive, sort, searchQuery) {
        val query = searchQuery.trim().lowercase()
        val queryDigits = query.filter { it.isDigit() }
        val filtered = clients
            .filter { it.isActive == showActive }
            .filter { client ->
                if (query.isEmpty()) return@filter true
                val nameMatch = client.fullName.lowercase().contains(query)
                val mobileMatch = client.mobile.lowercase().contains(query) ||
                    (queryDigits.isNotEmpty() &&
                        client.mobile.filter { it.isDigit() }.contains(queryDigits))
                nameMatch || mobileMatch
            }
        when (sort) {
            ClientSort.NewestFirst -> filtered.sortedByDescending { it.createdAtEpochMs }
            ClientSort.OldestFirst -> filtered.sortedBy { it.createdAtEpochMs }
            ClientSort.Alphabetical -> filtered.sortedWith(
                compareBy({ it.firstName.lowercase() }, { it.surname.lowercase() }),
            )
        }
    }
    val sessionsByClient = remember(sessions) {
        sessions.groupBy { it.clientId }
    }

    LaunchedEffect(searchOpen) {
        if (searchOpen) {
            searchFocus.requestFocus()
        }
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularHeaderIcon(
                        icon = if (searchOpen) Icons.Outlined.Close else Icons.Outlined.Search,
                        contentDescription = if (searchOpen) "Close search" else "Search clients",
                        onClick = {
                            if (searchOpen) {
                                searchOpen = false
                                searchQuery = ""
                            } else {
                                searchOpen = true
                            }
                        },
                    )

                    Box {
                        CircularHeaderIcon(
                            icon = Icons.Outlined.SwapVert,
                            contentDescription = "Sort clients",
                            onClick = { sortMenuOpen = true },
                        )

                        DropdownMenu(
                            expanded = sortMenuOpen,
                            onDismissRequest = { sortMenuOpen = false },
                            modifier = Modifier.background(IzadiFoam),
                        ) {
                            ClientSort.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.label,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = if (option == sort) IzadiSage else IzadiInk,
                                        )
                                    },
                                    onClick = {
                                        sort = option
                                        sortMenuOpen = false
                                        listResetKey += 1
                                    },
                                )
                            }
                        }
                    }
                }
            }

            if (searchOpen) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .focusRequester(searchFocus),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "Search by name or mobile",
                            color = IzadiInkSoft.copy(alpha = 0.55f),
                        )
                    },
                    textStyle = MaterialTheme.typography.titleMedium.copy(color = IzadiInk),
                    shape = RoundedCornerShape(18.dp),
                    colors = softFieldColors(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            ClientTabs(
                tabs = tabs,
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it },
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(18.dp))

            Box(modifier = Modifier.weight(1f)) {
                key(listResetKey) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 24.dp,
                            end = 24.dp,
                            bottom = 12.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(
                            items = visibleClients,
                            key = { it.id },
                        ) { client ->
                            ClientCard(
                                client = client,
                                sessionSummary = clientSessionSummary(
                                    sessionsByClient[client.id].orEmpty(),
                                ),
                                highlight = showActive && client.id == highlightClientId,
                                onHighlightFinished = onHighlightConsumed,
                                onClick = { onOpenClient(client) },
                            )
                        }
                    }
                }
            }

            if (onNewClient != null) {
                SoftActionButton(
                    label = "New Client",
                    onClick = onNewClient,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun CircularHeaderIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(IzadiBloom.copy(alpha = 0.75f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    bounded = true,
                    color = IzadiSageSoft.copy(alpha = 0.35f),
                ),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = IzadiSageSoft,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun ClientTabs(
    tabs: List<ClientTab>,
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
private fun ClientCard(
    client: Client,
    sessionSummary: String,
    highlight: Boolean,
    onHighlightFinished: () -> Unit,
    onClick: () -> Unit,
) {
    val restColor = IzadiFoam.copy(alpha = 0.95f)
    val pulseColor = IzadiBloom
    var pulseOn by remember { mutableStateOf(false) }

    LaunchedEffect(highlight, client.id) {
        if (!highlight) {
            pulseOn = false
            return@LaunchedEffect
        }
        // Soft green pulse — two gentle beats, then settle.
        repeat(2) {
            pulseOn = true
            delay(280)
            pulseOn = false
            delay(220)
        }
        onHighlightFinished()
    }

    val cardColor by animateColorAsState(
        targetValue = if (pulseOn) pulseColor else restColor,
        animationSpec = tween(durationMillis = 260),
        label = "clientCardPulse",
    )

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
                text = client.fullName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = IzadiInk,
                    fontSize = 16.sp,
                ),
            )
            Text(
                text = sessionSummary,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = IzadiInkSoft,
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
            contentDescription = null,
            tint = IzadiSageSoft,
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}

private fun clientSessionSummary(sessions: List<Session>): String {
    val upcoming = sessions.count { !isPastSession(it) }
    val completed = sessions.count { it.status == SessionStatus.Completed }
    return "$upcoming upcoming, $completed completed"
}
