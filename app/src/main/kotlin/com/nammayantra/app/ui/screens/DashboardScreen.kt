package com.nammayantra.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nammayantra.app.data.RequestStatus
import com.nammayantra.app.data.UserProfile
import com.nammayantra.app.data.UserType
import com.nammayantra.app.navigation.NavRoutes
import com.nammayantra.app.ui.components.BookingCard
import com.nammayantra.app.ui.components.BookingCardShimmer
import com.nammayantra.app.ui.components.MachineCard
import com.nammayantra.app.ui.theme.*
import com.nammayantra.app.viewmodel.BookingViewModel
import com.nammayantra.app.viewmodel.MachineViewModel

@Composable
fun DashboardScreen(
    navController: NavController,
    userProfile: UserProfile?,
    modifier: Modifier = Modifier,
    machineViewModel: MachineViewModel = viewModel(),
    bookingViewModel: BookingViewModel = viewModel()
) {
    val isOwner = userProfile?.getUserType() == UserType.OWNER
    val userId = userProfile?.uid ?: ""

    if (isOwner) {
        OwnerDashboard(
            navController = navController,
            userId = userId,
            userProfile = userProfile,
            modifier = modifier,
            machineViewModel = machineViewModel,
            bookingViewModel = bookingViewModel
        )
    } else {
        FarmerDashboard(
            navController = navController,
            userId = userId,
            modifier = modifier,
            bookingViewModel = bookingViewModel
        )
    }
}

@Composable
private fun OwnerDashboard(
    navController: NavController,
    userId: String,
    userProfile: UserProfile?,
    modifier: Modifier = Modifier,
    machineViewModel: MachineViewModel,
    bookingViewModel: BookingViewModel
) {
    val myMachines by machineViewModel.getOwnerMachines(userId).collectAsStateWithLifecycle(initialValue = emptyList())
    val incomingRequests by bookingViewModel.getOwnerBookings(userId).collectAsStateWithLifecycle(initialValue = emptyList())
    val pendingCount = incomingRequests.count { it.getStatus() == RequestStatus.PENDING }
    val totalEarnings = incomingRequests
        .filter { it.getStatus() == RequestStatus.COMPLETED }
        .sumOf { it.totalPrice }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(HeroGradientStart, GradientEnd)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Owner Dashboard",
                        style = MaterialTheme.typography.titleMedium,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Welcome, ${userProfile?.name?.split(" ")?.firstOrNull() ?: "Owner"}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
            }
        }

        // Stats row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    emoji = "🚜",
                    value = "${myMachines.size}",
                    label = "Machines"
                )
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    emoji = "📋",
                    value = "$pendingCount",
                    label = "Pending"
                )
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    emoji = "💰",
                    value = "₹${(totalEarnings / 1000).toInt()}K",
                    label = "Earned"
                )
            }
        }

        // My machines section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Machines",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                FloatingActionButton(
                    onClick = { navController.navigate(NavRoutes.ADD_MACHINE) },
                    modifier = Modifier.size(40.dp),
                    containerColor = Primary,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Machine", modifier = Modifier.size(20.dp))
                }
            }
        }

        if (myMachines.isEmpty()) {
            item {
                EmptyState(
                    emoji = "🚜",
                    title = "No machines yet",
                    subtitle = "Tap + to add your first machine"
                )
            }
        } else {
            items(myMachines, key = { it.id }) { machine ->
                MachineCard(machine = machine) {
                    navController.navigate(NavRoutes.machineDetail(machine.id))
                }
            }
        }

        // Incoming requests
        item {
            Text(
                text = "Incoming Requests",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (incomingRequests.isEmpty()) {
            item {
                EmptyState(
                    emoji = "📭",
                    title = "No requests yet",
                    subtitle = "Booking requests will appear here"
                )
            }
        } else {
            items(incomingRequests.take(10), key = { it.id }) { request ->
                BookingCard(
                    request = request,
                    showActions = true,
                    onAccept = {
                        bookingViewModel.acceptBooking(request.id, request.machineId)
                    },
                    onDecline = {
                        bookingViewModel.declineBooking(request.id, request.machineId)
                    }
                )
            }
        }
    }
}

@Composable
private fun FarmerDashboard(
    navController: NavController,
    userId: String,
    modifier: Modifier = Modifier,
    bookingViewModel: BookingViewModel
) {
    val bookings by bookingViewModel.getUserBookings(userId).collectAsStateWithLifecycle(initialValue = emptyList())
    val activeCount = bookings.count { it.getStatus() == RequestStatus.ACCEPTED }
    val pendingCount = bookings.count { it.getStatus() == RequestStatus.PENDING }
    val totalSpent = bookings.filter { it.getStatus() == RequestStatus.COMPLETED }.sumOf { it.totalPrice }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(HeroGradientStart, GradientEnd)
                        )
                    )
                    .padding(20.dp)
            ) {
                Text(
                    text = "My Activity",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardStatCard(Modifier.weight(1f), "✅", "$activeCount", "Active")
                DashboardStatCard(Modifier.weight(1f), "⏳", "$pendingCount", "Pending")
                DashboardStatCard(Modifier.weight(1f), "💸", "₹${(totalSpent / 1000).toInt()}K", "Spent")
            }
        }

        item {
            Text(
                text = "Recent Bookings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (bookings.isEmpty()) {
            items(3) { BookingCardShimmer() }
        } else {
            items(bookings, key = { it.id }) { request ->
                BookingCard(request = request)
            }
        }
    }
}

@Composable
private fun DashboardStatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
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
}
