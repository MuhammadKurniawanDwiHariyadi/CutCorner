package com.example.cepstun.ui.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.core.widget.doAfterTextChanged
import com.example.cepstun.R
import com.example.cepstun.databinding.ActivitySignUpBinding
import com.example.cepstun.viewModel.SignUpViewModel
import com.example.cepstun.viewModel.ViewModelFactory
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    private lateinit var level: String

    private val viewModel: SignUpViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playAnimation()

        level = intent.getStringExtra(USER_LEVEL) ?: "Customer"

        with(binding){
            BRegister.backgroundTintList = ContextCompat.getColorStateList(this@SignUpActivity, R.color.gray2)

            ETEmail.doAfterTextChanged { text ->
                viewModel.email.value = text.toString()
            }

            ETPassword.doAfterTextChanged { text ->
                viewModel.password.value = text.toString()
            }

            ETPassword2.doAfterTextChanged { text ->
                viewModel.password2.value = text.toString()
            }

            viewModel.isFormValid.observe(this@SignUpActivity) { isValid ->
                BRegister.isEnabled = isValid
                BRegister.backgroundTintList =
                    ContextCompat.getColorStateList(this@SignUpActivity, if (isValid) R.color.brown else R.color.gray2)
            }

            BRegister.setOnClickListener {
                viewModel.registerEmailPassword(ETEmail.text.toString().trim(), ETPassword.text.toString().trim(), this@SignUpActivity, level)
            }

            MCVLoginGoogle.setOnClickListener{
                CoroutineScope(Dispatchers.Main).launch {
                    val intentSenderRequest = viewModel.signingGoogle()
                    activityResultLauncher.launch(intentSenderRequest)
                }
            }

            TVLogin.setOnClickListener {
                moveToLogin()
            }

            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    moveToLogin()
                }
            })
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.tittle_dialog_Loading))
        builder.setMessage(getString(R.string.desc_dialog_SignUp))
        builder.setCancelable(false)
        val dialog = builder.create()

        viewModel.showProgressDialog.observe(this){
            if (!it){
                dialog.dismiss()
            } else {
                dialog.show()
            }
        }

        viewModel.showToast.observe(this){
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }
    }

    private fun playAnimation() {
        val tittle = ObjectAnimator.ofFloat(binding.TVLoginTittle, View.ALPHA, 1f).setDuration(500)
        val email = ObjectAnimator.ofFloat(binding.TVTittleEmail, View.ALPHA, 1f).setDuration(500)
        val etEmail = ObjectAnimator.ofFloat(binding.ETEmail, View.ALPHA, 1f).setDuration(500)
        val password = ObjectAnimator.ofFloat(binding.TVTittlePassword, View.ALPHA, 1f).setDuration(500)
        val etPassword = ObjectAnimator.ofFloat(binding.ETPassword, View.ALPHA, 1f).setDuration(500)
        val password2 = ObjectAnimator.ofFloat(binding.TVTittlePassword2, View.ALPHA, 1f).setDuration(500)
        val etPassword2 = ObjectAnimator.ofFloat(binding.ETPassword2, View.ALPHA, 1f).setDuration(500)


        val together2 = AnimatorSet().apply {
            playTogether(email, password, password2)
        }

        val together3 = AnimatorSet().apply {
            playTogether(etEmail, etPassword, etPassword2)
        }

        AnimatorSet().apply {
            playSequentially(tittle, together2, together3)
            start()
        }
    }

    private fun moveToLogin() {
        val optionsCompat: ActivityOptionsCompat =
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                this@SignUpActivity,
                Pair(binding.IVLogo2, "logo2"),
                Pair(binding.BRegister, "button")
            )

        Intent(this@SignUpActivity , LoginActivity::class.java).also { intent ->
            startActivity(intent, optionsCompat.toBundle())
        }
    }

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ){ result ->
        if (result.resultCode == RESULT_OK){
            CoroutineScope(Dispatchers.Main).launch {
                try{
                    viewModel.loginWithGoogle(this@SignUpActivity, result, level)
                } catch (e: ApiException){
                    if (e.statusCode == 16) {
                        // User has been temporarily blocked due to too many canceled sign-in prompts
                        AlertDialog.Builder(this@SignUpActivity)
                            .setTitle(getString(R.string.tittle_dialogErr_Login))
                            .setMessage(getString(R.string.desc_dialogErr_Login))
                            .setPositiveButton(getString(R.string.button_dialogErr_Login), null)
                            .show()
                    } else {
                        Toast.makeText(this@SignUpActivity,
                            getString(R.string.error_with, e.printStackTrace()), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else if (result.resultCode == RESULT_CANCELED) {
            // Handle the case where the user cancelled the operation
            viewModel.showProgressDialog.value = false
        }
    }

    companion object{
        const val USER_LEVEL: String = "user_level"
    }
}