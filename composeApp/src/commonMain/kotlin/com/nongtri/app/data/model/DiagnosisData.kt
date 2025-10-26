package com.nongtri.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Plant diagnosis data from AgriVision MCP server
 * Matches backend diagnosis_data JSONB structure
 */
@Serializable
data class DiagnosisData(
    @SerialName("crop") val crop: Crop? = null,
    @SerialName("health_status") val healthStatus: String = "Unable to assess",
    @SerialName("health_confidence") val healthConfidence: String? = null,
    @SerialName("issues") val issues: List<Issue> = emptyList(),
    @SerialName("growth_stage") val growthStage: String? = null,
    @SerialName("image_quality") val imageQuality: String = "Unknown",
    @SerialName("diagnostic_notes") val diagnosticNotes: String? = null,
    @SerialName("agriculture_api_reference") val agricultureApiReference: Boolean? = null,
    @SerialName("analyzed_at") val analyzedAt: String? = null
)

/**
 * Crop identification information
 */
@Serializable
data class Crop(
    @SerialName("name_en") val nameEn: String = "Unknown",
    @SerialName("name_vi") val nameVi: String = "Không xác định",
    @SerialName("scientific_name") val scientificName: String? = null,
    @SerialName("confidence") val confidence: String = "None"  // "High" | "Medium" | "Low" | "None"
)

/**
 * Plant health issue (disease, pest, deficiency)
 */
@Serializable
data class Issue(
    @SerialName("name") val name: String,
    @SerialName("scientific_name") val scientificName: String? = null,
    @SerialName("category") val category: String,  // "Fungal Disease" | "Pest" | "Nutrient Deficiency" | etc.
    @SerialName("severity") val severity: String,  // "Low" | "Moderate" | "High" | "Critical"
    @SerialName("stage") val stage: String? = null,  // "Early" | "Active" | "Late"
    @SerialName("affected_parts") val affectedParts: List<String> = emptyList(),  // ["Leaves", "Stems", "Fruit"]
    @SerialName("symptoms") val symptoms: List<String> = emptyList(),
    @SerialName("causal_agent") val causalAgent: String? = null  // "Fungus" | "Bacteria" | "Virus" | "Insect"
)

/**
 * Extension functions for easier UI display
 */

/**
 * Get health status color for UI
 */
fun DiagnosisData.getHealthStatusColor(): HealthStatusColor {
    return when (healthStatus.lowercase()) {
        "healthy" -> HealthStatusColor.GREEN
        "mild issue", "minor issue" -> HealthStatusColor.YELLOW
        "moderate issue" -> HealthStatusColor.ORANGE
        "severe issue", "critical issue" -> HealthStatusColor.RED
        else -> HealthStatusColor.GRAY
    }
}

/**
 * Health status color enum for UI rendering
 */
enum class HealthStatusColor {
    GREEN,   // Healthy
    YELLOW,  // Mild issue
    ORANGE,  // Moderate issue
    RED,     // Severe/Critical
    GRAY     // Unknown/Unable to assess
}

/**
 * Get severity icon for UI
 */
fun Issue.getSeverityIcon(): String {
    return when (severity.lowercase()) {
        "low" -> "⚠️"
        "moderate" -> "⚠️⚠️"
        "high" -> "🔴"
        "critical" -> "🔴🔴"
        else -> "ℹ️"
    }
}
