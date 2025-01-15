package com.xin.dev.red

import android.app.Application
import com.xin.dev.modulex.ModuleX
import com.xin.dev.modulex.api.anno.App
import com.xin.dev.main.api.MainApi

/**
 *  Created by HuangXin on 2025/1/2.
 */
@App
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ModuleX.init(this)
        ModuleX.getService(MainApi::class.java).refresh()
        ModuleX.getService(MainApi::class.java).doSomeThing()
    }
}