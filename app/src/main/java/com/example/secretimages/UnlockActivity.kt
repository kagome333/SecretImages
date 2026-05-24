package com.example.secretimages

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.secretimages.databinding.ActivityUnlockBinding

class UnlockActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUnlockBinding
    private var failCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnUnlock.setOnClickListener { tryUnlock() }

        binding.etPassword.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                tryUnlock()
                true
            } else false
        }
    }

    private fun tryUnlock() {
        val password = binding.etPassword.text.toString()
        if (PasswordManager.verifyPassword(this, password)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            failCount++
            binding.etPassword.text?.clear()
            binding.tvFailCount.text = if (failCount > 0) "認証失敗: ${failCount}回" else ""
            Toast.makeText(this, "パスワードが違います", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}
