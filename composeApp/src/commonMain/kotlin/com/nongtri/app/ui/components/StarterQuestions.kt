package com.nongtri.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nongtri.app.l10n.Language
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StarterQuestions(
    language: Language,
    locationName: String? = null,
    onQuestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val questions = getStarterQuestions(language, locationName)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = if (language == Language.VIETNAMESE) {
                "Gợi ý câu hỏi:"
            } else {
                "Suggested questions:"
            },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            questions.forEach { question ->
                SuggestionChip(
                    onClick = { onQuestionClick(question) },
                    label = {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )
            }
        }
    }
}

/**
 * Determines current farming season in Vietnam based on month
 */
private fun getCurrentSeason(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val month = now.monthNumber

    return when (month) {
        12, 1, 2 -> "winter"      // Đông: December - February (Winter-Spring crop planting)
        3, 4, 5 -> "spring"        // Xuân: March - May (Spring harvest, Summer-Autumn prep)
        6, 7, 8 -> "summer"        // Hạ: June - August (Summer-Autumn crop)
        9, 10, 11 -> "autumn"      // Thu: September - November (Autumn harvest, Winter prep)
        else -> "spring"
    }
}

/**
 * Detects if location is coastal region (for aquaculture relevance)
 */
private fun isCoastalRegion(locationName: String?): Boolean {
    if (locationName == null) return false
    val coastal = listOf(
        "đà nẵng", "nha trang", "vũng tàu", "hải phòng", "quảng ninh",
        "cần thơ", "cà mau", "bạc liêu", "kiên giang", "sóc trăng",
        "da nang", "nha trang", "vung tau", "hai phong", "quang ninh",
        "can tho", "ca mau", "bac lieu", "kien giang", "soc trang"
    )
    return coastal.any { locationName.lowercase().contains(it) }
}

/**
 * Generates context-aware starter questions based on location and season
 * Note: Location is ALWAYS available (at minimum from IP address)
 */
private fun getStarterQuestions(language: Language, locationName: String?): List<String> {
    val season = getCurrentSeason()
    val isCoastal = isCoastalRegion(locationName)

    return if (language == Language.VIETNAMESE) {
        when (season) {
            "winter" -> listOf(
                "Thời tiết hôm nay thế nào?",
                "Mùa đông xuân nên trồng cây gì?",
                "Chăm sóc lúa đông xuân như thế nào?",
                if (isCoastal) "Nuôi tôm mùa này cần lưu ý gì?" else "Chăm sóc gia súc mùa đông"
            )
            "spring" -> listOf(
                "Thời tiết hôm nay thế nào?",
                "Thời điểm thu hoạch lúa xuân?",
                "Chuẩn bị vụ hè thu như thế nào?",
                if (isCoastal) "Mùa cá này đánh bắt gì?" else "Phòng sâu bệnh mùa nóng"
            )
            "summer" -> listOf(
                "Thời tiết hôm nay thế nào?",
                "Cây trồng hè thu phù hợp?",
                "Chống hạn mùa khô thế nào?",
                if (isCoastal) "Nuôi thủy sản mùa này" else "Chăm sóc lúa hè thu"
            )
            "autumn" -> listOf(
                "Thời tiết hôm nay thế nào?",
                "Thu hoạch lúa mùa như thế nào?",
                "Chuẩn bị vụ đông xuân?",
                if (isCoastal) "Thời vụ tôm mùa thu" else "Phòng bệnh gia súc mùa mưa"
            )
            else -> listOf(
                "Thời tiết hôm nay thế nào?",
                "Mùa này nên trồng cây gì?",
                "Lịch chăm sóc lúa?",
                if (isCoastal) "Nuôi tôm cá thế nào?" else "Chăm sóc gia súc"
            )
        }
    } else {
        when (season) {
            "winter" -> listOf(
                "What's today's weather?",
                "What to plant in winter-spring season?",
                "How to care for winter-spring rice?",
                if (isCoastal) "Shrimp farming tips this season?" else "Livestock care in winter"
            )
            "spring" -> listOf(
                "What's today's weather?",
                "When to harvest spring rice?",
                "How to prepare for summer-autumn crop?",
                if (isCoastal) "Fishing season guidance?" else "Pest control in hot season"
            )
            "summer" -> listOf(
                "What's today's weather?",
                "Suitable summer-autumn crops?",
                "Drought management tips?",
                if (isCoastal) "Aquaculture this season" else "Summer-autumn rice care"
            )
            "autumn" -> listOf(
                "What's today's weather?",
                "How to harvest autumn rice?",
                "Winter crop preparation?",
                if (isCoastal) "Autumn shrimp season" else "Livestock disease prevention in rainy season"
            )
            else -> listOf(
                "What's today's weather?",
                "What crops to plant this season?",
                "Rice care schedule?",
                if (isCoastal) "Shrimp and fish farming?" else "Livestock care tips"
            )
        }
    }
}
