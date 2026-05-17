package com.nammayantra.app.ui.screens

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nammayantra.app.data.UiState
import com.nammayantra.app.ui.theme.*
import com.nammayantra.app.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity

    val otpSentState by authViewModel.otpSentState.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var showOtpField by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(otpSentState) {
        when (otpSentState) {
            is UiState.Success -> {
                showOtpField = true
                errorMessage = ""
            }
            is UiState.Error -> {
                errorMessage = (otpSentState as UiState.Error).message
            }
            else -> {}
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is UiState.Success -> onAuthSuccess()
            is UiState.Error -> errorMessage = (authState as UiState.Error).message
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(HeroGradientStart, GradientEnd)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.3f))

            // Logo area
            Text(text = "🚜", fontSize = 72.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Namma Yantra",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White
            )
            Text(
                text = "Agriculture Machinery Rental",
                style = MaterialTheme.typography.bodyMedium,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.weight(0.3f))

            // Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.xl),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (showOtpField) "Enter OTP" else "Sign In",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (showOtpField)
                            "We sent a 6-digit code to +91 $phoneNumber"
                        else
                            "Enter your mobile number to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    AnimatedVisibility(visible = !showOtpField) {
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { if (it.length <= 10) phoneNumber = it },
                            label = { Text("Mobile Number") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Primary)
                            },
                            prefix = { Text("+91 ", color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(Radius.md),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                focusedLabelColor = Primary
                            )
                        )
                    }

                    AnimatedVisibility(visible = showOtpField) {
                        OutlinedTextField(
                            value = otp,
                            onValueChange = { if (it.length <= 6) otp = it },
                            label = { Text("6-digit OTP") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(Radius.md),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                focusedLabelColor = Primary
                            )
                        )
                    }

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    val isLoading = otpSentState is UiState.Loading || authState is UiState.Loading

                    Button(
                        onClick = {
                            errorMessage = ""
                            if (!showOtpField) {
                                if (phoneNumber.length == 10) {
                                    authViewModel.sendOtp("+91$phoneNumber", activity)
                                } else {
                                    errorMessage = "Enter a valid 10-digit number"
                                }
                            } else {
                                if (otp.length == 6) {
                                    authViewModel.verifyOtp(otp)
                                } else {
                                    errorMessage = "Enter the 6-digit OTP"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(Radius.md),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = androidx.compose.ui.graphics.Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (showOtpField) "Verify OTP" else "Send OTP",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (showOtpField) {
                        TextButton(
                            onClick = {
                                showOtpField = false
                                otp = ""
                                authViewModel.resetState()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Change Number", color = Primary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))
        }
    }
}
