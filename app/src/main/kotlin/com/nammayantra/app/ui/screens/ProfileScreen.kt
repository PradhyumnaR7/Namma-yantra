package com.nammayantra.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nammayantra.app.data.UserProfile
import com.nammayantra.app.data.UserType
import com.nammayantra.app.ui.theme.*
import com.nammayantra.app.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel()
) {
    var showSignOutDialog by remember { mutableStateOf(false) }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.signOut()
                    onSignOut()
                }) {
                    Text("Sign Out", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        // Profile header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(HeroGradientStart, GradientEnd)))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(Radius.full))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (userProfile?.getUserType() == UserType.OWNER) "🚜" else "👨‍🌾",
                        fontSize = 40.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = userProfile?.name ?: "User",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = userProfile?.phone ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.full))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (userProfile?.getUserType() == UserType.OWNER) "Machine Owner" else "Farmer",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info section
        if (userProfile != null) {
            ProfileSection(title = "Account Info") {
                ProfileRow(icon = Icons.Default.Person, label = "Name", value = userProfile.name)
                ProfileRow(icon = Icons.Default.Phone, label = "Phone", value = userProfile.phone)
                if (userProfile.village.isNotEmpty()) {
                    ProfileRow(icon = Icons.Default.LocationOn, label = "Village", value = userProfile.village)
                }
                if (userProfile.district.isNotEmpty()) {
                    ProfileRow(icon = Icons.Default.Map, label = "District", value = userProfile.district)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // App section
        ProfileSection(title = "App") {
            ProfileRow(icon = Icons.Default.Info, label = "Version", value = "1.0.0")
            ProfileRow(icon = Icons.Default.Star, label = "Rate App", value = "")
            ProfileRow(icon = Icons.Default.Share, label = "Share App", value = "")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sign out
        Button(
            onClick = { showSignOutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(Radius.md),
            colors = ButtonDefaults.buttonColors(
                containerColor = Error.copy(alpha = 0.1f),
                contentColor = Error
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Card(
            shape = RoundedCornerShape(Radius.lg),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        } else {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextHint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
