package com.practice.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.practice.app.data.ordinalSuffix
import com.practice.app.ui.theme.IzadiBloom
import com.practice.app.ui.theme.IzadiFoam
import com.practice.app.ui.theme.IzadiInk
import com.practice.app.ui.theme.IzadiInkSoft
import com.practice.app.ui.theme.IzadiSage
import com.practice.app.ui.theme.IzadiSageSoft
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

val SessionDurationOptions = listOf(30, 45, 50, 60, 90)

data class ClockParts(
    val hour12: Int,
    val minute: Int,
    val isPm: Boolean,
)

fun nextFiveMinuteSlot(from: LocalTime = LocalTime.now()): ClockParts {
    var hour = from.hour
    var minute = ((from.minute / 5) + 1) * 5
    if (minute >= 60) {
        minute = 0
        hour = (hour + 1) % 24
    }
    val isPm = hour >= 12
    val hour12 = when (val h = hour % 12) {
        0 -> 12
        else -> h
    }
    return ClockParts(hour12 = hour12, minute = minute, isPm = isPm)
}

fun toLocalTime(hour12: Int, minute: Int, isPm: Boolean): LocalTime {
    val hour24 = when {
        hour12 == 12 && !isPm -> 0
        hour12 == 12 && isPm -> 12
        isPm -> hour12 + 12
        else -> hour12
    }
    return LocalTime.of(hour24, minute)
}

fun clockPartsFrom(time: LocalTime): ClockParts {
    val hour24 = time.hour
    val isPm = hour24 >= 12
    val hour12 = when (val h = hour24 % 12) {
        0 -> 12
        else -> h
    }
    val minute = (time.minute / 5) * 5
    return ClockParts(hour12 = hour12, minute = minute, isPm = isPm)
}

fun formatFriendlyDate(date: LocalDate): String {
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val day = "${date.dayOfMonth}${ordinalSuffix(date.dayOfMonth)}"
    val month = date.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    return "$dayName, $day $month ${date.year}"
}

fun rollingDates(days: Int = 60): List<LocalDate> {
    val today = LocalDate.now()
    return (0 until days).map { today.plusDays(it.toLong()) }
}

@Composable
fun schedulingFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = IzadiFoam.copy(alpha = 0.92f),
    unfocusedContainerColor = IzadiFoam.copy(alpha = 0.92f),
    disabledContainerColor = IzadiFoam.copy(alpha = 0.92f),
    focusedBorderColor = IzadiSageSoft.copy(alpha = 0.55f),
    unfocusedBorderColor = Color.Transparent,
    cursorColor = IzadiSage,
    focusedTextColor = IzadiInk,
    unfocusedTextColor = IzadiInk,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateDropdownField(
    selectedDate: LocalDate,
    dates: List<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "DATE",
            style = MaterialTheme.typography.labelLarge,
            color = IzadiSageSoft,
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = formatFriendlyDate(selectedDate),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    color = IzadiInk,
                    fontSize = 17.sp,
                ),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = IzadiSageSoft,
                    )
                },
                shape = RoundedCornerShape(18.dp),
                colors = schedulingFieldColors(),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(IzadiFoam),
            ) {
                dates.forEach { date ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = formatFriendlyDate(date),
                                style = MaterialTheme.typography.bodyLarge,
                                color = IzadiInk,
                            )
                        },
                        onClick = {
                            onDateSelected(date)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurationDropdownField(
    durationMinutes: Int,
    onDurationSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SESSION DURATION",
            style = MaterialTheme.typography.labelLarge,
            color = IzadiSageSoft,
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = "$durationMinutes minutes",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium.copy(color = IzadiInk),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = IzadiSageSoft,
                    )
                },
                shape = RoundedCornerShape(18.dp),
                colors = schedulingFieldColors(),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(IzadiFoam),
            ) {
                SessionDurationOptions.forEach { minutes ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "$minutes minutes",
                                style = MaterialTheme.typography.bodyLarge,
                                color = IzadiInk,
                            )
                        },
                        onClick = {
                            onDurationSelected(minutes)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleTimePicker(
    hour: Int,
    minute: Int,
    isPm: Boolean,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onIsPmChange: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "TIME",
            style = MaterialTheme.typography.labelLarge,
            color = IzadiSageSoft,
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(IzadiFoam.copy(alpha = 0.92f))
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperColumn(
                label = "Hour",
                valueText = hour.toString().padStart(2, '0'),
                onIncrement = { onHourChange(if (hour >= 12) 1 else hour + 1) },
                onDecrement = { onHourChange(if (hour <= 1) 12 else hour - 1) },
            )

            Text(
                text = ":",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = IzadiSage,
                    fontSize = 28.sp,
                ),
            )

            StepperColumn(
                label = "Min",
                valueText = minute.toString().padStart(2, '0'),
                onIncrement = { onMinuteChange((minute + 5) % 60) },
                onDecrement = { onMinuteChange(if (minute == 0) 55 else minute - 5) },
            )

            Spacer(modifier = Modifier.width(4.dp))

            StepperColumn(
                label = "Period",
                valueText = if (isPm) "PM" else "AM",
                onIncrement = { onIsPmChange(!isPm) },
                onDecrement = { onIsPmChange(!isPm) },
            )
        }
    }
}

@Composable
private fun StepperColumn(
    label: String,
    valueText: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
            ),
            color = IzadiSageSoft,
        )

        StepperButton(
            icon = Icons.Outlined.KeyboardArrowUp,
            contentDescription = "Increase $label",
            onClick = onIncrement,
        )

        Text(
            text = valueText,
            style = MaterialTheme.typography.titleLarge.copy(
                color = IzadiInk,
                fontSize = 28.sp,
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(56.dp),
        )

        StepperButton(
            icon = Icons.Outlined.KeyboardArrowDown,
            contentDescription = "Decrease $label",
            onClick = onDecrement,
        )
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(IzadiBloom)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = IzadiSageSoft.copy(alpha = 0.35f)),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = IzadiSage,
            modifier = Modifier.size(22.dp),
        )
    }
}
