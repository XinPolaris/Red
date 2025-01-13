package com.axon.dev.modulex.api.impl

import com.axon.dev.modulex.api.iface.IAppProxy
import java.util.concurrent.ConcurrentHashMap

/**
 *  Created by HuangXin on 2025/1/13.
 */
@Suppress("UNCHECKED_CAST")
class ModuleXImpl {
    private val appProxyName = "com.axon.dev.modulex.\$AppProxy"
    private val servicesMap = ConcurrentHashMap<Class<*>, Any>()
    private lateinit var appProxy: IAppProxy

    fun init() {
        val clazz = Class.forName(appProxyName)
        appProxy = clazz.getDeclaredConstructor().newInstance() as? IAppProxy ?: IAppProxy()
        appProxy.init()
    }

    fun <T> getService(clazz: Class<T>): T {
        if (!clazz.isInterface) throw RuntimeException("参数clazz（${clazz.name}）必须是接口！")
        if (clazz.declaredConstructors.any { it.parameterTypes.isNotEmpty() }) {
            throw RuntimeException("参数clazz（${clazz.name}）不能具有带参数的构造函数！")
        }
        servicesMap[clazz] ?: run {
            var newInstance = appProxy.services[clazz]?.create<T>()
            if (newInstance == null) {
                newInstance = clazz.getDeclaredConstructor().newInstance()
            }
            servicesMap[clazz] = newInstance!!
        }
        return servicesMap[clazz] as T
    }
}