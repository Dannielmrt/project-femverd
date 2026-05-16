package com.example.femverd.data

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_JWT_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_JWT_TOKEN, null)
    }

    fun clearToken() {
        prefs.edit().remove(KEY_JWT_TOKEN).apply()
    }

    companion object {
        private const val PREF_NAME = "femverd_preferences"
        private const val KEY_JWT_TOKEN = "jwt_auth_token"
    }
}