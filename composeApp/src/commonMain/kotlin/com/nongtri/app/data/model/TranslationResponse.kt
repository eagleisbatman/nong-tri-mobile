package com.nongtri.app.data.model

import kotlinx.serialization.Serializable

/**
 * Response from GET /api/translations/:language
 */
@Serializable
data class TranslationResponse(
    val language: String,
    val count: Int,
    val translations: Map<String, String>
)
