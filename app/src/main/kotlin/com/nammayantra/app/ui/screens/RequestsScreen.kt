package com.nammayantra.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nammayantra.app.data.RequestStatus
import com.nammayantra.app.data.UserProfile
import com.nammayantra.app.ui.components.BookingCard
import com.nammayantra.app.ui.components.BookingCardShimmer
import com.nammayantra.app.ui.theme.*
import com.nammayantra.app.viewmodel.BookingViewModel

@Composable
fun RequestsScreen(
    userProfile: UserProfile?,
    modifier: Modifier = Modifier,
    viewModel: BookingViewModel = viewModel()
) {
    val userId = userProfile?.uid ?: ""
    val bookings by viewModel.getUserBookings(userId).collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Pending", "Accepted", "Completed")

    val filtered = when (selectedTab) {
        1 -> bookings.filter { it.getStatus() == RequestStatus.PENDING }
        2 -> bookings.filter { it.getStatus() == RequestStatus.ACCEPTED }
        3 -> bookings.filter { it.getStatus() == RequestStatus.COMPLETED }
        else -> bookings
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Header
        Surface(color = Surface, shadowElevation = 1.dp) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "My Bookings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Surface,
                    contentColor = Primary,
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = tab,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 0.dp)
        ) {
            if (userId.isEmpty()) {
                item {
                    EmptyState(
                        emoji = "🔐",
                        title = "Not signed in",
                        subtitle = "Please sign in to view your bookings"
                    )
                }
            } else if (filtered.isEmpty() && bookings.isEmpty()) {
                items(3) { BookingCardShimmer() }
            } else if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        emoji = "📋",
                        title = "No bookings here",
                        subtitle = "Your ${tabs[selectedTab].lowercase()} bookings will appear here"
                    )
                }
            } else {
                items(filtered, key = { it.id }) { request ->
                    BookingCard(request = request)
                }
            }
        }
    }
}
