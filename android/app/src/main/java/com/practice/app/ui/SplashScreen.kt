package com.practice.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.practice.app.R
import com.practice.app.ui.theme.IzadiInkSoft
import com.practice.app.ui.theme.IzadiSageSoft

/**
 * Full-bleed mint splash — large centered bird, no SoftCloud band,
 * no radial “white panel” wash from SoftScreenBackground.
 */
@Composable
fun SplashScreen() {
    val mint = Color(0xFFEAF2F0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(mint),
        contentAlignment = Alignment.Center,
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
        Image(
            painter = painterResource(R.drawable.izadi_logo),
            contentDescription = null,
            modifier = Modifier.size(300.dp),
            contentScale = ContentScale.Fit,
        )
        Text(
            text = "© TurtleTech Designs 2026 💚",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 22.sp,
                color = IzadiInkSoft.copy(alpha = 0.75f),
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
        )
    }
}
