package com.xin.dev.red

import android.app.Application
import com.axon.dev.modulex.ModuleX
import com.axon.dev.modulex.api.anno.App

/**
 *  Created by HuangXin on 2025/1/2.
 */
@App
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ModuleX.init(this)
    }
}