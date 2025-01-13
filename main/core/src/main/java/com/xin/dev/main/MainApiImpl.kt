package com.xin.dev.main

import android.util.Log
import com.axon.dev.modulex.api.anno.Module
import com.xin.dev.main.api.MainApi

/**
 *  Created by HuangXin on 2025/1/13.
 */
@Module
class MainApiImpl : MainApi {
    override fun refresh(): Int {
        Log.i(TAG, "refresh: ")
        return 1
    }

    override fun doSomeThing() {
        Log.i(TAG, "doSomeThing: ")
    }

    companion object {
        private const val TAG = "MainApiImpl"
    }
}