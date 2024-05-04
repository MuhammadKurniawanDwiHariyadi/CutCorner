package com.example.cepstun.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cepstun.databinding.ActivityOnBoardingBinding

class OnBoardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnBoardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            BLogin.setOnClickListener {
                Intent(this@OnBoardingActivity, LoginActivity::class.java).also { intent ->
                    startActivity(intent)
                }
            }
            BSignUp.setOnClickListener {
                Intent(this@OnBoardingActivity, ChooseUserActivity::class.java).also { intent ->
                    startActivity(intent)
                }
            }
        }
    }
}