package com.xin.dev.main

import android.util.Log
import com.axon.dev.modulex.api.anno.Service
import com.xin.dev.main.api.MainApi

/**
 *  Created by HuangXin on 2025/1/13.
 */
@Service
class MainApiImpl : MainApi {
    override fun refresh(): Int {
        Log.i(TAG, "refresh: $this")
        return 1
    }

    override fun doSomeThing() {
        Log.i(TAG, "doSomeThing: $this")
    }

    companion object {
        private const val TAG = "MainApiImpl"
    }
}