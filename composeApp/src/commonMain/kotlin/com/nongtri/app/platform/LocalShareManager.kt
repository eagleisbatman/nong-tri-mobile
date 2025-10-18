package com.nongtri.app.platform

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal to provide ShareManager throughout the app
 */
val LocalShareManager = staticCompositionLocalOf<ShareManager> {
    error("ShareManager not provided")
}
