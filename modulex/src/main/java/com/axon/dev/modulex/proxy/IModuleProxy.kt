package com.axon.dev.modulex.proxy

import android.app.Application

/**
 *  Created by HuangXin on 2025/1/13.
 */
abstract class IModuleProxy {
    abstract fun onCreate(application: Application)

    abstract fun initServices(services: MutableMap<Class<*>, Creator<*>>)
}

interface Creator<T> {
    fun create(): T
}