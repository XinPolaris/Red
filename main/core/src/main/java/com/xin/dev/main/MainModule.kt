package com.xin.dev.main

import android.app.Application
import android.util.Log
import com.axon.dev.modulex.api.IModule
import com.axon.dev.modulex.api.anno.Module

/**
 *  Created by HuangXin on 2025/1/14.
 */
@Module
class MainModule : IModule {

    override fun onCreate(application: Application) {
        Log.i(TAG, "onCreate: ")
    }

    companion object {
        private const val TAG = "MainModule"
    }
}