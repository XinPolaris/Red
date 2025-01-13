package com.axon.dev.modulex.api

import com.axon.dev.modulex.api.impl.ModuleXImpl

/**
 *  Created by HuangXin on 2025/1/13.
 */
object ModuleX {


    private val moduleXImpl by lazy { ModuleXImpl() }

    @JvmStatic
    fun init() {
        moduleXImpl.init()
    }

    @JvmStatic
    fun <T> getService(clazz: Class<T>): T {
        return moduleXImpl.getService(clazz)
    }


}