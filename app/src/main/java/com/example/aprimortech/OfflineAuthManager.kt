package com.example.aprimortech

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class OfflineAuthManager(private val context: Context) {
    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREF_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveCredentials(email: String, password: String) {
        val normalizedEmail = email.trim().lowercase()
        val salt = generateSalt()
        val hash = hashPassword(password, salt)
        prefs.edit()
            .putString(KEY_EMAIL, normalizedEmail)
            .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
            .putLong(KEY_LAST_LOGIN, System.currentTimeMillis())
            .apply()
    }

    fun validateCredentials(email: String, password: String): Boolean {
        val storedEmail = prefs.getString(KEY_EMAIL, null) ?: return false
        if (storedEmail != email.trim().lowercase()) return false
        if (!isSessionValid()) return false
        val saltB64 = prefs.getString(KEY_SALT, null) ?: return false
        val hashB64 = prefs.getString(KEY_HASH, null) ?: return false
        val salt = Base64.decode(saltB64, Base64.NO_WRAP)
        val expectedHash = Base64.decode(hashB64, Base64.NO_WRAP)
        val candidate = hashPassword(password, salt)
        return constantTimeEquals(expectedHash, candidate)
    }

    fun hasOfflineUser(): Boolean =
        prefs.getString(KEY_EMAIL, null) != null &&
                prefs.getString(KEY_SALT, null) != null &&
                prefs.getString(KEY_HASH, null) != null

    fun getStoredEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun isSessionValid(maxDays: Int = SESSION_VALID_DAYS): Boolean {
        val last = prefs.getLong(KEY_LAST_LOGIN, -1L)
        if (last <= 0) return false
        val now = System.currentTimeMillis()
        return (now - last) <= (maxDays * DAY_MS)
    }

    fun refreshSessionTimestamp() {
        if (hasOfflineUser()) {
            prefs.edit().putLong(KEY_LAST_LOGIN, System.currentTimeMillis()).apply()
        }
    }

    fun clearCredentials() {
        prefs.edit().clear().apply()
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LEN)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LEN_BITS)
        val skf = SecretKeyFactory.getInstance(ALGO)
        return skf.generateSecret(spec).encoded
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }

    companion object {
        private const val PREF_FILE = "offline_auth"
        private const val KEY_EMAIL = "email"
        private const val KEY_SALT = "salt"
        private const val KEY_HASH = "hash"
        private const val KEY_LAST_LOGIN = "last_login"
        private const val ALGO = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 12000
        private const val KEY_LEN_BITS = 256
        private const val SALT_LEN = 16
        private const val SESSION_VALID_DAYS = 5
        private const val DAY_MS = 24 * 60 * 60 * 1000L
    }
}
