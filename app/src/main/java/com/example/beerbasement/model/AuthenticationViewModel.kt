package com.example.beerbasement.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.auth.FirebaseAuth

class AuthenticationViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    var user: FirebaseUser?  by mutableStateOf (auth.currentUser)
    var message by mutableStateOf("")

    fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user = auth.currentUser
                    message = ""
                } else {
                    user = null
                    message = task.exception?.message ?: "Unknown error"

                }
            }
    }

    fun signOut() {
        user = null
        auth.signOut()
    }

    fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user = auth.currentUser
                    message = ""
                } else {
                    user = null
                    message = task.exception?.message ?: "Unknown error"
                }
            }
    }


}

