package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.JournalEntry
import com.example.data.JournalRepository
import com.example.data.UserProfileEntity
import com.example.utils.ReminderManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: JournalRepository
    private val reminderManager: ReminderManager

    init {
        val database = AppDatabase.getDatabase(application)
        repository = JournalRepository(database.journalDao())
        reminderManager = ReminderManager(application)
    }

    // --- Authentication State ---
    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    // --- Reminder State ---
    private val _reminderEnabled = MutableStateFlow(reminderManager.isEnabled)
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled.asStateFlow()

    private val _reminderTime = MutableStateFlow(Pair(reminderManager.hour, reminderManager.minute))
    val reminderTime: StateFlow<Pair<Int, Int>> = _reminderTime.asStateFlow()

    // --- Sync Simulation State ---
    private val _syncState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncState: StateFlow<SyncUiState> = _syncState.asStateFlow()

    // --- Streams from Database ---
    val allEntries: StateFlow<List<JournalEntry>> = repository.getAllEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userProfile: StateFlow<UserProfileEntity> = repository.getUserProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfileEntity()
        )

    // Flow collector for Authentication Sync
    init {
        viewModelScope.launch {
            userProfile.collect { profile ->
                _authState.update {
                    it.copy(
                        isLoggedIn = profile.isVerified,
                        userEmail = profile.email,
                        userPhone = profile.phone
                    )
                }
            }
        }
    }

    // --- Login Actions ---
    fun selectLoginMethod(isEmail: Boolean) {
        _authState.update { it.copy(isEmailSelected = isEmail, otpSent = false, otpError = null, countdown = 0) }
    }

    fun onContactInputChanged(value: String) {
        _authState.update { it.copy(contactInput = value, otpError = null) }
    }

    fun onOtpInputChanged(value: String) {
        _authState.update { it.copy(otpInput = value, otpError = null) }
    }

    fun sendOtp() {
        val input = _authState.value.contactInput.trim()
        if (input.isEmpty()) {
            _authState.update { it.copy(otpError = "Input cannot be empty") }
            return
        }

        val isEmail = _authState.value.isEmailSelected
        if (isEmail && !android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            _authState.update { it.copy(otpError = "Please enter a valid email address") }
            return
        } else if (!isEmail && input.length < 9) {
            _authState.update { it.copy(otpError = "Please enter a valid mobile number") }
            return
        }

        // Generate a real simulated 6-digit OTP code to satisfy "No Dead Ends" and show to user
        val code = (100000..999999).random().toString()
        _authState.update {
            it.copy(
                isSendingOtp = true,
                otpError = null
            )
        }

        viewModelScope.launch {
            delay(1000) // Simulating network lag
            _authState.update {
                it.copy(
                    isSendingOtp = false,
                    otpSent = true,
                    simulatedCode = code,
                    otpInput = "",
                    countdown = 30
                )
            }
            // Starts countdown timer
            startOtpCountdown()
        }
    }

    private fun startOtpCountdown() {
        viewModelScope.launch {
            while (_authState.value.countdown > 0) {
                delay(1000)
                _authState.update { it.copy(countdown = it.countdown - 1) }
            }
        }
    }

    fun verifyOtp() {
        val uiState = _authState.value
        if (uiState.otpInput.trim() != uiState.simulatedCode) {
            _authState.update { it.copy(otpError = "Invalid verification code. Please try again.") }
            return
        }

        _authState.update { it.copy(isVerifying = true) }

        viewModelScope.launch {
            delay(1200) // Simulate validation step
            val currentProfile = userProfile.value
            val isEmail = uiState.isEmailSelected
            val emailValue = if (isEmail) uiState.contactInput else currentProfile.email
            val phoneValue = if (!isEmail) uiState.contactInput else currentProfile.phone

            val updatedProfile = currentProfile.copy(
                email = emailValue,
                phone = phoneValue,
                isVerified = true,
                name = currentProfile.name.ifBlank { if (isEmail) emailValue.substringBefore("@") else "User" }
            )
            repository.saveUserProfile(updatedProfile)

            _authState.update {
                it.copy(
                    isVerifying = false,
                    isLoggedIn = true,
                    simulatedCode = null,
                    contactInput = "",
                    otpInput = ""
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val currentProfile = userProfile.value
            val anonymousProfile = currentProfile.copy(isVerified = false)
            repository.saveUserProfile(anonymousProfile)
            _authState.update { AuthUiState() } // Reset local State
        }
    }

    // --- Profile Actions ---
    fun updateProfile(name: String, bio: String, photoPath: String?) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(
                name = name,
                bio = bio,
                profileImagePath = photoPath
            )
            repository.saveUserProfile(updated)
        }
    }

    fun toggleCloudSync(enabled: Boolean) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(isCloudSyncEnabled = enabled)
            repository.saveUserProfile(updated)
            if (enabled) {
                simulateCloudSync()
            } else {
                _syncState.value = SyncUiState.Idle
            }
        }
    }

    fun simulateCloudSync() {
        viewModelScope.launch {
            _syncState.value = SyncUiState.Syncing(0.15f)
            delay(800)
            _syncState.value = SyncUiState.Syncing(0.55f)
            delay(1000)
            _syncState.value = SyncUiState.Syncing(0.90f)
            delay(500)
            _syncState.value = SyncUiState.Success(System.currentTimeMillis())
        }
    }

    // --- Journal Entry Actions ---
    fun addJournalEntry(title: String, content: String, mood: String, imagePath: String?) {
        viewModelScope.launch {
            val newEntry = JournalEntry(
                title = title.ifBlank { "Untitled Reflection" },
                content = content,
                mood = mood,
                imagePath = imagePath,
                timestamp = System.currentTimeMillis()
            )
            repository.saveEntry(newEntry)
            if (userProfile.value.isCloudSyncEnabled) {
                simulateCloudSync()
            }
        }
    }

    fun updateJournalEntry(id: Int, title: String, content: String, mood: String, imagePath: String?, timestamp: Long) {
        viewModelScope.launch {
            val updated = JournalEntry(
                id = id,
                title = title.ifBlank { "Untitled Reflection" },
                content = content,
                mood = mood,
                imagePath = imagePath,
                timestamp = timestamp
            )
            repository.saveEntry(updated)
            if (userProfile.value.isCloudSyncEnabled) {
                simulateCloudSync()
            }
        }
    }

    fun deleteJournalEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }

    // --- Reminder Settings ---
    fun saveReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        reminderManager.updateReminder(enabled, hour, minute)
        _reminderEnabled.value = enabled
        _reminderTime.value = Pair(hour, minute)
    }
}

// UI State Classes
data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val isEmailSelected: Boolean = true, // true if Email Tab, false if SMS Tab
    val contactInput: String = "",
    val otpInput: String = "",
    val isSendingOtp: Boolean = false,
    val otpSent: Boolean = false,
    val simulatedCode: String? = null,
    val countdown: Int = 0,
    val isVerifying: Boolean = false,
    val otpError: String? = null,
    val userEmail: String = "",
    val userPhone: String = ""
)

sealed interface SyncUiState {
    object Idle : SyncUiState
    data class Syncing(val progress: Float) : SyncUiState
    data class Success(val lastSyncedTime: Long) : SyncUiState
}
