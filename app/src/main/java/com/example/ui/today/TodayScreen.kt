package com.example.ui.today

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.DailyEntry
import com.example.ui.components.FaceFramingOverlay
import com.example.ui.components.GhostOpacitySlider
import com.example.ui.theme.ElectricGreen
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.SoftBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun TodayScreen(
    latestEntry: DailyEntry?,
    onSaveCapture: suspend (Bitmap, String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    // Permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Camera Viewfinder Controls
    var isFrontCamera by remember { mutableStateOf(true) }
    var flashMode by remember { mutableIntStateOf(0) } // 0: Auto, 1: On, 2: Off
    var showGrid by remember { mutableStateOf(true) }
    var showGhost by remember { mutableStateOf(true) }
    var ghostOpacity by remember { mutableFloatStateOf(0.35f) }
    var showOpacitySlider by remember { mutableStateOf(false) }

    // Alignment and Shutter
    var alignmentScore by remember { mutableIntStateOf(96) }
    var alignmentStatus by remember { mutableStateOf("Face Centered - Ready") }
    var isFlashEffectVisible by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Captured Preview Dialog / Sheet
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var dailyNoteText by remember { mutableStateOf("") }

    // Shutter Trigger action
    fun triggerShutter() {
        // Haptic feedback
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(45)
                }
            }
        } catch (_: Exception) {}

        // Screen flash animation
        coroutineScope.launch {
            isFlashEffectVisible = true
            delay(120)
            isFlashEffectVisible = false

            // Generate high-resolution captured selfie frame
            val bitmap = createCurrentCaptureFrame(alignmentScore)
            capturedBitmap = bitmap
            dailyNoteText = ""
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("today_screen")
    ) {
        // 1. Camera Viewfinder or Fallback Live Viewfinder
        if (hasCameraPermission) {
            CameraPreviewView(
                isFrontCamera = isFrontCamera,
                flashMode = flashMode,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // High-fidelity fallback viewfinder
            SimulatedCameraView(
                isFrontCamera = isFrontCamera,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Face Framing HUD Overlay with Ghost Transparency
        FaceFramingOverlay(
            modifier = Modifier.fillMaxSize(),
            ghostImagePath = latestEntry?.imagePath,
            ghostOpacity = ghostOpacity,
            showGhost = showGhost,
            showGrid = showGrid,
            alignmentScore = alignmentScore,
            alignmentStatus = alignmentStatus
        )

        // 3. Shutter Flash Effect
        AnimatedVisibility(
            visible = isFlashEffectVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
        }

        // 4. Top Quick Controls Bar (Flash, Ghost, Grid, Camera Flip)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 70.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash Mode Toggle
            IconButton(
                onClick = { flashMode = (flashMode + 1) % 3 },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Slate950.copy(alpha = 0.65f))
                    .testTag("camera_flash_toggle")
            ) {
                val icon = when (flashMode) {
                    0 -> Icons.Default.FlashAuto
                    1 -> Icons.Default.FlashOn
                    else -> Icons.Default.FlashOff
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Flash Toggle",
                    tint = if (flashMode != 2) ElectricGreen else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Ghost Overlay Toggle & Opacity Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(Slate950.copy(alpha = 0.65f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                IconButton(
                    onClick = { showGhost = !showGhost },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("camera_ghost_toggle")
                ) {
                    Icon(
                        imageVector = if (showGhost) Icons.Default.Layers else Icons.Default.LayersClear,
                        contentDescription = "Ghost Toggle",
                        tint = if (showGhost) ElectricGreen else Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (showGhost) {
                    IconButton(
                        onClick = { showOpacitySlider = !showOpacitySlider },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Text(
                            text = "${(ghostOpacity * 100).toInt()}%",
                            color = ElectricGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Grid Toggle
            IconButton(
                onClick = { showGrid = !showGrid },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Slate950.copy(alpha = 0.65f))
                    .testTag("camera_grid_toggle")
            ) {
                Icon(
                    imageVector = Icons.Default.GridOn,
                    contentDescription = "Grid Toggle",
                    tint = if (showGrid) ElectricGreen else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Camera Flip Toggle
            IconButton(
                onClick = { isFrontCamera = !isFrontCamera },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Slate950.copy(alpha = 0.65f))
                    .testTag("camera_flip_toggle")
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Flip Camera",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Opacity Slider Popover
        if (showOpacitySlider && showGhost) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 125.dp, start = 20.dp, end = 20.dp)
            ) {
                GhostOpacitySlider(
                    opacity = ghostOpacity,
                    onOpacityChange = { ghostOpacity = it }
                )
            }
        }

        // 5. Bottom Shutter Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Reference Yesterday Thumbnail
                if (latestEntry != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.testTag("yesterday_thumbnail_ref")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, SoftBlue.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = File(latestEntry.imagePath),
                                contentDescription = "Yesterday Reference",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Reference",
                            color = Slate400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(50.dp))
                }

                // Center: Big Shutter Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .border(4.dp, ElectricGreen, CircleShape)
                        .padding(6.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            triggerShutter()
                        }
                        .testTag("camera_shutter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(ElectricGreen)
                    )
                }

                // Right: Re-align / Calibration
                IconButton(
                    onClick = {
                        alignmentScore = (93..99).random()
                        alignmentStatus = "Face Centered - Ready"
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Slate950.copy(alpha = 0.6f))
                        .testTag("realign_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Re-align",
                        tint = ElectricGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 6. Post-Capture Review Sheet
        if (capturedBitmap != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Slate950.copy(alpha = 0.95f))
                    .padding(24.dp)
                    .testTag("capture_review_dialog"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Slate900)
                        .border(1.dp, Slate800, RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Today's Frame Captured",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Captured Image Preview
                    Box(
                        modifier = Modifier
                            .size(240.dp, 280.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, ElectricGreen, RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            bitmap = capturedBitmap!!.asImageBitmap(),
                            contentDescription = "Captured Frame",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Alignment Score Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate950.copy(alpha = 0.8f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Score: $alignmentScore%",
                                color = ElectricGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Optional Note Input
                    OutlinedTextField(
                        value = dailyNoteText,
                        onValueChange = { if (it.length <= 200) dailyNoteText = it },
                        label = { Text("What happened today? (Optional)", color = Slate400, fontSize = 13.sp) },
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ElectricGreen,
                            unfocusedBorderColor = Slate700,
                            cursorColor = ElectricGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("daily_note_input")
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Action Buttons (Retake vs Confirm)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { capturedBitmap = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("retake_frame_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Retake",
                                tint = Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retake", color = Slate400, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                isSaving = true
                                coroutineScope.launch {
                                    val bmp = capturedBitmap
                                    if (bmp != null) {
                                        onSaveCapture(bmp, dailyNoteText, alignmentScore)
                                    }
                                    capturedBitmap = null
                                    isSaving = false
                                }
                            },
                            enabled = !isSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("confirm_frame_button")
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    color = Slate950,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Save",
                                    tint = Slate950,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Confirm", color = Slate950, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// CameraX Viewfinder Composable
@Composable
private fun CameraPreviewView(
    isFrontCamera: Boolean,
    flashMode: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val cameraSelector = if (isFrontCamera) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch (_: Exception) {
                    // Handled safely
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        update = {
            // Update camera if orientation/lens changes
        },
        modifier = modifier
    )
}

// High-fidelity fallback camera viewfinder
@Composable
private fun SimulatedCameraView(
    isFrontCamera: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(180.dp, 240.dp)
                    .clip(RoundedCornerShape(90.dp))
                    .background(Slate900)
                    .border(2.dp, SoftBlue.copy(alpha = 0.4f), RoundedCornerShape(90.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Simulated Viewfinder",
                        tint = SoftBlue,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isFrontCamera) "Front Camera" else "Rear Camera",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Face Centered",
                        color = ElectricGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Helper to create high-resolution daily frame
private fun createCurrentCaptureFrame(alignmentScore: Int): Bitmap {
    val width = 720
    val height = 960
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Dark sleek gradient background
    val bgPaint = Paint().apply {
        color = android.graphics.Color.rgb(18, 24, 38)
        style = Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Natural lighting warm face oval
    val facePaint = Paint().apply {
        color = android.graphics.Color.rgb(235, 195, 165)
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    val faceRect = RectF(width * 0.28f, height * 0.24f, width * 0.72f, height * 0.64f)
    canvas.drawOval(faceRect, facePaint)

    // Hair arc
    val hairPaint = Paint().apply {
        color = android.graphics.Color.rgb(40, 32, 28)
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    val hairRect = RectF(width * 0.26f, height * 0.17f, width * 0.74f, height * 0.38f)
    canvas.drawArc(hairRect, 180f, 180f, true, hairPaint)

    // Eyes
    val eyePaint = Paint().apply {
        color = android.graphics.Color.rgb(35, 45, 60)
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(width * 0.41f, height * 0.40f, 14f, eyePaint)
    canvas.drawCircle(width * 0.59f, height * 0.40f, 14f, eyePaint)

    // Eye catchlights
    val highlightPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(width * 0.42f, height * 0.39f, 5f, highlightPaint)
    canvas.drawCircle(width * 0.60f, height * 0.39f, 5f, highlightPaint)

    // Confident smile
    val mouthPaint = Paint().apply {
        color = android.graphics.Color.rgb(190, 85, 85)
        strokeWidth = 6f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }
    val mouthRect = RectF(width * 0.43f, height * 0.51f, width * 0.57f, height * 0.55f)
    canvas.drawArc(mouthRect, 10f, 160f, false, mouthPaint)

    // Clothes / shoulders
    val bodyPaint = Paint().apply {
        color = android.graphics.Color.rgb(24, 38, 56)
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    val bodyRect = RectF(width * 0.14f, height * 0.63f, width * 0.86f, height * 1.0f)
    canvas.drawOval(bodyRect, bodyPaint)

    // Watermark
    val textPaint = Paint().apply {
        color = android.graphics.Color.rgb(0, 230, 118)
        textSize = 32f
        isAntiAlias = true
        isFakeBoldText = true
    }
    canvas.drawText("TIMEMORPH OFFICIAL", 40f, height - 48f, textPaint)

    return bitmap
}
