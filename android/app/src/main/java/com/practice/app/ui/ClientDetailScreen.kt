package com.practice.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.practice.app.data.Client
import com.practice.app.data.EmergencyRelationship
import com.practice.app.data.capitalizeFirstLetter
import com.practice.app.data.formatDateOfBirthInput
import com.practice.app.data.formatDateOfBirthSelection
import com.practice.app.ui.theme.IzadiBloom
import com.practice.app.ui.theme.IzadiFoam
import com.practice.app.ui.theme.IzadiInk
import com.practice.app.ui.theme.IzadiInkSoft
import com.practice.app.ui.theme.IzadiRose
import com.practice.app.ui.theme.IzadiRoseDeep
import com.practice.app.ui.theme.IzadiSage
import com.practice.app.ui.theme.IzadiSageSoft
import com.practice.app.ui.theme.SoftScreenBackground

@Composable
fun ClientDetailScreen(
    client: Client,
    onBack: () -> Unit,
    onClientChange: (Client) -> Unit,
    onToggleActive: () -> Unit,
    onViewSessions: () -> Unit,
    onDeleteClient: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

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
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Client details",
                    style = MaterialTheme.typography.titleLarge,
                )

                Spacer(modifier = Modifier.height(4.dp))

                EditableField(
                    label = "First name",
                    value = client.firstName,
                    onValueChange = { onClientChange(client.copy(firstName = it)) },
                    capitalization = KeyboardCapitalization.Words,
                )
                EditableField(
                    label = "Surname",
                    value = client.surname,
                    onValueChange = { onClientChange(client.copy(surname = it)) },
                    capitalization = KeyboardCapitalization.Words,
                )
                DateOfBirthField(
                    value = client.dateOfBirth,
                    onValueChange = { onClientChange(client.copy(dateOfBirth = it)) },
                )
                EditableField(
                    label = "Mobile",
                    value = client.mobile,
                    onValueChange = { onClientChange(client.copy(mobile = it)) },
                    keyboardType = KeyboardType.Phone,
                )
                EditableField(
                    label = "Emergency contact name",
                    value = client.emergencyContactName,
                    onValueChange = { onClientChange(client.copy(emergencyContactName = it)) },
                    capitalization = KeyboardCapitalization.Words,
                )
                EditableField(
                    label = "Emergency contact number",
                    value = client.emergencyContactNumber,
                    onValueChange = { onClientChange(client.copy(emergencyContactNumber = it)) },
                    keyboardType = KeyboardType.Phone,
                )
                RelationshipDropdown(
                    selected = client.relationship,
                    onSelected = { onClientChange(client.copy(relationship = it)) },
                )

                Spacer(modifier = Modifier.height(8.dp))

                SoftActionButton(
                    label = "View sessions",
                    onClick = onViewSessions,
                    modifier = Modifier.fillMaxWidth(),
                )
                StatusToggleButton(
                    isActive = client.isActive,
                    onClick = onToggleActive,
                    modifier = Modifier.fillMaxWidth(),
                )
                SoftActionButton(
                    label = "Delete client",
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = IzadiRose,
                    contentColor = IzadiRoseDeep,
                    rippleColor = IzadiRoseDeep.copy(alpha = 0.25f),
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = IzadiFoam,
            title = {
                Text(
                    text = "Delete client?",
                    style = MaterialTheme.typography.titleMedium,
                    color = IzadiInk,
                )
            },
            text = {
                Text(
                    text = "This will permanently remove ${client.fullName} and all of their sessions. This can’t be undone.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = IzadiInkSoft,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteClient()
                    },
                ) {
                    Text("Delete", color = IzadiRoseDeep)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = IzadiSage)
                }
            },
        )
    }
}

@Composable
fun DateOfBirthField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    var fieldValue by remember {
        val formatted = formatDateOfBirthInput(value)
        mutableStateOf(
            TextFieldValue(
                text = formatted,
                selection = TextRange(formatted.length),
            ),
        )
    }

    LaunchedEffect(value) {
        if (value != fieldValue.text) {
            val formatted = formatDateOfBirthInput(value)
            fieldValue = TextFieldValue(
                text = formatted,
                selection = TextRange(formatted.length),
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "DATE OF BIRTH",
            style = MaterialTheme.typography.labelLarge,
            color = IzadiSageSoft,
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = fieldValue,
            onValueChange = { incoming ->
                val (formatted, cursor) = formatDateOfBirthSelection(
                    text = incoming.text,
                    selectionEnd = incoming.selection.end,
                )
                fieldValue = TextFieldValue(
                    text = formatted,
                    selection = TextRange(cursor),
                )
                onValueChange(formatted)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(color = IzadiInk),
            placeholder = {
                Text(
                    text = "DD/MM/YYYY",
                    style = MaterialTheme.typography.titleMedium,
                    color = IzadiInkSoft.copy(alpha = 0.55f),
                )
            },
            shape = RoundedCornerShape(18.dp),
            colors = softFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}

@Composable
fun EditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String? = null,
) {
    val isProseField = keyboardType == KeyboardType.Text ||
        keyboardType == KeyboardType.Ascii

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = IzadiSageSoft,
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { raw ->
                onValueChange(
                    if (isProseField) capitalizeFirstLetter(raw) else raw,
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(color = IzadiInk),
            placeholder = placeholder?.let {
                {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        color = IzadiInkSoft.copy(alpha = 0.55f),
                    )
                }
            },
            shape = RoundedCornerShape(18.dp),
            colors = softFieldColors(),
            keyboardOptions = KeyboardOptions(
                capitalization = if (isProseField) capitalization else KeyboardCapitalization.None,
                keyboardType = keyboardType,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelationshipDropdown(
    selected: EmergencyRelationship?,
    onSelected: (EmergencyRelationship) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "RELATIONSHIP",
            style = MaterialTheme.typography.labelLarge,
            color = IzadiSageSoft,
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = selected?.label.orEmpty(),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium.copy(color = IzadiInk),
                placeholder = {
                    Text(
                        text = "Select",
                        style = MaterialTheme.typography.titleMedium,
                        color = IzadiInkSoft.copy(alpha = 0.55f),
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = IzadiSageSoft,
                    )
                },
                shape = RoundedCornerShape(18.dp),
                colors = softFieldColors(),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(IzadiFoam),
            ) {
                EmergencyRelationship.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = IzadiInk,
                            )
                        },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun softFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = IzadiFoam.copy(alpha = 0.92f),
    unfocusedContainerColor = IzadiFoam.copy(alpha = 0.92f),
    disabledContainerColor = IzadiFoam.copy(alpha = 0.92f),
    focusedBorderColor = IzadiSageSoft.copy(alpha = 0.65f),
    unfocusedBorderColor = IzadiSageSoft.copy(alpha = 0.35f),
    cursorColor = IzadiSage,
    focusedTextColor = IzadiInk,
    unfocusedTextColor = IzadiInk,
)

@Composable
private fun StatusToggleButton(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = if (isActive) "Move to Inactive" else "Move To Active"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(IzadiBloom)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = IzadiSageSoft.copy(alpha = 0.35f)),
                onClick = onClick,
            )
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = IzadiSage,
            textAlign = TextAlign.Center,
        )
    }
}
