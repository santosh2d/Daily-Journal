package com.example.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoUtils {
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "DailyJournalSecretKey"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12

    init {
        try {
            getOrCreateSecretKey()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) {
            return existingKey
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    /**
     * Encrypts plain text string and prepends the Initialization Vector (IV).
     */
    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val key = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv ?: ByteArray(IV_SIZE)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            
            val combinedBytes = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combinedBytes, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combinedBytes, iv.size, encryptedBytes.size)
            
            Base64.encodeToString(combinedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            plainText // Fallback in case of absolute keystore failure
        }
    }

    /**
     * Decrypts ciphertext where the first 12 bytes are the GCM IV.
     */
    fun decrypt(encryptedText: String): String {
        if (encryptedText.isEmpty()) return ""
        return try {
            val combinedBytes = Base64.decode(encryptedText, Base64.NO_WRAP)
            if (combinedBytes.size < IV_SIZE) return encryptedText

            val iv = ByteArray(IV_SIZE)
            System.arraycopy(combinedBytes, 0, iv, 0, IV_SIZE)

            val encryptedBytes = ByteArray(combinedBytes.size - IV_SIZE)
            System.arraycopy(combinedBytes, IV_SIZE, encryptedBytes, 0, encryptedBytes.size)

            val key = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            "[Decryption Error]"
        }
    }
}
