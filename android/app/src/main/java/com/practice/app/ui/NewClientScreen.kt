package com.practice.app.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.practice.app.data.Client
import com.practice.app.data.EmergencyRelationship
import com.practice.app.ui.theme.IzadiFoam
import com.practice.app.ui.theme.IzadiInk
import com.practice.app.ui.theme.IzadiInkSoft
import com.practice.app.ui.theme.IzadiSage
import com.practice.app.ui.theme.SoftScreenBackground
import java.util.UUID

@Composable
fun NewClientScreen(
    onBack: () -> Unit,
    onClientCreated: (Client) -> Unit,
    onBookFirstSession: (Client) -> Unit,
    onSkipFirstSession: (Client) -> Unit,
) {
    var firstName by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var emergencyContactName by remember { mutableStateOf("") }
    var emergencyContactNumber by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf<EmergencyRelationship?>(null) }
    var createdClient by remember { mutableStateOf<Client?>(null) }

    val canSave = firstName.isNotBlank() && surname.isNotBlank()

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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "New client",
                    style = MaterialTheme.typography.titleLarge,
                )

                Text(
                    text = "Add their details. You can book a session for them later.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = IzadiInkSoft,
                )

                Spacer(modifier = Modifier.height(4.dp))

                EditableField(
                    label = "First name",
                    value = firstName,
                    onValueChange = { firstName = it },
                    capitalization = KeyboardCapitalization.Words,
                )
                EditableField(
                    label = "Surname",
                    value = surname,
                    onValueChange = { surname = it },
                    capitalization = KeyboardCapitalization.Words,
                )
                DateOfBirthField(
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                )
                EditableField(
                    label = "Mobile",
                    value = mobile,
                    onValueChange = { mobile = it },
                    keyboardType = KeyboardType.Phone,
                )
                EditableField(
                    label = "Emergency contact name",
                    value = emergencyContactName,
                    onValueChange = { emergencyContactName = it },
                    capitalization = KeyboardCapitalization.Words,
                )
                EditableField(
                    label = "Emergency contact number",
                    value = emergencyContactNumber,
                    onValueChange = { emergencyContactNumber = it },
                    keyboardType = KeyboardType.Phone,
                )
                RelationshipDropdown(
                    selected = relationship,
                    onSelected = { relationship = it },
                )

                SoftActionButton(
                    label = "Create client",
                    onClick = {
                        if (!canSave || createdClient != null) return@SoftActionButton

                        val client = Client(
                            id = UUID.randomUUID().toString(),
                            firstName = firstName.trim(),
                            surname = surname.trim(),
                            dateOfBirth = dateOfBirth.trim(),
                            mobile = mobile.trim(),
                            emergencyContactName = emergencyContactName.trim(),
                            emergencyContactNumber = emergencyContactNumber.trim(),
                            relationship = relationship,
                            isActive = true,
                            createdAtEpochMs = System.currentTimeMillis(),
                        )
                        onClientCreated(client)
                        createdClient = client
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 12.dp)
                        .alpha(if (canSave) 1f else 0.45f),
                )
            }
        }
    }

    val pending = createdClient
    if (pending != null) {
        AlertDialog(
            onDismissRequest = { onSkipFirstSession(pending) },
            containerColor = IzadiFoam,
            title = {
                Text(
                    text = "Book first session?",
                    style = MaterialTheme.typography.titleMedium,
                    color = IzadiInk,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Would you like to set up ${pending.firstName}’s first session now?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = IzadiInkSoft,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SoftActionButton(
                        label = "Book session",
                        onClick = { onBookFirstSession(pending) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SoftActionButton(
                        label = "Not now",
                        onClick = { onSkipFirstSession(pending) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {},
            dismissButton = {},
        )
    }
}
