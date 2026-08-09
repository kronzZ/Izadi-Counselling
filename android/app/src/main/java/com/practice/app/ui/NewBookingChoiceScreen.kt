package com.practice.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.practice.app.ui.theme.IzadiInkSoft
import com.practice.app.ui.theme.IzadiSage
import com.practice.app.ui.theme.IzadiSageSoft

/**
 * Choice screen only — continuous mint atmosphere, no SoftCloud / foam
 * "content well" like placeholder section pages (e.g. Payments).
 */
@Composable
fun NewBookingChoiceScreen(
    onBack: () -> Unit,
    onNewClient: () -> Unit,
    onExistingClient: () -> Unit,
) {
    val mint = Color(0xFFEAF2F0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(mint),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            IzadiSageSoft.copy(alpha = 0.22f),
                            IzadiSageSoft.copy(alpha = 0.10f),
                            IzadiSageSoft.copy(alpha = 0.16f),
                        ),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
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
                    text = "New booking",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Who is this session for?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = IzadiInkSoft,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SoftActionButton(
                    label = "New Client",
                    onClick = onNewClient,
                    modifier = Modifier.fillMaxWidth(),
                )
                SoftActionButton(
                    label = "Existing Client",
                    onClick = onExistingClient,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
