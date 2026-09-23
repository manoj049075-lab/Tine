package com.example.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricGreen
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.SoftBlue

data class OnboardingStep(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val steps = listOf(
        OnboardingStep(
            title = "Capture Your Journey",
            subtitle = "Your life, one frame at a time. Record daily selfies to witness subtle changes compound into stunning transformations.",
            icon = Icons.Default.CameraAlt
        ),
        OnboardingStep(
            title = "Match Yesterday with Ghost Overlay",
            subtitle = "Align your facial angle, eye line, and distance with pinpoint transparency so every frame in your movie aligns seamlessly.",
            icon = Icons.Default.Layers
        ),
        OnboardingStep(
            title = "Private & Secure Storage",
            subtitle = "Your photos are strictly stored in private app storage. No public feeds, no unapproved uploads, and on-device alignment.",
            icon = Icons.Default.Shield
        ),
        OnboardingStep(
            title = "Open Your Future with Time Capsule",
            subtitle = "Lock your journey for 3, 6, or 12 months. When the capsule unlocks, watch your high-speed transformation video.",
            icon = Icons.Default.HourglassBottom
        )
    )

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val step = steps[currentStepIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(24.dp)
            .testTag("onboarding_screen")
    ) {
        // Skip Button
        if (currentStepIndex < steps.size - 1) {
            TextButton(
                onClick = onFinishOnboarding,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .testTag("skip_onboarding_btn")
            ) {
                Text("Skip", color = Slate400, fontSize = 14.sp)
            }
        }

        // Center Content
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboarding_content"
            ) { targetStep ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Slate900)
                            .border(2.dp, ElectricGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = targetStep.icon,
                            contentDescription = targetStep.title,
                            tint = ElectricGreen,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = targetStep.title,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = targetStep.subtitle,
                        color = Slate400,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Step Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                steps.indices.forEach { index ->
                    val isSelected = index == currentStepIndex
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 24.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) ElectricGreen else Slate800)
                    )
                }
            }
        }

        // Bottom Action Button
        Button(
            onClick = {
                if (currentStepIndex < steps.size - 1) {
                    currentStepIndex++
                } else {
                    onFinishOnboarding()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(52.dp)
                .testTag("onboarding_action_btn")
        ) {
            Text(
                text = if (currentStepIndex == steps.size - 1) "Create My Capsule" else "Next",
                color = Slate950,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
