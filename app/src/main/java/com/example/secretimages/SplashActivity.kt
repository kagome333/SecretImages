package com.example.secretimages

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = if (PasswordManager.isPasswordSet(this)) {
            Intent(this, UnlockActivity::class.java)
        } else {
            Intent(this, SetupPasswordActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}
