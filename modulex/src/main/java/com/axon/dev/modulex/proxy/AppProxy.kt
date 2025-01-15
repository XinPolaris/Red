package com.axon.dev.modulex.proxy

import android.app.Application
import java.util.concurrent.ConcurrentHashMap

/**
 *  Created by HuangXin on 2025/1/13.
 */
open class AppProxy {
    private val modules = mutableListOf<ModuleProxy>()
    internal val services = ConcurrentHashMap<Class<*>, Creator<*>>()
    internal val clazzMap = ConcurrentHashMap<String, Class<*>>()

    fun init() {
        initModules(modules)
        modules.forEach { module ->
            module.initServices(services)
            module.initClazz(clazzMap)
        }
    }

    open fun initModules(modules: MutableList<ModuleProxy>) {}

    fun onCreate(application: Application) {
        modules.forEach { it.onCreate(application) }
    }
}