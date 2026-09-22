package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.BuildConfig

class SecurePreferences(context: Context) {
    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "miniapp_forge_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences("miniapp_forge_prefs_fallback", Context.MODE_PRIVATE)
    }

    fun getGeminiApiKey(): String {
        val customKey = prefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
        if (customKey.isNotBlank()) return customKey
        return try {
            if (BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") {
                BuildConfig.GEMINI_API_KEY
            } else ""
        } catch (e: Exception) {
            ""
        }
    }

    fun setGeminiApiKey(key: String) {
        prefs.edit().putString(KEY_GEMINI_API_KEY, key.trim()).apply()
    }

    fun hasSeenDisclaimer(): Boolean {
        return prefs.getBoolean(KEY_DISCLAIMER_SEEN, false)
    }

    fun setDisclaimerSeen(seen: Boolean = true) {
        prefs.edit().putBoolean(KEY_DISCLAIMER_SEEN, seen).apply()
    }

    companion object {
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_DISCLAIMER_SEEN = "has_seen_disclaimer"
    }
}
