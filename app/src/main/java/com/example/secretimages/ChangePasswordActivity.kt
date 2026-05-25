package com.example.secretimages

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.secretimages.databinding.ActivityChangePasswordBinding

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "パスワード変更"
        }

        binding.btnChangePassword.setOnClickListener {
            val current = binding.etCurrentPassword.text.toString()
            val newPass = binding.etNewPassword.text.toString()
            val confirm = binding.etConfirmNewPassword.text.toString()

            if (!PasswordManager.verifyPassword(this, current)) {
                Toast.makeText(this, "現在のパスワードが違います", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPass.length < 4) {
                Toast.makeText(this, "パスワードは4文字以上にしてください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPass != confirm) {
                Toast.makeText(this, "新しいパスワードが一致しません", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            PasswordManager.setPassword(this, newPass)
            Toast.makeText(this, "パスワードを変更しました", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
