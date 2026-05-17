package com.nammayantra.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammayantra.app.data.BookingRequest
import com.nammayantra.app.data.Machine
import com.nammayantra.app.data.RentalType
import com.nammayantra.app.data.RequestStatus
import com.nammayantra.app.data.UiState
import com.nammayantra.app.data.repo.FirebaseRepo
import com.nammayantra.app.utils.PriceCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BookingViewModel : ViewModel() {

    private val repo = FirebaseRepo.getInstance()

    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice = _totalPrice.asStateFlow()

    private val _requestState = MutableStateFlow<UiState<String>>(UiState.Empty)
    val requestState = _requestState.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val updateState = _updateState.asStateFlow()

    // Farmer's bookings
    fun getUserBookings(userId: String): Flow<List<BookingRequest>> =
        repo.getUserRequests(userId)

    // Owner's incoming requests
    fun getOwnerBookings(ownerId: String): Flow<List<BookingRequest>> =
        repo.getOwnerRequests(ownerId)

    fun calculatePrice(hourlyRate: Double, dailyRate: Double, durationHours: Int, rentalType: RentalType) {
        _totalPrice.value = PriceCalculator.calculatePrice(hourlyRate, dailyRate, durationHours, rentalType)
    }

    fun sendRequest(
        machine: Machine,
        durationHours: Int,
        startDate: Long,
        rentalType: RentalType,
        requesterName: String,
        requesterPhone: String
    ) {
        viewModelScope.launch {
            val userId = repo.currentUserId
            if (userId == null) {
                _requestState.value = UiState.Error("Please sign in to book")
                return@launch
            }

            _requestState.value = UiState.Loading

            val request = BookingRequest(
                machineId = machine.id,
                machineName = machine.name.ifEmpty { machine.type },
                machineType = machine.type,
                machineImageUrl = machine.imageUrl,
                ownerId = machine.ownerId,
                ownerName = machine.ownerName,
                requesterId = userId,
                requesterName = requesterName,
                requesterPhone = requesterPhone,
                startDate = startDate,
                durationHours = durationHours,
                rentalType = rentalType.name,
                totalPrice = _totalPrice.value,
                status = RequestStatus.PENDING.name
            )

            val result = repo.sendBookingRequest(request)
            _requestState.value = if (result.isSuccess) {
                UiState.Success(result.getOrDefault(""))
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Failed to send request")
            }
        }
    }

    fun acceptBooking(requestId: String, machineId: String) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            val result = repo.updateBookingStatus(requestId, RequestStatus.ACCEPTED, machineId)
            _updateState.value = if (result.isSuccess) UiState.Success(Unit)
            else UiState.Error(result.exceptionOrNull()?.message ?: "Failed")
        }
    }

    fun declineBooking(requestId: String, machineId: String) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            val result = repo.updateBookingStatus(requestId, RequestStatus.DECLINED, machineId)
            _updateState.value = if (result.isSuccess) UiState.Success(Unit)
            else UiState.Error(result.exceptionOrNull()?.message ?: "Failed")
        }
    }

    fun completeBooking(requestId: String, machineId: String) {
        viewModelScope.launch {
            val result = repo.updateBookingStatus(requestId, RequestStatus.COMPLETED, machineId)
        }
    }

    fun resetRequestState() {
        _requestState.value = UiState.Empty
    }

    fun resetUpdateState() {
        _updateState.value = UiState.Empty
    }
}
