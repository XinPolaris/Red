package com.axon.dev.modulex.api.iface

import android.app.Application
import java.util.concurrent.ConcurrentHashMap

/**
 *  Created by HuangXin on 2025/1/13.
 */
open class IAppProxy {
    private val modules = mutableListOf<IModuleProxy>()
    internal val services = ConcurrentHashMap<Class<*>, Creator>()

    init {
        initModules(modules)
    }

    fun init() {
        modules.forEach { module ->
            services.putAll(module.getServices())
        }
    }

    open fun initModules(modules: MutableList<IModuleProxy>) {

    }

    fun onCreate(application: Application) {
        modules.forEach { it.onCreate(application) }
    }
}