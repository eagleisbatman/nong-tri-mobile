package com.nongtri.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nongtri.app.l10n.Language
import com.nongtri.app.l10n.LocalizationProvider
import com.nongtri.app.ui.theme.NongTriColors

@Composable
fun LanguageSelectionScreen(
    onLanguageSelected: (Language) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedLanguage by remember { mutableStateOf<Language?>(null) }
    var isButtonVisible by remember { mutableStateOf(false) }

    // Animate background gradient
    val infiniteTransition = rememberInfiniteTransition()
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NongTriColors.Primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .testTag("language_selection_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App logo/name with animation
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.testTag("language_header")
                ) {
                    Text(
                        text = "🌾",
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Nông Trí",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = NongTriColors.Primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your AI Farming Assistant",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Language selection title
            Text(
                text = "Select Language / Chọn ngôn ngữ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .testTag("language_title")
            )

            // Language options
            Language.entries.forEach { language ->
                LanguageCard(
                    language = language,
                    isSelected = selectedLanguage == language,
                    onClick = {
                        selectedLanguage = language
                        isButtonVisible = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Continue button with animation
            AnimatedVisibility(
                visible = isButtonVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Button(
                    onClick = {
                        selectedLanguage?.let { onLanguageSelected(it) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("continue_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NongTriColors.Primary
                    )
                ) {
                    Text(
                        text = when (selectedLanguage) {
                            Language.VIETNAMESE -> "Tiếp tục"
                            else -> "Continue"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageCard(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val borderColor = if (isSelected) {
        NongTriColors.Primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    val backgroundColor = if (isSelected) {
        NongTriColors.Primary.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .testTag("language_card_${language.code}"),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = language.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) {
                        NongTriColors.Primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = language.code.uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NongTriColors.Primary,
                    modifier = Modifier.testTag("selected_icon_${language.code}")
                )
            }
        }
    }
}
