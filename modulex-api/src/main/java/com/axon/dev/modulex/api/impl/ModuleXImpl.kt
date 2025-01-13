package com.axon.dev.modulex.api.impl

import com.axon.dev.modulex.api.iface.IAppProxy
import java.util.concurrent.ConcurrentHashMap

/**
 *  Created by HuangXin on 2025/1/13.
 */
class ModuleXImpl {
    private val appProxyName = "com.axon.dev.modulex.\$AppProxy"
    private val servicesMap = ConcurrentHashMap<String, Any>()
    private var appProxy: IAppProxy? = null

    fun init() {
        val clazz = Class.forName(appProxyName)
        appProxy = clazz.getDeclaredConstructor().newInstance() as? IAppProxy
        appProxy?.init()
    }

    fun <T> getService(clazz: Class<T>): T {
        return servicesMap[clazz] ?: clazz.newInstance()
    }
}