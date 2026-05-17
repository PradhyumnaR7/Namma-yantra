package com.nammayantra.app.data

import kotlinx.serialization.Serializable

@Serializable
data class BookingRequest(
    val id: String = "",
    val machineId: String = "",
    val machineName: String = "",
    val machineType: String = "",
    val machineImageUrl: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val requesterPhone: String = "",
    val startDate: Long = 0L,
    val durationHours: Int = 0,
    val rentalType: String = RentalType.HOURLY.name, // "HOURLY" or "DAILY"
    val totalPrice: Double = 0.0,
    val status: String = RequestStatus.PENDING.name,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val notes: String = ""
) {
    fun getStatus(): RequestStatus = try {
        RequestStatus.valueOf(status)
    } catch (e: Exception) {
        RequestStatus.PENDING
    }

    fun getRentalType(): RentalType = try {
        RentalType.valueOf(rentalType)
    } catch (e: Exception) {
        RentalType.HOURLY
    }
}

enum class RequestStatus {
    PENDING, ACCEPTED, DECLINED, COMPLETED;

    fun displayName(): String = when (this) {
        PENDING -> "Pending"
        ACCEPTED -> "Accepted"
        DECLINED -> "Declined"
        COMPLETED -> "Completed"
    }

    fun colorHex(): String = when (this) {
        PENDING -> "#F59E0B"
        ACCEPTED -> "#10B981"
        DECLINED -> "#EF4444"
        COMPLETED -> "#6B7280"
    }
}

enum class RentalType {
    HOURLY, DAILY;

    fun displayName(): String = when (this) {
        HOURLY -> "Hourly"
        DAILY -> "Daily"
    }
}
