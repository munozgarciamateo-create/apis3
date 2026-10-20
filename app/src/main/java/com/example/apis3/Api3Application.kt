package com.example.apis3

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory

class Apis3Application : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .respectCacheHeaders(false)
            .build()
}