package com.nongtri.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography

/**
 * Professional markdown renderer using multiplatform-markdown-renderer
 * Supports full CommonMark spec with proper parsing and rendering
 * With smooth fade-in animation for streaming text
 */
@Composable
fun MarkdownText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    isStreaming: Boolean = false  // Add flag to disable animation during streaming
) {
    // Clean up escaped characters and extra newlines that GPT-4o sometimes sends
    val processedText = text
        .replace("\\n", "\n")
        .replace("\\\"", "\"")
        .replace("\\'", "'")
        .replace("\\\\", "\\")
        // Preserve one blank line between paragraphs (convert 2+ newlines to exactly 2)
        .replace(Regex("\n{3,}"), "\n\n")  // Collapse 3+ newlines to 2
        // Note: Keep \n\n for proper paragraph spacing

    // Custom markdown colors for better visual treatment
    val customColors = markdownColor(
        text = color,
        codeText = MaterialTheme.colorScheme.primary,
        linkText = MaterialTheme.colorScheme.primary,
        codeBackground = MaterialTheme.colorScheme.surfaceVariant,
        dividerColor = MaterialTheme.colorScheme.outlineVariant,
        inlineCodeBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )

    // Custom typography for consistent text styling
    val customTypography = markdownTypography(
        h1 = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        h2 = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        h3 = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        h4 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        h5 = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        h6 = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
        text = MaterialTheme.typography.bodyLarge,
        code = MaterialTheme.typography.bodyMedium,
        quote = MaterialTheme.typography.bodyLarge,
        paragraph = MaterialTheme.typography.bodyLarge,
        ordered = MaterialTheme.typography.bodyLarge,
        bullet = MaterialTheme.typography.bodyLarge,
        list = MaterialTheme.typography.bodyLarge
    )

    // During streaming, render directly without animation to prevent flickering
    // Only animate once when the message is complete
    if (isStreaming) {
        // Direct rendering - no animation during streaming
        Markdown(
            content = processedText,
            colors = customColors,
            typography = customTypography,
            modifier = modifier
        )
    } else {
        // Animate content only when message is complete (not streaming)
        AnimatedContent(
            targetState = processedText,
            transitionSpec = {
                // Fade in + slide up from bottom (like ChatGPT, Claude, etc.)
                (fadeIn(animationSpec = tween(300)) +
                        slideInVertically(
                            initialOffsetY = { it / 10 },  // Start 10% down
                            animationSpec = tween(300)
                        ))
                    .togetherWith(fadeOut(animationSpec = tween(200)))
            },
            label = "markdown_complete_animation",
            modifier = modifier
        ) { animatedText ->
            Markdown(
                content = animatedText,
                colors = customColors,
                typography = customTypography
            )
        }
    }
}
