package com.nongtri.app

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.PlatformContext
import com.nongtri.app.util.Base64ImageFetcher

class NongTriApplication : Application(), SingletonImageLoader.Factory {
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(Base64ImageFetcher.Factory())
            }
            .build()
    }
}
