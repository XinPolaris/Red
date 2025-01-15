package com.xin.dev.posts

import android.app.Application
import android.util.Log
import com.xin.dev.modulex.api.IModule
import com.xin.dev.modulex.api.anno.Module

/**
 *  Created by HuangXin on 2025/1/15.
 */
@Module
class PostsModule : IModule {
    override fun onCreate(application: Application) {
        Log.i(TAG, "onCreate: ")
    }

    companion object {
        private const val TAG = "PostsModule"
    }
}