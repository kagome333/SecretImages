package com.example.secretimages

import android.content.Context
import java.security.MessageDigest

object PasswordManager {
    private const val PREFS_NAME = "secret_prefs"
    private const val KEY_PASSWORD_HASH = "password_hash"

    fun isPasswordSet(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.contains(KEY_PASSWORD_HASH)
    }

    fun setPassword(context: Context, password: String) {
        val hash = hashPassword(password)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_PASSWORD_HASH, hash).apply()
    }

    fun verifyPassword(context: Context, password: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString(KEY_PASSWORD_HASH, null) ?: return false
        return stored == hashPassword(password)
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
