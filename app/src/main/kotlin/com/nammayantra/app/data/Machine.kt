package com.nammayantra.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Machine(
    val id: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val ownerPhone: String = "",
    val name: String = "",          // e.g. "Mahindra 575 DI"
    val type: String = "",          // Tractor, Harvester, Sprayer, Power Tiller
    val description: String = "",
    val hourlyRate: Double = 0.0,
    val dailyRate: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val lastServiceDate: Long = 0L,
    val conditionRating: Double = 0.0, // 0-5
    val isAvailable: Boolean = true,
    val imageUrl: String = "",
    val imageUrls: List<String> = emptyList(),
    val village: String = "",
    val district: String = "",
    val createdAt: Long = 0L
) {
    // Computed at runtime — not stored in Firebase
    @kotlinx.serialization.Transient
    var distanceKm: Double = 0.0
}

object MachineType {
    const val TRACTOR = "Tractor"
    const val HARVESTER = "Harvester"
    const val SPRAYER = "Sprayer"
    const val POWER_TILLER = "Power Tiller"

    val all = listOf(TRACTOR, HARVESTER, SPRAYER, POWER_TILLER)

    fun emoji(type: String): String = when (type) {
        TRACTOR -> "🚜"
        HARVESTER -> "🌾"
        SPRAYER -> "💧"
        POWER_TILLER -> "⚙️"
        else -> "🚜"
    }
}
