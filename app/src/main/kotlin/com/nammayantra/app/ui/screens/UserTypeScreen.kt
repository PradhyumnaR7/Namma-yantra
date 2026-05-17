package com.nammayantra.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nammayantra.app.data.UserType
import com.nammayantra.app.ui.theme.*
import com.nammayantra.app.viewmodel.AuthViewModel

@Composable
fun UserTypeScreen(
    phone: String,
    onComplete: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var selectedType by remember { mutableStateOf<UserType?>(null) }
    var name by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Who are you?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This helps us personalise your experience",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Farmer card
            UserTypeCard(
                emoji = "👨‍🌾",
                title = "Farmer",
                subtitle = "I want to rent machinery for my farm",
                isSelected = selectedType == UserType.FARMER,
                onClick = { selectedType = UserType.FARMER }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Owner card
            UserTypeCard(
                emoji = "🚜",
                title = "Machine Owner",
                subtitle = "I own machinery and want to rent it out",
                isSelected = selectedType == UserType.OWNER,
                onClick = { selectedType = UserType.OWNER }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Your Full Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.md),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary
                )
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage, color = Error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    when {
                        selectedType == null -> errorMessage = "Please select your role"
                        name.trim().isEmpty() -> errorMessage = "Please enter your name"
                        else -> {
                            authViewModel.saveUserProfile(name.trim(), selectedType!!, phone)
                            onComplete()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(Radius.md),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun UserTypeCard(
    emoji: String,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Primary else Divider
    val bgColor = if (isSelected) PrimaryContainer else Surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.lg))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(Radius.lg)
            )
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 40.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Primary else TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(Radius.full))
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = androidx.compose.ui.graphics.Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
