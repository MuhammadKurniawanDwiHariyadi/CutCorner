package com.example.cepstun.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast

//fun Context.moveActivity(java: Class<*>) {
//    startActivity(Intent(this, java))
//}

fun Context.showToast(message: String, duration: Int){
    Toast.makeText(this, message, duration).show()
}
