package com.nammayantra.app.data

import kotlinx.serialization.Serializable

enum class UserType { FARMER, OWNER }

@Serializable
data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val userType: String = UserType.FARMER.name, // "FARMER" or "OWNER"
    val village: String = "",
    val district: String = "",
    val state: String = "",
    val profileImageUrl: String = "",
    val fcmToken: String = "",
    val createdAt: Long = 0L
) {
    fun getUserType(): UserType = try {
        UserType.valueOf(userType)
    } catch (e: Exception) {
        UserType.FARMER
    }
}
