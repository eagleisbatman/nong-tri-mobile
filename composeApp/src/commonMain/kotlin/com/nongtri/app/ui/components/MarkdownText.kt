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
    modifier: Modifier = Modifier
) {
    // Clean up escaped characters and extra newlines that GPT-4o sometimes sends
    val processedText = text
        .replace("\\n", "\n")
        .replace("\\\"", "\"")
        .replace("\\'", "'")
        .replace("\\\\", "\\")
        // Reduce multiple consecutive newlines to maximum of 1 (no blank lines between paragraphs)
        .replace(Regex("\n{2,}"), "\n")

    // Animate content changes with smooth fade-in + slide-up effect
    // This creates a beautiful reveal animation as new chunks stream in
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
        label = "markdown_streaming_animation",
        modifier = modifier
    ) { animatedText ->
        Markdown(
            content = animatedText,
            colors = markdownColor(
                text = color,
                codeText = color,
                linkText = color,
                codeBackground = color.copy(alpha = 0.1f)
            ),
            typography = markdownTypography(
                h1 = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                h2 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                h3 = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                h4 = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                h5 = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                h6 = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                text = MaterialTheme.typography.bodyLarge,
                code = MaterialTheme.typography.bodyLarge,
                quote = MaterialTheme.typography.bodyLarge,
                paragraph = MaterialTheme.typography.bodyLarge,
                ordered = MaterialTheme.typography.bodyLarge,
                bullet = MaterialTheme.typography.bodyLarge,
                list = MaterialTheme.typography.bodyLarge
            )
        )
    }
}
