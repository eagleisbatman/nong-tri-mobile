package com.nongtri.app

import android.app.Application
import android.util.Log
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.PlatformContext
import com.nongtri.app.util.Base64ImageFetcher

class NongTriApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        Log.i("NongTriApplication", "✅ Application onCreate() called")

        // Explicitly set the singleton ImageLoader factory
        SingletonImageLoader.setSafe { context ->
            Log.i("NongTriApplication", "✅ Building singleton ImageLoader with Base64ImageFetcher")
            ImageLoader.Builder(context)
                .components {
                    add(Base64ImageFetcher.Factory())
                }
                .build()
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        Log.i("NongTriApplication", "✅ newImageLoader() called - registering Base64ImageFetcher")
        return ImageLoader.Builder(context)
            .components {
                add(Base64ImageFetcher.Factory())
            }
            .build()
    }
}
