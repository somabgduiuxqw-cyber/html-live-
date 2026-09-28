package com.example.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.data.git.GitCredentials

class GitCredentialStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("htmllive_git_credentials", Context.MODE_PRIVATE)

    fun getCredentials(repoUrlOrHost: String): GitCredentials? {
        val host = extractHost(repoUrlOrHost)
        val username = prefs.getString("user_$host", null) ?: return null
        val token = prefs.getString("token_$host", null) ?: return null
        return GitCredentials(username = username, tokenOrPassword = token)
    }

    fun saveCredentials(repoUrlOrHost: String, credentials: GitCredentials) {
        val host = extractHost(repoUrlOrHost)
        prefs.edit()
            .putString("user_$host", credentials.username)
            .putString("token_$host", credentials.tokenOrPassword)
            .apply()
    }

    fun removeCredentials(repoUrlOrHost: String) {
        val host = extractHost(repoUrlOrHost)
        prefs.edit()
            .remove("user_$host")
            .remove("token_$host")
            .apply()
    }

    private fun extractHost(url: String): String {
        return try {
            val clean = url.removePrefix("https://").removePrefix("http://").substringBefore("/")
            clean.ifEmpty { "github.com" }
        } catch (_: Exception) {
            "github.com"
        }
    }
}
