package com.axon.dev.modulex.api.impl

import android.app.Application
import android.util.Log
import com.axon.dev.modulex.ModuleX
import com.axon.dev.modulex.proxy.IAppProxy
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap

/**
 *  Created by HuangXin on 2025/1/13.
 */
@Suppress("UNCHECKED_CAST")
internal class ModuleXImpl {
    private val appProxyName = "com.axon.dev.modulex.ksp._AppProxy"
    private val servicesMap = ConcurrentHashMap<Class<*>, Any>()
    private lateinit var appProxy: IAppProxy

    fun init(application: Application) {
        try {
            val clazz = Class.forName(appProxyName)
            appProxy = clazz.getDeclaredConstructor().newInstance() as? IAppProxy ?: IAppProxy()
        } catch (cls: ClassNotFoundException) {
            Log.e(TAG, "ModuleX initialization failed")
        }
        if (!::appProxy.isInitialized) appProxy = IAppProxy()
        appProxy.init()
        appProxy.onCreate(application)
    }

    fun <T> getService(clazz: Class<T>): T {
        if (!clazz.isInterface) throw RuntimeException("The parameter clazz (${clazz}) must be an interface!")
        if (clazz.declaredConstructors.any { it.parameterTypes.isNotEmpty() }) {
            throw RuntimeException("The parameter clazz (${clazz}) must not have any constructor with parameters!")
        }
        servicesMap[clazz] ?: run {
            var newInstance = appProxy.services[clazz]?.create() as? T
            if (newInstance == null) {
                Log.e(TAG, "No implementation class found for the interface $clazz")
                newInstance = Proxy.newProxyInstance(
                    clazz.classLoader, arrayOf(clazz)
                ) { _, _, _ -> null } as T?
            }
            servicesMap[clazz] = newInstance!!
        }
        return servicesMap[clazz] as T
    }

    companion object {
        private const val TAG = ModuleX.TAG
    }
}