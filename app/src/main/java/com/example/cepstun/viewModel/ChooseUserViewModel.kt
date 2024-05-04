package com.example.cepstun.viewModel

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cepstun.R
import com.example.cepstun.ui.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class ChooseUserViewModel(private val auth: FirebaseAuth, private val database: FirebaseDatabase) :
    ViewModel() {

    private val _isUserSelected = MutableLiveData<Boolean>()
    val isUserSelected: MutableLiveData<Boolean> = _isUserSelected

    init {
        _isUserSelected.value = false
    }

    fun selectedUser(){
        _isUserSelected.value = true
    }

    fun uploadToDatabase(userType: String, context: Context, idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = task.result?.user
                if (user != null) {
                    database.getReference("users").child(user.uid).get()
                        .addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                val userMap = mapOf(
                                    "FName" to user.displayName.toString(),
                                    "Level" to userType,
                                    "Photo" to user.photoUrl.toString()
                                )
                                database.getReference("users").child(user.uid).setValue(userMap)
                                    .addOnSuccessListener {
                                        Intent(context, MainActivity::class.java). also {
                                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            context.startActivity(it)
                                        }
                                        Toast.makeText(context,
                                            context.getString(R.string.saved), Toast.LENGTH_LONG).show()
                                    }.addOnFailureListener { e ->
                                        Toast.makeText(context, e.printStackTrace().toString(), Toast.LENGTH_LONG).show()
                                    }
                            } else {
                                Toast.makeText(context,
                                    context.getString(R.string.fail_save), Toast.LENGTH_LONG).show()
                            }
                        }
                }
            } else {
                Toast.makeText(context, context.getString(R.string.fail_connect), Toast.LENGTH_LONG).show()
            }
        }
    }

}