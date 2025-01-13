package com.xin.dev.red

import android.app.Application
import com.axon.dev.modulex.api.ModuleX

/**
 *  Created by HuangXin on 2025/1/2.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ModuleX.init()
    }
}