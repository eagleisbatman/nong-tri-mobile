package com.nongtri.app

import android.app.Application
import android.util.Log
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.PlatformContext
import com.nongtri.app.util.Base64ImageFetcher
import com.nongtri.app.util.Base64DataMapper

class NongTriApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        Log.i("NongTriApplication", "✅ Application onCreate() called")
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        Log.i("NongTriApplication", "✅ Creating ImageLoader with Base64DataMapper")
        return ImageLoader.Builder(context)
            .components {
                // Register Mapper to convert base64 data URLs to ByteArray
                // Mappers run BEFORE Fetchers, so this intercepts the String before NetworkFetcher sees it
                add(Base64DataMapper())
            }
            .build()
    }
}
