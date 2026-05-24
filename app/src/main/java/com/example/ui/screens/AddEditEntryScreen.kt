package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEntryScreen(
    entryId: Int,
    viewModel: JournalViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isEditMode by remember { mutableStateOf(false) }

    // Form inputs state
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("😀 Joyful") }
    var selectedImagePath by remember { mutableStateOf<String?>(null) }
    var originalTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }

    // Preset atmospheric gradients to make it look stunning on emulators instantly
    val presets = listOf(
        Pair("Dawn Glow", "gradient:Dawn Glow"),
        Pair("Midnight Echo", "gradient:Midnight Echo"),
        Pair("Forest Breeze", "gradient:Forest Breeze"),
        Pair("Cosmic Dream", "gradient:Cosmic Dream"),
        Pair("Zen Garden", "gradient:Zen Garden")
    )

    val moods = listOf("😀 Joyful", "😔 Melancholy", "⚡ Energized", "🍃 Calm", "🤔 Reflective", "🔥 Stressed")

    // Retrieve active details if in edit mode
    LaunchedEffect(entryId) {
        if (entryId != -1) {
            isEditMode = true
            val entryObj = viewModel.allEntries.value.find { it.id == entryId }
            entryObj?.let {
                title = it.title
                content = it.content
                selectedMood = it.mood
                selectedImagePath = it.imagePath
                originalTimestamp = it.timestamp
            }
        }
    }

    // System Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Keep the absolute string path for loading
            selectedImagePath = it.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Reflection" else "New Reflection") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("add_edit_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isEditMode) {
                                viewModel.updateJournalEntry(
                                    id = entryId,
                                    title = title,
                                    content = content,
                                    mood = selectedMood,
                                    imagePath = selectedImagePath,
                                    timestamp = originalTimestamp
                                )
                            } else {
                                viewModel.addJournalEntry(
                                    title = title,
                                    content = content,
                                    mood = selectedMood,
                                    imagePath = selectedImagePath
                                )
                            }
                            onNavigateBack()
                        },
                        enabled = content.isNotBlank(),
                        modifier = Modifier.testTag("save_entry_button")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Save and Encrypt Memory")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Encryption Guard Stamp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Shield guard lock icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "All inputs are hardware-key AES-256 GCM encrypted.",
                        color = Color(0xFFCCC4D0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title Field input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Reflection Title (Optional)") },
                placeholder = { Text("Today was sweet...") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFE6E1E5),
                    unfocusedTextColor = Color(0xFFE6E1E5),
                    focusedLabelColor = Color(0xFFD0BCFF),
                    unfocusedLabelColor = Color(0xFFCCC4D0),
                    focusedBorderColor = Color(0xFFD0BCFF),
                    unfocusedBorderColor = Color(0xFF4A4458)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("entry_title_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Body Content field input
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("What's on your mind? (Required)") },
                placeholder = { Text("Pen down your personal secrets, memories, and events...") },
                minLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFE6E1E5),
                    unfocusedTextColor = Color(0xFFE6E1E5),
                    focusedLabelColor = Color(0xFFD0BCFF),
                    unfocusedLabelColor = Color(0xFFCCC4D0),
                    focusedBorderColor = Color(0xFFD0BCFF),
                    unfocusedBorderColor = Color(0xFF4A4458)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("entry_content_input")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mood choice selector Row
            Text(
                text = "Today's Mood Choice:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(moods) { mood ->
                    val isSelected = (selectedMood == mood)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) Color(0xFFEADDFF)
                                else Color(0xFF2B2930)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFD0BCFF) else Color(0xFF4A4458),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedMood = mood }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mood,
                            color = if (isSelected) Color(0xFF21005D)
                            else Color(0xFFCCC4D0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Photo / Graphic Backdrop additions
            Text(
                text = "Add Today's Visual Aspect:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Picker Buttons row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // System picker
                Button(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.testTag("pick_photo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Pick Local Android Photo",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose Device Photo", fontSize = 12.sp)
                }

                if (selectedImagePath != null) {
                    // Clear Image
                    TextButton(
                        onClick = { selectedImagePath = null },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Remove Image", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Atmospheric Pre-set templates row
            Text(
                text = "Or choose a preset visual ambiance:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presets) { preset ->
                    val isSelected = (selectedImagePath == preset.second)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                else Color.Transparent
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedImagePath = preset.second }
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(getBrushForGradient(preset.first))
                            )
                            Text(
                                text = preset.first,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Preview selected state (if any)
            if (selectedImagePath != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Selected aspect preview:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(6.dp))

                val isPreset = selectedImagePath!!.startsWith("gradient:")
                if (isPreset) {
                    val gradientName = selectedImagePath!!.removePrefix("gradient:")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(getBrushForGradient(gradientName))
                    )
                } else {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✓ Custom Media Picked:\n${selectedImagePath?.take(40)}...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Big Save Button
            Button(
                onClick = {
                    if (isEditMode) {
                        viewModel.updateJournalEntry(
                            id = entryId,
                            title = title,
                            content = content,
                            mood = selectedMood,
                            imagePath = selectedImagePath,
                            timestamp = originalTimestamp
                        )
                    } else {
                        viewModel.addJournalEntry(
                            title = title,
                            content = content,
                            mood = selectedMood,
                            imagePath = selectedImagePath
                        )
                    }
                    onNavigateBack()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = content.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("big_save_button")
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock Shield Lock Logo", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditMode) "Securely Encrypt & Save Details" else "Securely Encrypt & Save Memory",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
