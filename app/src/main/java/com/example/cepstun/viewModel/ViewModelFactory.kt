package com.example.cepstun.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cepstun.di.Injection
import com.example.cepstun.di.Injection.provideFirebaseOneTap
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ViewModelFactory private constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val oneTapClient: SignInClient
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(auth, oneTapClient, database) as T
            }
            modelClass.isAssignableFrom(ChooseUserViewModel::class.java) -> {
                ChooseUserViewModel(auth, database) as T
            }
            modelClass.isAssignableFrom(SignUpViewModel::class.java) -> {
                SignUpViewModel(auth, database, oneTapClient) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name + "Check ViewModelFactory")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null
        @JvmStatic
        fun getInstance(context: Context): ViewModelFactory {
            if (INSTANCE == null) {
                synchronized(ViewModelFactory::class.java) {
                    INSTANCE = ViewModelFactory(Injection.provideFirebaseAuth(), Injection.provideFirebaseDatabase(), provideFirebaseOneTap(context))
                }
            }
            return INSTANCE as ViewModelFactory
        }
    }
}