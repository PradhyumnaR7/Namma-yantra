package com.nammayantra.app.data.repo

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.nammayantra.app.data.BookingRequest
import com.nammayantra.app.data.Machine
import com.nammayantra.app.data.RequestStatus
import com.nammayantra.app.data.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseRepo {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val currentUserId: String? get() = auth.currentUser?.uid
    val isLoggedIn: Boolean get() = auth.currentUser != null

    private val rtdb = FirebaseDatabase.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // RTDB refs (real-time availability)
    private val machinesRef = rtdb.reference.child("machines")
    private val requestsRef = rtdb.reference.child("requests")

    // Firestore collections (structured data)
    private val usersCol = firestore.collection("users")
    private val machinesCol = firestore.collection("machines")
    private val bookingsCol = firestore.collection("bookings")

    // ─── AUTH ────────────────────────────────────────────────────────────────

    suspend fun signInAnonymously(): Boolean = suspendCoroutine { cont ->
        auth.signInAnonymously().addOnCompleteListener { task ->
            cont.resume(task.isSuccessful)
        }
    }

    suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): Result<String> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    // ─── USER PROFILE ────────────────────────────────────────────────────────

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            usersCol.document(profile.uid).set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): Result<UserProfile?> {
        return try {
            val doc = usersCol.document(uid).get().await()
            Result.success(doc.toObject(UserProfile::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserProfileFlow(uid: String): Flow<UserProfile?> = callbackFlow {
        val listener = usersCol.document(uid).addSnapshotListener { snap, _ ->
            trySend(snap?.toObject(UserProfile::class.java))
        }
        awaitClose { listener.remove() }
    }

    // ─── MACHINES (RTDB — real-time) ─────────────────────────────────────────

    fun getMachines(): Flow<List<Machine>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val machines = snapshot.children.mapNotNull {
                    it.getValue(Machine::class.java)?.also { m ->
                        // Ensure id is set from key if empty
                        if (m.id.isEmpty()) {
                            it.key?.let { key ->
                                // Return copy with key as id
                            }
                        }
                    }
                }
                trySend(machines)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        machinesRef.addValueEventListener(listener)
        awaitClose { machinesRef.removeEventListener(listener) }
    }

    suspend fun getMachineById(machineId: String): Result<Machine?> {
        return try {
            val snapshot = machinesRef.child(machineId).get().await()
            Result.success(snapshot.getValue(Machine::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addMachine(machine: Machine): Result<String> {
        return try {
            val key = machinesRef.push().key ?: return Result.failure(Exception("Failed to generate key"))
            val machineWithId = machine.copy(id = key)
            machinesRef.child(key).setValue(machineWithId).await()
            Result.success(key)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMachine(machine: Machine): Result<Unit> {
        return try {
            machinesRef.child(machine.id).setValue(machine).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMachine(machineId: String): Result<Unit> {
        return try {
            machinesRef.child(machineId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMachineAvailability(machineId: String, isAvailable: Boolean): Result<Unit> {
        return try {
            machinesRef.child(machineId).child("isAvailable").setValue(isAvailable).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getOwnerMachines(ownerId: String): Flow<List<Machine>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val machines = snapshot.children
                    .mapNotNull { it.getValue(Machine::class.java) }
                    .filter { it.ownerId == ownerId }
                trySend(machines)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        machinesRef.orderByChild("ownerId").equalTo(ownerId).addValueEventListener(listener)
        awaitClose { machinesRef.removeEventListener(listener) }
    }

    // ─── BOOKING REQUESTS (RTDB) ─────────────────────────────────────────────

    fun getUserRequests(userId: String): Flow<List<BookingRequest>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requests = snapshot.children
                    .mapNotNull { it.getValue(BookingRequest::class.java) }
                    .filter { it.requesterId == userId }
                    .sortedByDescending { it.createdAt }
                trySend(requests)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        requestsRef.orderByChild("requesterId").equalTo(userId).addValueEventListener(listener)
        awaitClose { requestsRef.removeEventListener(listener) }
    }

    fun getOwnerRequests(ownerId: String): Flow<List<BookingRequest>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requests = snapshot.children
                    .mapNotNull { it.getValue(BookingRequest::class.java) }
                    .filter { it.ownerId == ownerId }
                    .sortedByDescending { it.createdAt }
                trySend(requests)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        requestsRef.orderByChild("ownerId").equalTo(ownerId).addValueEventListener(listener)
        awaitClose { requestsRef.removeEventListener(listener) }
    }

    suspend fun sendBookingRequest(request: BookingRequest): Result<String> {
        return try {
            val key = requestsRef.push().key
                ?: return Result.failure(Exception("Failed to generate key"))
            val requestWithId = request.copy(
                id = key,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            requestsRef.child(key).setValue(requestWithId).await()
            Result.success(key)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBookingStatus(
        requestId: String,
        status: RequestStatus,
        machineId: String? = null
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "status" to status.name,
                "updatedAt" to System.currentTimeMillis()
            )
            requestsRef.child(requestId).updateChildren(updates).await()

            // If accepted, mark machine as unavailable
            if (status == RequestStatus.ACCEPTED && machineId != null) {
                updateMachineAvailability(machineId, false)
            }
            // If completed/declined, mark machine as available again
            if ((status == RequestStatus.COMPLETED || status == RequestStatus.DECLINED) && machineId != null) {
                updateMachineAvailability(machineId, true)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── IMAGE UPLOAD (Firebase Storage) ─────────────────────────────────────

    suspend fun uploadMachineImage(machineId: String, imageUri: Uri): Result<String> {
        return try {
            val ref = storage.reference
                .child("machines/$machineId/${System.currentTimeMillis()}.jpg")
            ref.putFile(imageUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfileImage(userId: String, imageUri: Uri): Result<String> {
        return try {
            val ref = storage.reference.child("profiles/$userId/avatar.jpg")
            ref.putFile(imageUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: FirebaseRepo? = null

        fun getInstance(): FirebaseRepo {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseRepo().also { INSTANCE = it }
            }
        }
    }
}
