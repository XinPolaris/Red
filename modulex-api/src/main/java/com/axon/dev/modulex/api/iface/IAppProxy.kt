package com.axon.dev.modulex.api.iface

import android.app.Application
import java.util.concurrent.ConcurrentHashMap

/**
 *  Created by HuangXin on 2025/1/13.
 */
abstract class IAppProxy {
    protected val modules = mutableListOf<IModuleProxy>()
    protected val services = ConcurrentHashMap<Class<*>, Class<*>>()

    fun init() {
        modules.forEach { module ->
            services.putAll(module.getServices())
        }
    }

    abstract fun initModules()

    fun onCreate(application: Application) {
        modules.forEach { it.onCreate(application) }
    }
}