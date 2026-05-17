package com.nammayantra.app.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.nammayantra.app.data.UiState
import com.nammayantra.app.data.UserProfile
import com.nammayantra.app.data.UserType
import com.nammayantra.app.data.repo.FirebaseRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {

    private val repo = FirebaseRepo.getInstance()

    private val _authState = MutableStateFlow<UiState<String>>(UiState.Empty)
    val authState = _authState.asStateFlow()

    private val _otpSentState = MutableStateFlow<UiState<String>>(UiState.Empty)
    val otpSentState = _otpSentState.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    private var verificationId: String = ""

    val isLoggedIn: Boolean get() = repo.isLoggedIn
    val currentUserId: String? get() = repo.currentUserId

    fun sendOtp(phoneNumber: String, activity: Activity) {
        _otpSentState.value = UiState.Loading

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-retrieval or instant verification
                signInWithCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _otpSentState.value = UiState.Error(e.message ?: "Verification failed")
            }

            override fun onCodeSent(
                vId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = vId
                _otpSentState.value = UiState.Success(vId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(repo.auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(otp: String) {
        if (verificationId.isEmpty()) {
            _authState.value = UiState.Error("Please request OTP first")
            return
        }
        _authState.value = UiState.Loading
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            val result = repo.signInWithPhoneCredential(credential)
            if (result.isSuccess) {
                _authState.value = UiState.Success(result.getOrDefault(""))
                loadUserProfile()
            } else {
                _authState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Sign in failed")
            }
        }
    }

    fun saveUserProfile(name: String, userType: UserType, phone: String) {
        val uid = repo.currentUserId ?: return
        viewModelScope.launch {
            val profile = UserProfile(
                uid = uid,
                name = name,
                phone = phone,
                userType = userType.name,
                createdAt = System.currentTimeMillis()
            )
            repo.saveUserProfile(profile)
            _userProfile.value = profile
        }
    }

    fun loadUserProfile() {
        val uid = repo.currentUserId ?: return
        viewModelScope.launch {
            val result = repo.getUserProfile(uid)
            if (result.isSuccess) {
                _userProfile.value = result.getOrNull()
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repo.signOut()
            _authState.value = UiState.Empty
            _userProfile.value = null
        }
    }

    fun resetState() {
        _authState.value = UiState.Empty
        _otpSentState.value = UiState.Empty
    }

    init {
        if (repo.isLoggedIn) {
            loadUserProfile()
        }
    }
}
