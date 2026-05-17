package com.nammayantra.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.nammayantra.app.data.*
import com.nammayantra.app.ui.components.AvailabilityBadge
import com.nammayantra.app.ui.components.MachineCardShimmer
import com.nammayantra.app.ui.theme.*
import com.nammayantra.app.viewmodel.BookingViewModel
import com.nammayantra.app.viewmodel.MachineViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineDetailScreen(
    machineId: String,
    navController: NavController,
    userProfile: UserProfile?,
    modifier: Modifier = Modifier,
    machineViewModel: MachineViewModel = viewModel(),
    bookingViewModel: BookingViewModel = viewModel()
) {
    val machineState by machineViewModel.selectedMachine.collectAsStateWithLifecycle()
    val requestState by bookingViewModel.requestState.collectAsStateWithLifecycle()
    val totalPrice by bookingViewModel.totalPrice.collectAsStateWithLifecycle()

    var showBookingSheet by remember { mutableStateOf(false) }
    var snackMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(machineId) {
        machineViewModel.loadMachineById(machineId)
    }

    LaunchedEffect(requestState) {
        when (requestState) {
            is UiState.Success -> {
                snackMessage = "Booking request sent successfully!"
                showBookingSheet = false
                bookingViewModel.resetRequestState()
            }
            is UiState.Error -> {
                snackMessage = (requestState as UiState.Error).message
                bookingViewModel.resetRequestState()
            }
            else -> {}
        }
        if (snackMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(snackMessage)
            snackMessage = ""
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    when (machineState) {
                        is UiState.Success -> Text(
                            (machineState as UiState.Success<Machine>).data.name.ifEmpty {
                                (machineState as UiState.Success<Machine>).data.type
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        else -> Text("Machine Details")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        when (machineState) {
            is UiState.Loading, UiState.Empty -> {
                Column(modifier = Modifier.padding(padding)) {
                    repeat(3) { MachineCardShimmer() }
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (machineState as UiState.Error).message,
                        color = Error
                    )
                }
            }
            is UiState.Success -> {
                val machine = (machineState as UiState.Success<Machine>).data
                MachineDetailContent(
                    machine = machine,
                    modifier = Modifier.padding(padding),
                    onBookClick = { showBookingSheet = true }
                )

                if (showBookingSheet) {
                    BookingBottomSheet(
                        machine = machine,
                        userProfile = userProfile,
                        totalPrice = totalPrice,
                        requestState = requestState,
                        onDismiss = { showBookingSheet = false },
                        onPriceCalculate = { hourly, daily, hours, type ->
                            bookingViewModel.calculatePrice(hourly, daily, hours, type)
                        },
                        onSendRequest = { hours, date, type ->
                            bookingViewModel.sendRequest(
                                machine = machine,
                                durationHours = hours,
                                startDate = date,
                                rentalType = type,
                                requesterName = userProfile?.name ?: "User",
                                requesterPhone = userProfile?.phone ?: ""
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MachineDetailContent(
    machine: Machine,
    modifier: Modifier = Modifier,
    onBookClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(scrollState)
    ) {
        // Hero image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            if (machine.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = machine.imageUrl,
                    contentDescription = machine.type,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(HeroGradientStart, GradientEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = MachineType.emoji(machine.type), fontSize = 80.sp)
                }
            }
            // Gradient overlay at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Background)))
            )
        }

        // Main content card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title + availability
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = machine.name.ifEmpty { machine.type },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = machine.type,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                AvailabilityBadge(isAvailable = machine.isAvailable)
            }

            // Pricing card
            Card(
                shape = RoundedCornerShape(Radius.lg),
                colors = CardDefaults.cardColors(containerColor = PrimaryContainer),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PriceItem(label = "Per Hour", value = "₹${machine.hourlyRate.toInt()}")
                    VerticalDivider(modifier = Modifier.height(40.dp), color = Primary.copy(alpha = 0.2f))
                    PriceItem(label = "Per Day", value = "₹${machine.dailyRate.toInt()}")
                }
            }

            // Info grid
            Card(
                shape = RoundedCornerShape(Radius.lg),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Machine Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    InfoRow(icon = "⭐", label = "Condition", value = "${"%.1f".format(machine.conditionRating)}/5")
                    InfoRow(icon = "🔧", label = "Last Service", value = if (machine.lastServiceDate > 0) dateFormat.format(Date(machine.lastServiceDate)) else "Not specified")
                    InfoRow(icon = "📍", label = "Location", value = listOf(machine.village, machine.district).filter { it.isNotEmpty() }.joinToString(", ").ifEmpty { "Location not set" })
                    if (machine.distanceKm > 0) {
                        InfoRow(icon = "🗺️", label = "Distance", value = "${"%.1f".format(machine.distanceKm)} km away")
                    }
                    if (machine.description.isNotEmpty()) {
                        InfoRow(icon = "📝", label = "Description", value = machine.description)
                    }
                }
            }

            // Owner card
            Card(
                shape = RoundedCornerShape(Radius.lg),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(Radius.full))
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👨‍🌾", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = machine.ownerName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Machine Owner",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    if (machine.ownerPhone.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Map
            if (machine.latitude != 0.0 && machine.longitude != 0.0) {
                Card(
                    shape = RoundedCornerShape(Radius.lg),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    val cameraState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(
                            LatLng(machine.latitude, machine.longitude), 14f
                        )
                    }
                    GoogleMap(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        cameraPositionState = cameraState,
                        uiSettings = MapUiSettings(zoomControlsEnabled = false, scrollGesturesEnabled = false)
                    ) {
                        Marker(
                            state = MarkerState(LatLng(machine.latitude, machine.longitude)),
                            title = machine.name.ifEmpty { machine.type }
                        )
                    }
                }
            }

            // Book button
            if (machine.isAvailable) {
                Button(
                    onClick = onBookClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(Radius.md),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Book Now",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(Radius.md)
                ) {
                    Text("Currently Unavailable", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PriceItem(label: String, value: String) {
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

@Composable
private fun InfoRow(icon: String, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(text = icon, fontSize = 16.sp, modifier = Modifier.width(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingBottomSheet(
    machine: Machine,
    userProfile: UserProfile?,
    totalPrice: Double,
    requestState: UiState<String>,
    onDismiss: () -> Unit,
    onPriceCalculate: (Double, Double, Int, RentalType) -> Unit,
    onSendRequest: (Int, Long, RentalType) -> Unit
) {
    var durationHours by remember { mutableIntStateOf(4) }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var rentalType by remember { mutableStateOf(RentalType.HOURLY) }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)

    LaunchedEffect(durationHours, rentalType) {
        onPriceCalculate(machine.hourlyRate, machine.dailyRate, durationHours, rentalType)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        shape = RoundedCornerShape(topStart = Radius.xl, topEnd = Radius.xl)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Book Machine",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Rental type toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RentalType.values().forEach { type ->
                    val isSelected = rentalType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { rentalType = type },
                        label = { Text(type.displayName(), style = MaterialTheme.typography.labelMedium) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Date picker trigger
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.md)
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start: ${dateFormat.format(Date(selectedDate))}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { selectedDate = it }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            // Duration slider (only for hourly)
            if (rentalType == RentalType.HOURLY) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Duration", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text(
                            "$durationHours hours",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                    }
                    Slider(
                        value = durationHours.toFloat(),
                        onValueChange = { durationHours = it.roundToInt() },
                        valueRange = 1f..23f,
                        steps = 21,
                        colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
                    )
                }
            }

            // Price display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.md))
                    .background(PrimaryContainer)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Amount", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text(
                    text = "₹${totalPrice.toInt()}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            val isLoading = requestState is UiState.Loading

            Button(
                onClick = {
                    onSendRequest(
                        if (rentalType == RentalType.DAILY) 24 else durationHours,
                        selectedDate,
                        rentalType
                    )
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
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Send Booking Request",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
