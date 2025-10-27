package com.nongtri.app.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownPadding
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
    // Clean up escaped characters and extra newlines that GPT-4o sometimes sends
    val processedText = text
        .replace("\\n", "\n")
        .replace("\\\"", "\"")
        .replace("\\'", "'")
        .replace("\\\\", "\\")
        // Reduce multiple consecutive newlines to maximum of 1 (no blank lines between paragraphs)
        .replace(Regex("\n{2,}"), "\n")

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
        ),
        // Reduce paragraph spacing for tighter, cleaner formatting
        // Default is 8.dp for paragraphs which creates excessive spacing
        padding = markdownPadding(
            block = PaddingValues(vertical = 4.dp),  // Reduced from default 8.dp
            indentList = PaddingValues(start = 16.dp)
        ),
        modifier = modifier
    )
}
