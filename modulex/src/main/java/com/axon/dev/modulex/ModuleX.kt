package com.axon.dev.modulex

import android.app.Application
import android.util.Log
import com.axon.dev.modulex.impl.ModuleXImpl
import com.axon.dev.modulex.proxy.IAppProxy

/**
 *  Created by HuangXin on 2025/1/13.
 */
object ModuleX {
    const val TAG = "ModuleX"
    private val moduleXImpl by lazy { ModuleXImpl() }

    @JvmStatic
    fun init(application: Application) {
        moduleXImpl.init(application)
    }

    @JvmStatic
    fun <T> getService(clazz: Class<T>): T {
        return moduleXImpl.getService(clazz)
    }
}