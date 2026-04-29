package com.abdallah.taskvault.domain.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<FirebaseUser?>
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser>
    suspend fun signOut()
    fun isSignedIn(): Boolean
    fun getCurrentUserId(): String?
}
