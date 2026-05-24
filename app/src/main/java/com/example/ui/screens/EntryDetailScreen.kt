package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.JournalEntry
import com.example.ui.JournalViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    entryId: Int,
    viewModel: JournalViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (id: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var entry by remember { mutableStateOf<JournalEntry?>(null) }
    val scope = rememberCoroutineScope()

    // Fetch the decrypted journal entry dynamically from repository (transparently decrypts values)
    LaunchedEffect(entryId) {
        entry = viewModel.allEntries.value.find { it.id == entryId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reflection Page") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    entry?.let { currentEntry ->
                        IconButton(
                            onClick = { onNavigateToEdit(currentEntry.id) },
                            modifier = Modifier.testTag("edit_entry_button")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Current Reflection")
                        }
                        IconButton(
                            onClick = {
                                viewModel.deleteJournalEntry(currentEntry)
                                onNavigateBack()
                            },
                            modifier = Modifier.testTag("delete_entry_button")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Reflection permanently")
                        }
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
        if (entry == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val currentEntry = entry!!
            val dateStr = remember(currentEntry.timestamp) {
                val sdf = SimpleDateFormat("EEEE, MMMM dd, yyyy  •  hh:mm a", Locale.getDefault())
                sdf.format(Date(currentEntry.timestamp))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {
                // If there's an image/gradient
                if (!currentEntry.imagePath.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        if (currentEntry.imagePath.startsWith("gradient:")) {
                            val gradientName = currentEntry.imagePath.removePrefix("gradient:")
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(getBrushForGradient(gradientName))
                            )
                        } else {
                            Image(
                                painter = rememberAsyncImagePainter(currentEntry.imagePath),
                                contentDescription = "Reflection full size photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Bottom ambient fade / overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.61f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Mood choice: ${currentEntry.mood}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    if (currentEntry.imagePath.isNullOrEmpty()) {
                        // Display mood chips inline if there's no picture
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEADDFF), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Mood Choice: ${currentEntry.mood}",
                                fontSize = 13.sp,
                                color = Color(0xFF21005D),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Encryption Guard Banner
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
                                contentDescription = "AES key design",
                                tint = Color(0xFF81C784),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Decrypted on-device. AES-256 GCM hardware storage active.",
                                color = Color(0xFF81C784),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = currentEntry.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = currentEntry.content,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            lineHeight = 24.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}
