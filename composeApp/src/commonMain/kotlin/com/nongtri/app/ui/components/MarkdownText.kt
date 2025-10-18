package com.nongtri.app.ui.components

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
 */
@Composable
fun MarkdownText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    // Clean up escaped characters that GPT-4o sometimes sends
    val processedText = text
        .replace("\\n", "\n")
        .replace("\\\"", "\"")
        .replace("\\'", "'")
        .replace("\\\\", "\\")

    Markdown(
        content = processedText,
        colors = markdownColor(
            text = color,
            codeText = color,
            linkText = color,
            codeBackground = color.copy(alpha = 0.1f)
        ),
        typography = markdownTypography(
            h1 = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            h2 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            h3 = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            h4 = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            h5 = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            h6 = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            text = MaterialTheme.typography.bodyLarge,
            code = MaterialTheme.typography.bodyMedium,
            quote = MaterialTheme.typography.bodyMedium,
            paragraph = MaterialTheme.typography.bodyLarge,
            ordered = MaterialTheme.typography.bodyLarge,
            bullet = MaterialTheme.typography.bodyLarge,
            list = MaterialTheme.typography.bodyLarge
        ),
        modifier = modifier
    )
}
