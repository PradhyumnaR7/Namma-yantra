package com.nammayantra.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nammayantra.app.data.MachineType
import com.nammayantra.app.data.UserProfile
import com.nammayantra.app.navigation.NavRoutes
import com.nammayantra.app.ui.components.MachineCard
import com.nammayantra.app.ui.components.MachineCardShimmer
import com.nammayantra.app.ui.theme.*
import com.nammayantra.app.viewmodel.MachineViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    userProfile: UserProfile?,
    modifier: Modifier = Modifier,
    viewModel: MachineViewModel = viewModel()
) {
    val machines by viewModel.nearbyMachines.collectAsStateWithLifecycle()
    val isLoading = machines.isEmpty()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Hero section
        item {
            HeroSection(
                userName = userProfile?.name?.split(" ")?.firstOrNull() ?: "Farmer",
                onSearchClick = { navController.navigate(NavRoutes.MACHINES) }
            )
        }

        // Category chips
        item {
            CategorySection(
                onCategoryClick = { type ->
                    viewModel.setSelectedType(type)
                    navController.navigate(NavRoutes.MACHINES)
                }
            )
        }

        // Featured / Nearby header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nearby Machines",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                TextButton(onClick = { navController.navigate(NavRoutes.MACHINES) }) {
                    Text("See all", color = Primary, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Shimmer or machines
        if (isLoading) {
            items(4) { MachineCardShimmer() }
        } else {
            items(machines.take(6), key = { it.id }) { machine ->
                MachineCard(machine = machine) {
                    navController.navigate(NavRoutes.machineDetail(machine.id))
                }
            }
        }

        // Stats banner
        item {
            StatsBanner()
        }
    }
}

@Composable
private fun HeroSection(userName: String, onSearchClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(HeroGradientStart, GradientEnd))
            )
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        Column {
            Text(
                text = "Good day, $userName 👋",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Find machinery\nnear your farm",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 36.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Search bar (tappable, navigates to machines)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.md))
                    .background(Color.White)
                    .clickable { onSearchClick() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextHint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Search tractors, harvesters...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextHint
                )
            }
        }
    }
}

@Composable
private fun CategorySection(onCategoryClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(top = 20.dp)) {
        Text(
            text = "Browse by Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(MachineType.all) { type ->
                CategoryChip(
                    emoji = MachineType.emoji(type),
                    label = type,
                    onClick = { onCategoryClick(type) }
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(emoji: String, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.lg))
            .background(Surface)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatsBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(Radius.lg))
            .background(PrimaryContainer)
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(value = "500+", label = "Machines")
        VerticalDivider(modifier = Modifier.height(40.dp), color = Primary.copy(alpha = 0.2f))
        StatItem(value = "1200+", label = "Farmers")
        VerticalDivider(modifier = Modifier.height(40.dp), color = Primary.copy(alpha = 0.2f))
        StatItem(value = "50+", label = "Districts")
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
