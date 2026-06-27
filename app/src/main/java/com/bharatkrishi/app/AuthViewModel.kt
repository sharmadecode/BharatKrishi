package com.bharatkrishi.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> = _user

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        _user.value = auth.currentUser
        auth.addAuthStateListener { firebaseAuth ->
            _user.value = firebaseAuth.currentUser
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isStrongPassword(pass: String): Boolean {
        if (pass.length < 8) return false
        val hasLetter = pass.any { it.isLetter() }
        val hasDigit = pass.any { it.isDigit() }
        return hasLetter && hasDigit
    }

    fun login(email: String, pass: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || pass.isEmpty()) {
            _authState.value = AuthState.Error("Email and Password cannot be empty")
            return
        }
        if (!isValidEmail(trimmedEmail)) {
            _authState.value = AuthState.Error("Invalid email address format")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(trimmedEmail, pass)
            .addOnSuccessListener {
                _authState.value = AuthState.Success
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.message ?: "Login Failed")
            }
    }

    fun signup(email: String, pass: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || pass.isEmpty()) {
            _authState.value = AuthState.Error("Email and Password cannot be empty")
            return
        }
        if (!isValidEmail(trimmedEmail)) {
            _authState.value = AuthState.Error("Invalid email address format")
            return
        }
        if (!isStrongPassword(pass)) {
            _authState.value = AuthState.Error("Password must be at least 8 characters long and contain both letters and digits")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(trimmedEmail, pass)
            .addOnSuccessListener {
                _authState.value = AuthState.Success
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.message ?: "Signup Failed")
            }
    }

    fun logout() {
        auth.signOut()
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
