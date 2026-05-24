package com.example.data

import com.example.security.CryptoUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Decrypted high-level model used in views
data class JournalEntry(
    val id: Int = 0,
    val title: String,
    val content: String,
    val mood: String,
    val imagePath: String?,
    val timestamp: Long
)

class JournalRepository(private val journalDao: JournalDao) {

    // Streams list of fully decrypted journal entries to the view
    fun getAllEntries(): Flow<List<JournalEntry>> {
        return journalDao.getAllEntries().map { list ->
            list.map { entity ->
                JournalEntry(
                    id = entity.id,
                    title = CryptoUtils.decrypt(entity.encryptedTitle),
                    content = CryptoUtils.decrypt(entity.encryptedContent),
                    mood = CryptoUtils.decrypt(entity.encryptedMood),
                    imagePath = entity.encryptedImagePath?.let { CryptoUtils.decrypt(it) },
                    timestamp = entity.timestamp
                )
            }
        }
    }

    suspend fun getEntryById(id: Int): JournalEntry? {
        val entity = journalDao.getEntryById(id) ?: return null
        return JournalEntry(
            id = entity.id,
            title = CryptoUtils.decrypt(entity.encryptedTitle),
            content = CryptoUtils.decrypt(entity.encryptedContent),
            mood = CryptoUtils.decrypt(entity.encryptedMood),
            imagePath = entity.encryptedImagePath?.let { CryptoUtils.decrypt(it) },
            timestamp = entity.timestamp
        )
    }

    suspend fun saveEntry(entry: JournalEntry) {
        val entity = JournalEntryEntity(
            id = entry.id,
            encryptedTitle = CryptoUtils.encrypt(entry.title),
            encryptedContent = CryptoUtils.encrypt(entry.content),
            encryptedMood = CryptoUtils.encrypt(entry.mood),
            encryptedImagePath = entry.imagePath?.let { CryptoUtils.encrypt(it) },
            timestamp = entry.timestamp
        )
        journalDao.insertEntry(entity)
    }

    suspend fun deleteEntry(entry: JournalEntry) {
        val entity = JournalEntryEntity(
            id = entry.id,
            encryptedTitle = CryptoUtils.encrypt(entry.title),
            encryptedContent = CryptoUtils.encrypt(entry.content),
            encryptedMood = CryptoUtils.encrypt(entry.mood),
            encryptedImagePath = entry.imagePath?.let { CryptoUtils.encrypt(it) },
            timestamp = entry.timestamp
        )
        journalDao.deleteEntry(entity)
    }

    fun getUserProfile(): Flow<UserProfileEntity> {
        return journalDao.getUserProfile().map { profile ->
            profile ?: UserProfileEntity() // Provide default if not set yet
        }
    }

    suspend fun saveUserProfile(profile: UserProfileEntity) {
        journalDao.insertUserProfile(profile)
    }
}
