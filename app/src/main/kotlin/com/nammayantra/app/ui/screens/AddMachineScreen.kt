package com.nammayantra.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nammayantra.app.data.Machine
import com.nammayantra.app.data.MachineType
import com.nammayantra.app.data.UiState
import com.nammayantra.app.data.UserProfile
import com.nammayantra.app.ui.theme.*
import com.nammayantra.app.viewmodel.MachineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMachineScreen(
    navController: NavController,
    userProfile: UserProfile?,
    modifier: Modifier = Modifier,
    viewModel: MachineViewModel = viewModel()
) {
    val addState by viewModel.addMachineState.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(MachineType.TRACTOR) }
    var description by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("") }
    var dailyRate by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var conditionRating by remember { mutableFloatStateOf(4f) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    LaunchedEffect(addState) {
        when (addState) {
            is UiState.Success -> {
                viewModel.resetAddMachineState()
                navController.popBackStack()
            }
            is UiState.Error -> {
                errorMessage = (addState as UiState.Error).message
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Machine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(Radius.lg))
                    .background(SurfaceVariant)
                    .border(2.dp, Divider, RoundedCornerShape(Radius.lg))
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Machine image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📷", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap to add photo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Machine name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Machine Name (e.g. Mahindra 575 DI)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.md),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, focusedLabelColor = Primary)
            )

            // Type dropdown
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = it }
            ) {
                OutlinedTextField(
                    value = "${MachineType.emoji(selectedType)} $selectedType",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Machine Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(Radius.md),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, focusedLabelColor = Primary)
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    MachineType.all.forEach { type ->
                        DropdownMenuItem(
                            text = { Text("${MachineType.emoji(type)} $type") },
                            onClick = {
                                selectedType = type
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(Radius.md),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, focusedLabelColor = Primary)
            )

            // Pricing row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = hourlyRate,
                    onValueChange = { hourlyRate = it },
                    label = { Text("Hourly Rate (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Radius.md),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, focusedLabelColor = Primary)
                )
                OutlinedTextField(
                    value = dailyRate,
                    onValueChange = { dailyRate = it },
                    label = { Text("Daily Rate (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Radius.md),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, focusedLabelColor = Primary)
                )
            }

            // Location
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = village,
                    onValueChange = { village = it },
                    label = { Text("Village") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Radius.md),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, focusedLabelColor = Primary)
                )
                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text("District") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Radius.md),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, focusedLabelColor = Primary)
                )
            }

            // Condition rating
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Condition Rating", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text(
                        "${"%.1f".format(conditionRating)}/5 ⭐",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                }
                Slider(
                    value = conditionRating,
                    onValueChange = { conditionRating = it },
                    valueRange = 1f..5f,
                    steps = 7,
                    colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
                )
            }

            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = Error, style = MaterialTheme.typography.bodySmall)
            }

            val isLoading = addState is UiState.Loading

            Button(
                onClick = {
                    errorMessage = ""
                    val hRate = hourlyRate.toDoubleOrNull()
                    val dRate = dailyRate.toDoubleOrNull()
                    when {
                        name.trim().isEmpty() -> errorMessage = "Enter machine name"
                        hRate == null || hRate <= 0 -> errorMessage = "Enter valid hourly rate"
                        dRate == null || dRate <= 0 -> errorMessage = "Enter valid daily rate"
                        else -> {
                            val machine = Machine(
                                ownerId = userProfile?.uid ?: "",
                                ownerName = userProfile?.name ?: "",
                                ownerPhone = userProfile?.phone ?: "",
                                name = name.trim(),
                                type = selectedType,
                                description = description.trim(),
                                hourlyRate = hRate,
                                dailyRate = dRate,
                                village = village.trim(),
                                district = district.trim(),
                                conditionRating = conditionRating.toDouble(),
                                isAvailable = true,
                                createdAt = System.currentTimeMillis()
                            )
                            viewModel.addMachine(machine, imageUri)
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
                        "Add Machine",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
