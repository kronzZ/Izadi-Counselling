package com.practice.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.practice.app.data.Client
import com.practice.app.ui.theme.IzadiInk
import com.practice.app.ui.theme.IzadiInkSoft
import com.practice.app.ui.theme.IzadiSage
import com.practice.app.ui.theme.IzadiSageSoft
import com.practice.app.ui.theme.SoftScreenBackground
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun BookSessionScreen(
    client: Client,
    onBack: () -> Unit,
    onSaveBooking: (date: LocalDate, time: LocalTime, durationMinutes: Int) -> Unit,
) {
    val availableDates = remember { rollingDates() }
    var selectedDate by remember { mutableStateOf(availableDates.first()) }
    var durationMinutes by remember { mutableIntStateOf(50) }

    val initialTime = remember { nextFiveMinuteSlot() }
    var hour by remember { mutableIntStateOf(initialTime.hour12) }
    var minute by remember { mutableIntStateOf(initialTime.minute) }
    var isPm by remember { mutableStateOf(initialTime.isPm) }

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
                    text = "Book session",
                    style = MaterialTheme.typography.titleLarge,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "CLIENT",
                    style = MaterialTheme.typography.labelLarge,
                    color = IzadiSageSoft,
                )
                Text(
                    text = client.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    color = IzadiInk,
                )
                Text(
                    text = client.mobile,
                    style = MaterialTheme.typography.bodyMedium,
                    color = IzadiInkSoft,
                )

                Spacer(modifier = Modifier.height(8.dp))

                DateDropdownField(
                    selectedDate = selectedDate,
                    dates = availableDates,
                    onDateSelected = { selectedDate = it },
                )

                SimpleTimePicker(
                    hour = hour,
                    minute = minute,
                    isPm = isPm,
                    onHourChange = { hour = it },
                    onMinuteChange = { minute = it },
                    onIsPmChange = { isPm = it },
                )

                DurationDropdownField(
                    durationMinutes = durationMinutes,
                    onDurationSelected = { durationMinutes = it },
                )
            }

            SoftActionButton(
                label = "Save booking",
                onClick = {
                    onSaveBooking(
                        selectedDate,
                        toLocalTime(hour, minute, isPm),
                        durationMinutes,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 20.dp),
            )
        }
    }
}
