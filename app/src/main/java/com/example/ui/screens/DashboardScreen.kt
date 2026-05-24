package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.JournalEntry
import com.example.ui.JournalViewModel
import com.example.ui.SyncUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: JournalViewModel,
    onNavigateToAddEdit: (id: Int) -> Unit,
    onNavigateToDetail: (id: Int) -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.allEntries.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val isReminderEnabled by viewModel.reminderEnabled.collectAsState()
    val reminderRawTime by viewModel.reminderTime.collectAsState()

    val formattedReminderTime = remember(isReminderEnabled, reminderRawTime) {
        if (!isReminderEnabled) {
            "Off"
        } else {
            val hour = reminderRawTime.first
            val minute = reminderRawTime.second
            val amPm = if (hour >= 12) "PM" else "AM"
            val displayHour = if (hour % 12 == 0) 12 else hour % 12
            val displayMinute = String.format(Locale.getDefault(), "%02d", minute)
            "$displayHour:$displayMinute $amPm"
        }
    }

    var selectedMoodFilter by remember { mutableStateOf("All") }

    val moods = listOf("All", "😀 Joyful", "😔 Melancholy", "⚡ Energized", "🍃 Calm", "🤔 Reflective", "🔥 Stressed")

    // Filtered entries
    val filteredEntries = remember(entries, selectedMoodFilter) {
        if (selectedMoodFilter == "All") {
            entries
        } else {
            entries.filter { it.mood.contains(selectedMoodFilter.split(" ").last()) || it.mood == selectedMoodFilter }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Menu book icon styled like the professional HTML header component
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD0BCFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = "Journal Icon Logo",
                                tint = Color(0xFF381E72),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Text(
                            text = "My Journal",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Profile/Settings picture shortcut on far right header
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4A4458))
                            .border(1.dp, Color(0xFF938F99), CircleShape)
                            .clickable { onNavigateToProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userProfile.profileImagePath.isNullOrEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(userProfile.profileImagePath),
                                contentDescription = "User profile configuration shortcut",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = userProfile.name.take(1).uppercase(Locale.getDefault()),
                                color = Color(0xFFD0BCFF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddEdit(-1) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .testTag("add_entry_fab")
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add New Encrypted Entry",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Live Cloud Sync Indicator Banner
            AnimatedVisibility(
                visible = userProfile.isCloudSyncEnabled && syncState !is SyncUiState.Idle,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    val context = LocalContext.current
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        when (val state = syncState) {
                            is SyncUiState.Syncing -> {
                                CircularProgressIndicator(
                                    progress = { state.progress },
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Backing up memories with end-to-end sync...",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            is SyncUiState.Success -> {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Sync Finished Logo",
                                    tint = Color(0xFF81C784),
                                    modifier = Modifier.size(18.dp)
                                )
                                val timeStr = remember(state.lastSyncedTime) {
                                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(state.lastSyncedTime))
                                }
                                Text(
                                    text = "Private cloud backup active. Synced at $timeStr",
                                    fontSize = 12.sp,
                                    color = Color(0xFF81C784),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }

            // High Fidelity Header Greeting with Encryption Status Banner (Matching Design Specs)
            val currentDayString = remember {
                SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(Date())
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentDayString,
                        fontSize = 13.sp,
                        color = Color(0xFFCCC4D0),
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("How's your day, ")
                            withStyle(style = SpanStyle(color = Color(0xFFD0BCFF), fontWeight = FontWeight.SemiBold)) {
                                append(userProfile.name)
                            }
                            append("?")
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Encrypted Lock Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2B2930))
                        .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encryption Shield lock icon",
                        tint = Color(0xFF81C784),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "ENCRYPTED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCCC4D0),
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Cryptographic Security Banner Label
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2B2930))
                    .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Lock Shield Indicator",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Hardware KeyStore Encryption Enabled (AES-256-GCM)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Choice Mood Selector
            Text(
                text = "Filter by Mood:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(moods) { mood ->
                    val isSelected = (selectedMoodFilter == mood)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedMoodFilter = mood },
                        label = { Text(mood) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEADDFF),
                            selectedLabelColor = Color(0xFF21005D),
                            containerColor = Color(0xFF2B2930),
                            labelColor = Color(0xFFCCC4D0)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color(0xFF4A4458),
                            selectedBorderColor = Color(0xFFD0BCFF)
                        ),
                        modifier = Modifier.testTag("mood_chip_${mood.replace(" ", "_").lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Journal List
            if (filteredEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color(0xFF4A4458)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = "Empty memory layout icon",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Your encrypted pages are blank",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (selectedMoodFilter == "All") {
                                    "Tap the + button to capture your thoughts, add memories, and track today's mood under total privacy."
                                } else {
                                    "You don't have any days saved under '$selectedMoodFilter' yet."
                                },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredEntries, key = { it.id }) { entry ->
                        JournalEntryCard(
                            entry = entry,
                            onClick = { onNavigateToDetail(entry.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("entry_card_${entry.id}")
                        )
                    }
                }
            }

            // Bottom Double Grid Status Cards (Matching the design spec exactly)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Reminder Status Widget
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF2B2930), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4A4458)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Daily Reminder Clock",
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "REMINDER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCCC4D0),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = formattedReminderTime,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE6E1E5)
                        )
                    }
                }

                // Cloud Sync Status Widget
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF2B2930), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4A4458)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (userProfile.isCloudSyncEnabled) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = "Cloud Backup Sync Logo Status",
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "CLOUD SYNC",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCCC4D0),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (userProfile.isCloudSyncEnabled) "Active" else "Offline",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE6E1E5)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JournalEntryCard(
    entry: JournalEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStr = remember(entry.timestamp) {
        val sdf = SimpleDateFormat("EEEE, MMM dd, yyyy  •  hh:mm a", Locale.getDefault())
        sdf.format(Date(entry.timestamp))
    }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF4A4458)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Illustrated dynamic top card backdrop if image is specified
            if (!entry.imagePath.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    // Check if is predefined colorful gradient template
                    if (entry.imagePath.startsWith("gradient:")) {
                        val gradientName = entry.imagePath.removePrefix("gradient:")
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(getBrushForGradient(gradientName))
                        )
                    } else {
                        Image(
                            painter = rememberAsyncImagePainter(entry.imagePath),
                            contentDescription = "Journal custom visual photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Floating mood emoji on top of the image
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = entry.mood,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                if (entry.imagePath.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Inline mood rating
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEADDFF), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = entry.mood,
                                fontSize = 12.sp,
                                color = Color(0xFF21005D),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = entry.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = entry.content,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                Spacer(modifier = Modifier.height(8.dp))

                // Date and Encryption lock stamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "AES GCM Security shield icon",
                            tint = Color(0xFF81C784).copy(alpha = 0.8f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "AES Encrypted",
                            fontSize = 10.sp,
                            color = Color(0xFF81C784).copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Global gradient brushes helper
fun getBrushForGradient(name: String): Brush {
    return when (name) {
        "Dawn Glow" -> Brush.linearGradient(listOf(Color(0xFFFFA07A), Color(0xFFFF4500)))
        "Midnight Echo" -> Brush.linearGradient(listOf(Color(0xFF2E0854), Color(0xFF0F111A)))
        "Forest Breeze" -> Brush.linearGradient(listOf(Color(0xFF556B2F), Color(0xFF8FBC8F)))
        "Cosmic Dream" -> Brush.linearGradient(listOf(Color(0xFF3F51B5), Color(0xFFE91E63)))
        "Zen Garden" -> Brush.linearGradient(listOf(Color(0xFF80CBC4), Color(0xFF004D40)))
        else -> Brush.linearGradient(listOf(Color(0xFF6200EE), Color(0xFF03DAC5)))
    }
}
