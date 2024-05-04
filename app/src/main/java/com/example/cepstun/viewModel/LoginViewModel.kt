@file:Suppress("DEPRECATION")

package com.example.cepstun.viewModel

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.cepstun.BuildConfig
import com.example.cepstun.R
import com.example.cepstun.ui.activity.ChooseUserActivity
import com.example.cepstun.ui.activity.LoginActivity
import com.example.cepstun.ui.activity.MainActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

@Suppress("DEPRECATION")
class LoginViewModel(
    private val auth: FirebaseAuth,
    private var oneTapClient: SignInClient,
    private val database: FirebaseDatabase
) : ViewModel() {

    private val _showProgressDialog = MutableLiveData<Boolean>()
    val showProgressDialog: MutableLiveData<Boolean> = _showProgressDialog

    private val _showToast = MutableLiveData<String>()
    val showToast: MutableLiveData<String> = _showToast

    // for validate TextView
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    private val isEmailValid = email.map { it?.isNotEmpty() == true }
    private val isPasswordValid = password.map { it?.isNotEmpty() == true }


    val isFormValid = MediatorLiveData<Boolean>().apply {
        addSource(isEmailValid) { value = validateForm() }
        addSource(isPasswordValid) { value = validateForm() }
    }


    private fun validateForm(): Boolean {
        return isEmailValid.value == true && isPasswordValid.value == true
    }

    fun loginEmailPassword(email: String, password: String, context: Context) {
        _showProgressDialog.value = true
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val verification = auth.currentUser?.isEmailVerified
                if (verification == true) {
                    //  Next Activity
                    _showToast.value = context.getString(R.string.login_success)
                    Intent(context, MainActivity::class.java).also { intent ->
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }
                } else {
                    _showToast.value = context.getString(R.string.not_verified)
                    auth.signOut()
                }
            } else {
                _showToast.value = task.exception?.message.toString()
                auth.signOut()
            }
            _showProgressDialog.value = false
            auth.signOut()
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


    fun loginWithGoogle(context: Context, result: ActivityResult) {
        val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
        val idToken = credential.googleIdToken
        if (idToken != null) {
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(firebaseCredential).addOnCompleteListener {
                if (it.isSuccessful) {
                    val user = it.result?.user
                    if (user != null) {
                        // Cek whether the user is new and the user data does not yet exist in the Realtime Database
                        database.getReference("users").child(user.uid).get()
                            .addOnCompleteListener { task2 ->
                                if (task2.isSuccessful) {
                                    val result2 = task2.result
                                    if (result2 != null && !result2.exists()) {
                                        _showProgressDialog.value = false
                                        // New user and user data do not yet exist in the Realtime Database
                                        Intent(
                                            context, ChooseUserActivity::class.java
                                        ).also { intent ->
                                            intent.flags =
                                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            intent.putExtra(
                                                ChooseUserActivity.ID_TOKEN, idToken
                                            )
                                            context.startActivity(intent)
                                        }
                                    } else {
                                        _showProgressDialog.value = false
                                        // The user already exists in the Realtime Database
                                        Intent(
                                            context, MainActivity::class.java
                                        ).also { intent ->
                                            intent.flags =
                                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            context.startActivity(intent)
                                        }
                                    }
                                } else {
                                    _showToast.value = context.getString(R.string.fail_connect_data)
                                }
                            }
                    }
                }
            }
        }
    }


    fun resendEmail(email: String, password: String, context: Context) {

        if (email.isEmpty() && password.isEmpty()) {
            _showToast.value = context.getString(R.string.password_email_empty)

        } else {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    currentUser?.reload()
                    if (!currentUser?.isEmailVerified!!) {
                        currentUser.sendEmailVerification()
                        _showToast.value = context.getString(R.string.send_email_verif)
                        auth.signOut()
                    } else {
                        _showToast.value = context.getString(R.string.verified_email)
                        auth.signOut()
                    }
                } else {
                    _showToast.value =
                        context.getString(R.string.fail_send_email, task.exception?.message)
                    auth.signOut()
                }
            }
        }
    }

    fun forgetPassword(email: String, context: Context) {
        if (email.isEmpty()) {
            _showToast.value = context.getString(R.string.email_empty)
        } else {
            auth.sendPasswordResetEmail(email).addOnCompleteListener { reset ->
                if (reset.isSuccessful) {
                    Intent(context, LoginActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(it)
                        _showToast.value = context.getString(R.string.email_reset_password)
                    }
                } else {
                    _showToast.value = reset.exception?.message.toString()
                }
            }
        }
    }

}