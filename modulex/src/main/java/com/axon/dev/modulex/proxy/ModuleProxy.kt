package com.axon.dev.modulex.proxy

import android.app.Application

/**
 *  Created by HuangXin on 2025/1/13.
 */
abstract class ModuleProxy {
    abstract fun onCreate(application: Application)

    abstract fun initServices(services: MutableMap<Class<*>, Creator<*>>)

    abstract fun initClazz(clazzMap: MutableMap<String, Class<*>>)
}

interface Creator<T> {
    fun create(): T
}