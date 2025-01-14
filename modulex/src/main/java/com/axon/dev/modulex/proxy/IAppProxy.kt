package com.axon.dev.modulex.proxy

import android.app.Application
import java.util.concurrent.ConcurrentHashMap

/**
 *  Created by HuangXin on 2025/1/13.
 */
open class IAppProxy {
    private val modules = mutableListOf<IModuleProxy>()
    internal val services = ConcurrentHashMap<Class<*>, Creator<*>>()

    fun init() {
        initModules(modules)
        modules.forEach { module ->
            module.initServices(services)
        }
    }

    open fun initModules(modules: MutableList<IModuleProxy>) {}

    fun onCreate(application: Application) {
        modules.forEach { it.onCreate(application) }
    }
}