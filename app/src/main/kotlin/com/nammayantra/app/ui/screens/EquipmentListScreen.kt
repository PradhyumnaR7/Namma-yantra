package com.nammayantra.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.nammayantra.app.data.MachineType
import com.nammayantra.app.navigation.NavRoutes
import com.nammayantra.app.services.LocationService
import com.nammayantra.app.ui.components.MachineCard
import com.nammayantra.app.ui.components.MachineCardShimmer
import com.nammayantra.app.ui.theme.*
import com.nammayantra.app.viewmodel.MachineViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EquipmentListScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: MachineViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val machines by viewModel.nearbyMachines.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()
    val showAvailableOnly by viewModel.showAvailableOnly.collectAsStateWithLifecycle()

    val locationPermission = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION) { granted ->
        if (granted) {
            coroutineScope.launch {
                try {
                    val loc = LocationService(context).getCurrentLocation()
                    loc?.let { viewModel.updateUserLocation(it.latitude, it.longitude) }
                } catch (_: Exception) {}
            }
        }
    }

    LaunchedEffect(Unit) {
        if (locationPermission.status.isGranted) {
            try {
                val loc = LocationService(context).getCurrentLocation()
                loc?.let { viewModel.updateUserLocation(it.latitude, it.longitude) }
            } catch (_: Exception) {}
        } else {
            locationPermission.launchPermissionRequest()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Top search bar
        Surface(
            color = Surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search machines, owners...", color = TextHint) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextHint)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextHint)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.md),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Divider,
                        focusedLabelColor = Primary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter chips
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Available only toggle
                    FilterChip(
                        selected = showAvailableOnly,
                        onClick = { viewModel.setShowAvailableOnly(!showAvailableOnly) },
                        label = { Text("Available", style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        )
                    )

                    // Type filters
                    val types = listOf("All") + MachineType.all
                    types.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { viewModel.setSelectedType(type) },
                            label = {
                                Text(
                                    text = if (type == "All") "All" else "${MachineType.emoji(type)} $type",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Results count
        if (machines.isNotEmpty()) {
            Text(
                text = "${machines.size} machine${if (machines.size != 1) "s" else ""} found",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // List
        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (machines.isEmpty() && searchQuery.isEmpty() && selectedType == "All") {
                // Loading shimmer
                items(5) { MachineCardShimmer() }
            } else if (machines.isEmpty()) {
                item {
                    EmptyState(
                        emoji = "🔍",
                        title = "No machines found",
                        subtitle = "Try adjusting your filters or search query"
                    )
                }
            } else {
                items(machines, key = { it.id }) { machine ->
                    MachineCard(machine = machine) {
                        navController.navigate(NavRoutes.machineDetail(machine.id))
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(emoji: String, title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = emoji, style = MaterialTheme.typography.displaySmall)
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}
