package com.axon.dev.modulex

import android.app.Application
import com.axon.dev.modulex.api.impl.ModuleXImpl

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