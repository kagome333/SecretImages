package com.example.secretimages

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.secretimages.databinding.ActivitySetupPasswordBinding

class SetupPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSetPassword.setOnClickListener {
            val password = binding.etPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()

            if (password.length < 4) {
                Toast.makeText(this, "パスワードは4文字以上にしてください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirm) {
                Toast.makeText(this, "パスワードが一致しません", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            PasswordManager.setPassword(this, password)
            Toast.makeText(this, "パスワードを設定しました", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
