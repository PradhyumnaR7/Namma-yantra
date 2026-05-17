package com.nammayantra.app.utils

import com.nammayantra.app.data.RentalType

object PriceCalculator {

    fun calculatePrice(
        hourlyRate: Double,
        dailyRate: Double,
        durationHours: Int,
        rentalType: RentalType = RentalType.HOURLY
    ): Double {
        return when {
            rentalType == RentalType.DAILY -> dailyRate
            durationHours >= 24 -> dailyRate
            else -> hourlyRate * durationHours
        }
    }

    fun formatPrice(amount: Double): String {
        return when {
            amount >= 100000 -> "₹${String.format("%.1f", amount / 100000)}L"
            amount >= 1000 -> "₹${String.format("%.1f", amount / 1000)}K"
            else -> "₹${amount.toInt()}"
        }
    }

    fun formatPriceExact(amount: Double): String = "₹${amount.toInt()}"
}
