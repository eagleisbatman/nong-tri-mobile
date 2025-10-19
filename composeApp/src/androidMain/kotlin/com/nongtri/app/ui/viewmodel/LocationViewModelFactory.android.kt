package com.nongtri.app.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberLocationViewModel(): LocationViewModel {
    val context = LocalContext.current
    return remember {
        LocationViewModel().apply {
            initialize(context.applicationContext)
        }
    }
}
