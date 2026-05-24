package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val encryptedTitle: String,
    val encryptedContent: String,
    val encryptedMood: String,
    val encryptedImagePath: String?,
    val timestamp: Long
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 0, // Single user config, id is always 0
    val name: String = "Journaler",
    val bio: String = "Every day is a story.",
    val profileImagePath: String? = null,
    val email: String = "",
    val phone: String = "",
    val isVerified: Boolean = false,
    val isCloudSyncEnabled: Boolean = false
)
