package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TextMuted

@Composable
fun StartupSplashScreen(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("startup_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background glow
        Box(
            modifier = Modifier
                .size(320.dp)
                .scale(pulseScale)
                .alpha(0.18f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor, Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Rotating geometric emblem
            Box(
                modifier = Modifier
                    .size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer rotating geometric frame
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .rotate(rotationAngle)
                        .background(
                            color = accentColor.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(32.dp)
                        )
                )
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .rotate(rotationAngle + 45f)
                        .background(
                            color = accentColor.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(32.dp)
                        )
                )

                // Central glowing icon badge with minimalist icon
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(pulseScale)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFF59E0B), accentColor)
                            ),
                            shape = RoundedCornerShape(30.dp)
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.alhuda_minimal_icon_1791477132069),
                        contentDescription = "Al-Huda Minimalist Emblem",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(28.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "AL-HUDA",
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "THE DIVINE GUIDANCE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp,
                color = accentColor
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Animated loading bar
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(3.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
            ) {
                val progressAnim by infiniteTransition.animateFloat(
                    initialValue = 0.1f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "bar"
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressAnim)
                        .background(accentColor, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}
