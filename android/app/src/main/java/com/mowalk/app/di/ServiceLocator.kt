package com.mowalk.app.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object ServiceLocator {

    private lateinit var appContext: Context
    private lateinit var preferences: SharedPreferences

    fun initialize(context: Context) {
        if (::appContext.isInitialized) return

        appContext = context.applicationContext

        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        preferences = EncryptedSharedPreferences.create(
            appContext,
            "mowalk_encrypted_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getApplication(): Context {
        require(::appContext.isInitialized) { "ServiceLocator not initialized" }
        return appContext
    }

    fun getPreferences(): SharedPreferences {
        require(::preferences.isInitialized) { "Preferences not initialized" }
        return preferences
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return preferences.getString(key, defaultValue) ?: defaultValue
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return preferences.getLong(key, defaultValue)
    }

    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return preferences.getFloat(key, defaultValue)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return preferences.getBoolean(key, defaultValue)
    }

    fun edit(block: SharedPreferences.Editor.() -> Unit) {
        preferences.edit().apply(block).apply()
    }
}
