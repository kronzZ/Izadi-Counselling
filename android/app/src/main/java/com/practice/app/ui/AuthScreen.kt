package com.practice.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.practice.app.R
import com.practice.app.ui.theme.IzadiInk
import com.practice.app.ui.theme.IzadiInkSoft
import com.practice.app.ui.theme.IzadiRoseDeep
import com.practice.app.ui.theme.IzadiSage
import com.practice.app.ui.theme.SoftScreenBackground

@Composable
fun AuthScreen(
    errorMessage: String?,
    onSignIn: (email: String, password: String) -> Unit,
    onSignUp: (email: String, password: String) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var creating by remember { mutableStateOf(false) }

    val canSubmit = email.isNotBlank() && password.length >= 6

    SoftScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.izadi_logo),
                contentDescription = null,
                modifier = Modifier.size(112.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Izadi",
                style = MaterialTheme.typography.displayLarge,
                color = IzadiInk,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (creating) {
                    "Create your practice account to sync clients and sessions in the cloud."
                } else {
                    "Sign in to open your practice across Android and iPhone."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = IzadiInkSoft,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = IzadiRoseDeep,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            SoftActionButton(
                label = if (creating) "Create account" else "Sign in",
                onClick = {
                    if (!canSubmit) return@SoftActionButton
                    if (creating) onSignUp(email, password) else onSignIn(email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (canSubmit) 1f else 0.45f),
            )
            TextButton(onClick = { creating = !creating }) {
                Text(
                    text = if (creating) {
                        "Already have an account? Sign in"
                    } else {
                        "New here? Create an account"
                    },
                    color = IzadiSage,
                )
            }
        }
    }
}
