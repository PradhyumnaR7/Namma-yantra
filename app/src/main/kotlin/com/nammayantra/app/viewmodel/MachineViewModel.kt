package com.nammayantra.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammayantra.app.data.Machine
import com.nammayantra.app.data.UiState
import com.nammayantra.app.data.repo.FirebaseRepo
import com.nammayantra.app.utils.DistanceCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MachineViewModel : ViewModel() {

    private val repo = FirebaseRepo.getInstance()

    // User location
    private val _userLat = MutableStateFlow(12.9716) // Default Bangalore
    private val _userLng = MutableStateFlow(77.5946)

    // Search & filter state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow("All")
    val selectedType = _selectedType.asStateFlow()

    private val _showAvailableOnly = MutableStateFlow(false)
    val showAvailableOnly = _showAvailableOnly.asStateFlow()

    private val _maxDistanceKm = MutableStateFlow(50.0)
    val maxDistanceKm = _maxDistanceKm.asStateFlow()

    // Selected machine for detail screen
    private val _selectedMachine = MutableStateFlow<UiState<Machine>>(UiState.Empty)
    val selectedMachine = _selectedMachine.asStateFlow()

    // Owner machine management
    private val _addMachineState = MutableStateFlow<UiState<String>>(UiState.Empty)
    val addMachineState = _addMachineState.asStateFlow()

    private val _rawMachines: Flow<List<Machine>> = repo.getMachines()

    private val _userLocation = _userLat.combine(_userLng) { lat, lng -> lat to lng }

    // Machines with distance computed and sorted by distance
    private val _machinesWithDistance: Flow<List<Machine>> =
        _rawMachines.combine(_userLocation) { machines, (lat, lng) ->
            machines.map { machine ->
                machine.also {
                    it.distanceKm = DistanceCalculator.calculateDistance(
                        lat, lng, machine.latitude, machine.longitude
                    )
                }
            }.sortedBy { it.distanceKm }
        }

    // Filtered machines exposed to UI
    val nearbyMachines: StateFlow<List<Machine>> = combine(
        _machinesWithDistance,
        _searchQuery,
        _selectedType,
        _showAvailableOnly,
        _maxDistanceKm
    ) { machines, query, type, availableOnly, maxDist ->
        machines.filter { machine ->
            val matchesQuery = query.isEmpty() ||
                    machine.type.contains(query, ignoreCase = true) ||
                    machine.name.contains(query, ignoreCase = true) ||
                    machine.ownerName.contains(query, ignoreCase = true) ||
                    machine.village.contains(query, ignoreCase = true)
            val matchesType = type == "All" || machine.type == type
            val matchesAvailability = !availableOnly || machine.isAvailable
            val matchesDistance = machine.distanceKm <= maxDist
            matchesQuery && matchesType && matchesAvailability && matchesDistance
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Owner's machines
    fun getOwnerMachines(ownerId: String): Flow<List<Machine>> =
        repo.getOwnerMachines(ownerId).map { machines ->
            machines.map { machine ->
                machine.also {
                    it.distanceKm = DistanceCalculator.calculateDistance(
                        _userLat.value, _userLng.value,
                        machine.latitude, machine.longitude
                    )
                }
            }
        }

    fun updateUserLocation(lat: Double, lng: Double) {
        _userLat.value = lat
        _userLng.value = lng
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedType(type: String) {
        _selectedType.value = type
    }

    fun setShowAvailableOnly(show: Boolean) {
        _showAvailableOnly.value = show
    }

    fun setMaxDistance(km: Double) {
        _maxDistanceKm.value = km
    }

    fun loadMachineById(machineId: String) {
        viewModelScope.launch {
            _selectedMachine.value = UiState.Loading
            val result = repo.getMachineById(machineId)
            _selectedMachine.value = if (result.isSuccess) {
                val machine = result.getOrNull()
                if (machine != null) {
                    machine.distanceKm = DistanceCalculator.calculateDistance(
                        _userLat.value, _userLng.value,
                        machine.latitude, machine.longitude
                    )
                    UiState.Success(machine)
                } else {
                    UiState.Error("Machine not found")
                }
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load machine")
            }
        }
    }

    fun addMachine(machine: Machine, imageUri: Uri? = null) {
        viewModelScope.launch {
            _addMachineState.value = UiState.Loading
            val result = repo.addMachine(machine)
            if (result.isSuccess) {
                val machineId = result.getOrDefault("")
                if (imageUri != null && machineId.isNotEmpty()) {
                    val uploadResult = repo.uploadMachineImage(machineId, imageUri)
                    if (uploadResult.isSuccess) {
                        val imageUrl = uploadResult.getOrDefault("")
                        val updatedMachine = machine.copy(
                            id = machineId,
                            imageUrl = imageUrl,
                            imageUrls = listOf(imageUrl)
                        )
                        repo.updateMachine(updatedMachine)
                    }
                }
                _addMachineState.value = UiState.Success(machineId)
            } else {
                _addMachineState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to add machine"
                )
            }
        }
    }

    fun updateMachine(machine: Machine) {
        viewModelScope.launch {
            repo.updateMachine(machine)
        }
    }

    fun deleteMachine(machineId: String) {
        viewModelScope.launch {
            repo.deleteMachine(machineId)
        }
    }

    fun resetAddMachineState() {
        _addMachineState.value = UiState.Empty
    }

    init {
        viewModelScope.launch {
            repo.signInAnonymously()
        }
    }
}
