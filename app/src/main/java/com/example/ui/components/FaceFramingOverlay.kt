package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricGreen
import com.example.ui.theme.Slate950
import com.example.ui.theme.SoftBlue
import java.io.File

@Composable
fun FaceFramingOverlay(
    modifier: Modifier = Modifier,
    ghostImagePath: String? = null,
    ghostOpacity: Float = 0.35f,
    showGhost: Boolean = true,
    showGrid: Boolean = true,
    alignmentScore: Int = 96,
    alignmentStatus: String = "Face Centered - Ready"
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("face_framing_overlay")
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        // 1. Ghost Overlay of previous photo (if exists & enabled)
        if (showGhost && !ghostImagePath.isNullOrEmpty()) {
            val bitmap = remember(ghostImagePath) {
                try {
                    val file = File(ghostImagePath)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                    } else null
                } catch (_: Exception) {
                    null
                }
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Yesterday Ghost Overlay",
                    contentScale = ContentScale.Crop,
                    alpha = ghostOpacity.coerceIn(0.10f, 0.70f),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 2. Vector Framing Guides & 3x3 Grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Optional 3x3 Grid
            if (showGrid) {
                val gridStroke = 1.dp.toPx()
                val gridColor = Color(0x33FFFFFF)
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                // Vertical lines
                drawLine(
                    color = gridColor,
                    start = Offset(width / 3f, 0f),
                    end = Offset(width / 3f, height),
                    strokeWidth = gridStroke,
                    pathEffect = pathEffect
                )
                drawLine(
                    color = gridColor,
                    start = Offset(2 * width / 3f, 0f),
                    end = Offset(2 * width / 3f, height),
                    strokeWidth = gridStroke,
                    pathEffect = pathEffect
                )
                // Horizontal lines
                drawLine(
                    color = gridColor,
                    start = Offset(0f, height / 3f),
                    end = Offset(width, height / 3f),
                    strokeWidth = gridStroke,
                    pathEffect = pathEffect
                )
                drawLine(
                    color = gridColor,
                    start = Offset(0f, 2 * height / 3f),
                    end = Offset(width, 2 * height / 3f),
                    strokeWidth = gridStroke,
                    pathEffect = pathEffect
                )
            }

            // Central Face Framing Oval
            val ovalWidth = width * 0.58f
            val ovalHeight = height * 0.44f
            val ovalLeft = (width - ovalWidth) / 2f
            val ovalTop = height * 0.20f

            // Outer dark vignette mask around the face oval
            drawOval(
                color = ElectricGreen.copy(alpha = 0.85f),
                topLeft = Offset(ovalLeft, ovalTop),
                size = Size(ovalWidth, ovalHeight),
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 16f), 0f)
                )
            )

            // Eye Level Guideline (horizontal reference at 38% of oval)
            val eyeY = ovalTop + (ovalHeight * 0.38f)
            val eyeLineWidth = ovalWidth * 0.65f
            val eyeLeft = (width - eyeLineWidth) / 2f
            drawLine(
                color = SoftBlue.copy(alpha = 0.7f),
                start = Offset(eyeLeft, eyeY),
                end = Offset(eyeLeft + eyeLineWidth, eyeY),
                strokeWidth = 2.dp.toPx()
            )

            // Center vertical symmetry line (forehead to chin)
            val centerX = width / 2f
            drawLine(
                color = SoftBlue.copy(alpha = 0.5f),
                start = Offset(centerX, ovalTop + 20f),
                end = Offset(centerX, ovalTop + ovalHeight - 20f),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
            )

            // Corner ticks for futuristic camera HUD
            val cornerLen = 28.dp.toPx()
            val cornerStroke = 3.dp.toPx()
            val hudPad = 24.dp.toPx()

            // Top Left
            drawLine(ElectricGreen, Offset(hudPad, hudPad), Offset(hudPad + cornerLen, hudPad), cornerStroke)
            drawLine(ElectricGreen, Offset(hudPad, hudPad), Offset(hudPad, hudPad + cornerLen), cornerStroke)
            // Top Right
            drawLine(ElectricGreen, Offset(width - hudPad, hudPad), Offset(width - hudPad - cornerLen, hudPad), cornerStroke)
            drawLine(ElectricGreen, Offset(width - hudPad, hudPad), Offset(width - hudPad, hudPad + cornerLen), cornerStroke)
            // Bottom Left
            drawLine(ElectricGreen, Offset(hudPad, height - hudPad), Offset(hudPad + cornerLen, height - hudPad), cornerStroke)
            drawLine(ElectricGreen, Offset(hudPad, height - hudPad), Offset(hudPad, height - hudPad - cornerLen), cornerStroke)
            // Bottom Right
            drawLine(ElectricGreen, Offset(width - hudPad, height - hudPad), Offset(width - hudPad - cornerLen, height - hudPad), cornerStroke)
            drawLine(ElectricGreen, Offset(width - hudPad, height - hudPad), Offset(width - hudPad, height - hudPad - cornerLen), cornerStroke)
        }

        // 3. Dynamic Alignment Feedback Banner at Top
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .background(Slate950.copy(alpha = 0.75f), RoundedCornerShape(20.dp))
                .border(1.dp, ElectricGreen.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Alignment Status",
                    tint = ElectricGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = alignmentStatus,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "$alignmentScore%",
                    color = ElectricGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
