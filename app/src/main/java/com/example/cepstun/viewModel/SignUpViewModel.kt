package com.example.cepstun.viewModel

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.cepstun.BuildConfig
import com.example.cepstun.R
import com.example.cepstun.ui.activity.LoginActivity
import com.example.cepstun.ui.activity.MainActivity
import com.example.cepstun.ui.activity.RegisterBarberActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

@Suppress("DEPRECATION")
class SignUpViewModel(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val oneTapClient: SignInClient
) : ViewModel(){

    private val _showProgressDialog = MutableLiveData<Boolean>()
    val showProgressDialog: MutableLiveData<Boolean> = _showProgressDialog

    private val _showToast = MutableLiveData<String>()
    val showToast: MutableLiveData<String> = _showToast

    // for validate TextView
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val password2 = MutableLiveData<String>()

    private val isEmailValid = email.map { it?.isNotEmpty() == true }
    private val isPasswordValid = password.map { it?.isNotEmpty() == true }
    private val isPasswordValid2 = password2.map { it?.isNotEmpty() == true && it == password.value }

    val isFormValid = MediatorLiveData<Boolean>().apply {
        addSource(isEmailValid) { value = validateForm() }
        addSource(isPasswordValid) { value = validateForm() }
        addSource(isPasswordValid2) { value = validateForm() }
    }

    private fun validateForm(): Boolean {
        return isEmailValid.value == true && isPasswordValid.value == true && isPasswordValid2.value == true
    }

    fun registerEmailPassword(email: String, password: String, context: Context, level: String) {
        _showProgressDialog.value = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.reload()
                    auth.currentUser?.sendEmailVerification()
                        ?.addOnSuccessListener {
                            _showProgressDialog.value = false
                            _showToast.value = context.getString(R.string.success_create_account)

                            // Save Data to database
                            val userID = auth.currentUser!!.uid
                            val userMap = mapOf(
                                "FName" to email,
                                "Level" to level
                            )
                            database.getReference("users").child(userID).setValue(userMap)
                                .addOnSuccessListener {
                                    if (level == "Customer"){
                                        // If the save to realtime database is successful, proceed to the LoginActivity
                                        Intent(context, LoginActivity::class.java).also {
                                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            context.startActivity(it)
                                            auth.signOut()
                                        }
                                    } else {
                                        Intent(context, RegisterBarberActivity::class.java).also {
                                            context.startActivity(it)
                                            auth.signOut()
                                        }
                                    }

                                }
                                .addOnFailureListener { e ->
                                    _showProgressDialog.value = false
                                    _showToast.value =
                                        context.getString(R.string.fail_save_data, e.message)
                                }
                        }
                        ?.addOnFailureListener { e ->
                            _showProgressDialog.value = false
                            _showToast.value =
                                context.getString(R.string.fail_send_verif, e.message)
                        }
                } else {
                    _showProgressDialog.value = false
                    _showToast.value =
                        context.getString(R.string.fail_create_account, task.exception?.message)
                }
            }
    }

    private var signInRequest: BeginSignInRequest =
        BeginSignInRequest.builder().setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder().setSupported(true)
                .setServerClientId(BuildConfig.DEFAULT_WEB_CLIENT_ID)
                .setFilterByAuthorizedAccounts(false).build()
        ).build()

    suspend fun signingGoogle(): IntentSenderRequest {
        _showProgressDialog.value = true
        val result = oneTapClient.beginSignIn(signInRequest).await()
        return IntentSenderRequest.Builder(result.pendingIntent).build()
    }

    fun loginWithGoogle(context: Context, result: ActivityResult, level: String) {
        val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
        val idToken = credential.googleIdToken
        if (idToken != null) {
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(firebaseCredential).addOnCompleteListener {task->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        // Cek whether the user is new and the user data does not yet exist in the Realtime Database
                        database.getReference("users").child(user.uid).get()
                            .addOnCompleteListener { task2 ->
                                if (task2.isSuccessful) {
                                    val result2 = task2.result
                                    if (result2 != null && !result2.exists()) {
                                        _showProgressDialog.value = false
                                        // New user and user data do not yet exist in the Realtime Database
                                        val userMap = mapOf(
                                            "FName" to user.displayName.toString(),
                                            "Level" to level,
                                            "Photo" to user.photoUrl.toString()
                                        )
                                        database.getReference("users").child(user.uid).setValue(userMap)
                                            .addOnSuccessListener {
                                                Toast.makeText(context,
                                                    context.getString(R.string.saved), Toast.LENGTH_LONG).show()
                                                if (level == "Customer"){
                                                    Intent(context, MainActivity::class.java). also {
                                                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                        context.startActivity(it)
                                                    }
                                                } else {
                                                    Intent(context, RegisterBarberActivity::class.java).also {
                                                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                        context.startActivity(it)
                                                    }
                                                }


                                            }.addOnFailureListener { e ->
                                                Toast.makeText(context, e.printStackTrace().toString(), Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            Intent(context, MainActivity::class.java). also {
                                                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                context.startActivity(it)
                                            }
                                            Toast.makeText(context,
                                                context.getString(R.string.already_have_an_account), Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    _showProgressDialog.value = false
                                    _showToast.value = context.getString(R.string.fail_connect_data)
                                }
                            }
                    }
                }
            }
        }
    }
}