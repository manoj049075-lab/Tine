package com.example.ui.capsule

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.DailyEntry
import com.example.data.TimeCapsule
import com.example.ui.components.CountdownCard
import com.example.ui.theme.ElectricGreen
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.SoftBlue
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun CapsuleScreen(
    capsules: List<TimeCapsule>,
    entries: List<DailyEntry>,
    onCreateCapsule: (String, Int, Boolean, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var activePlayingCapsule by remember { mutableStateOf<TimeCapsule?>(null) }
    var showExportModal by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .testTag("capsule_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                // Screen Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Time Capsules",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lock your journey today. Unlock your evolution later.",
                            color = Slate400,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .border(1.dp, ElectricGreen.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .testTag("create_capsule_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Capsule",
                            tint = ElectricGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "New",
                            color = ElectricGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick demo playback banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Slate900)
                        .border(1.dp, SoftBlue.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .clickable {
                            if (entries.isNotEmpty()) {
                                activePlayingCapsule = capsules.firstOrNull() ?: TimeCapsule(
                                    id = "preview",
                                    title = "Preview Journey",
                                    durationMonths = 3,
                                    startDate = System.currentTimeMillis() - 864000000L,
                                    unlockDate = System.currentTimeMillis()
                                )
                            }
                        }
                        .padding(16.dp)
                        .testTag("transformation_player_banner")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SoftBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = "Player",
                                tint = SoftBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Preview Transformation Player",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Watch ${entries.size} captured frames at 3 FPS time-lapse",
                                color = Slate400,
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Open Player",
                            tint = ElectricGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Capsule Countdown Cards
            items(capsules) { capsule ->
                val targetDays = capsule.durationMonths * 30
                CountdownCard(
                    capsule = capsule,
                    capturedDays = entries.size.coerceAtMost(targetDays),
                    totalTargetDays = targetDays,
                    onPlayClick = {
                        activePlayingCapsule = capsule
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Full Screen Transformation Movie Player Dialog
        if (activePlayingCapsule != null && entries.isNotEmpty()) {
            TransformationPlayerModal(
                capsule = activePlayingCapsule!!,
                entries = entries,
                onDismiss = { activePlayingCapsule = null },
                onExportClick = { showExportModal = true }
            )
        }

        // Create Capsule Dialog
        if (showCreateDialog) {
            CreateCapsuleDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { title, durationMonths, isStrict, note ->
                    onCreateCapsule(title, durationMonths, isStrict, note)
                    showCreateDialog = false
                }
            )
        }

        // Export Modal
        if (showExportModal) {
            ExportModal(
                entryCount = entries.size,
                onDismiss = { showExportModal = false },
                onShare = {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Witnessing my personal transformation with TimeMorph! ${entries.size} days logged.")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Transformation"))
                    showExportModal = false
                }
            )
        }
    }
}

// Fullscreen Transformation Player Modal
@Composable
fun TransformationPlayerModal(
    capsule: TimeCapsule,
    entries: List<DailyEntry>,
    onDismiss: () -> Unit,
    onExportClick: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var currentFrameIndex by remember { mutableIntStateOf(0) }
    var fps by remember { mutableIntStateOf(3) } // 1, 2, 3, 5 FPS

    LaunchedEffect(isPlaying, fps, entries.size) {
        while (isPlaying && entries.isNotEmpty()) {
            val intervalMs = (1000L / fps).coerceAtLeast(150L)
            delay(intervalMs)
            currentFrameIndex = (currentFrameIndex + 1) % entries.size
        }
    }

    val currentEntry = entries.getOrNull(currentFrameIndex) ?: entries.first()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("transformation_player_modal")
        ) {
            // Main Frame Image
            AsyncImage(
                model = File(currentEntry.imagePath),
                contentDescription = "Transformation Frame",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top Header: Close & Title & Export
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Slate950.copy(alpha = 0.7f))
                        .testTag("close_player_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Player",
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate950.copy(alpha = 0.7f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "FRAME ${currentFrameIndex + 1}/${entries.size}",
                        color = ElectricGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onExportClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Slate950.copy(alpha = 0.7f))
                        .testTag("export_player_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export Movie",
                        tint = ElectricGreen
                    )
                }
            }

            // Bottom Overlays: Date, Note Caption, & Scrub Controls
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Slate950.copy(alpha = 0.85f))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Date & Alignment Score
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentEntry.dateString,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Score: ${currentEntry.alignmentScore}%",
                        color = ElectricGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Daily Note Caption (if exists)
                if (currentEntry.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentEntry.note,
                        color = Slate300,
                        fontSize = 13.sp,
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Timeline Scrubber Slider
                Slider(
                    value = currentFrameIndex.toFloat(),
                    onValueChange = {
                        isPlaying = false
                        currentFrameIndex = it.toInt().coerceIn(0, entries.size - 1)
                    },
                    valueRange = 0f..(entries.size - 1).toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricGreen,
                        activeTrackColor = ElectricGreen,
                        inactiveTrackColor = Slate800
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Controls: FPS Selectors & Play/Pause
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // FPS Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1, 2, 3, 5).forEach { speed ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (fps == speed) ElectricGreen else Slate850)
                                    .clickable { fps = speed }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${speed}FPS",
                                    color = if (fps == speed) Slate950 else Slate400,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Play/Pause Button
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ElectricGreen)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Slate950,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

// Create Capsule Dialog
@Composable
fun CreateCapsuleDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Boolean, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedMonths by remember { mutableIntStateOf(6) }
    var isStrict by remember { mutableStateOf(true) }
    var customNote by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Slate900)
                .border(1.dp, Slate800, RoundedCornerShape(20.dp))
                .padding(20.dp)
                .testTag("create_capsule_dialog")
        ) {
            Text(
                text = "Create Time Capsule",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Capsule Title (e.g. 6-Month Evolution)", color = Slate400) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = ElectricGreen,
                    unfocusedBorderColor = Slate700
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("LOCK DURATION", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(3, 6, 12, 24).forEach { months ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedMonths == months) ElectricGreen else Slate850)
                            .clickable { selectedMonths = months }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${months}M",
                            color = if (selectedMonths == months) Slate950 else Slate400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Strict lock switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Strict Lock", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("Photos remain hidden until unlock date", color = Slate400, fontSize = 11.sp)
                }
                Switch(
                    checked = isStrict,
                    onCheckedChange = { isStrict = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = ElectricGreen)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", color = Slate400)
                }

                Button(
                    onClick = {
                        val name = if (title.isNotBlank()) title else "${selectedMonths}-Month Morph"
                        onConfirm(name, selectedMonths, isStrict, customNote)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Create", color = Slate950, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Export Modal
@Composable
fun ExportModal(
    entryCount: Int,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    var selectedRatio by remember { mutableStateOf("9:16 Portrait") }
    var selectedRes by remember { mutableStateOf("1080p") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Slate900)
                .border(1.dp, Slate800, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "Export Transformation Video",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Generate MP4 movie from $entryCount daily frames",
                color = Slate400,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            Text("ASPECT RATIO", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("9:16 Portrait", "1:1 Square", "16:9 Cinema").forEach { ratio ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedRatio == ratio) ElectricGreen else Slate850)
                            .clickable { selectedRatio = ratio }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ratio.split(" ")[0],
                            color = if (selectedRatio == ratio) Slate950 else Slate400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("RESOLUTION", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("720p HD", "1080p Full HD").forEach { res ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedRes == res) ElectricGreen else Slate850)
                            .clickable { selectedRes = res }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = res,
                            color = if (selectedRes == res) Slate950 else Slate400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Movie",
                    tint = Slate950
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export & Share", color = Slate950, fontWeight = FontWeight.Bold)
            }
        }
    }
}
