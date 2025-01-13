package com.axon.dev.modulex.api.iface

import android.app.Application

/**
 *  Created by HuangXin on 2025/1/13.
 */
abstract class IModuleProxy {
    abstract fun init()

    abstract fun onCreate(application: Application)

    @Suppress("UNCHECKED_CAST")
    abstract fun getServices(): Map<Class<*>, Creator>
}

interface Creator {
    fun <T> create(): T
}