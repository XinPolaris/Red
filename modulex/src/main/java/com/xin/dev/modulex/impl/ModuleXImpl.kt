package com.xin.dev.modulex.impl

import android.app.Activity
import android.app.Application
import android.app.Fragment
import android.util.Log
import android.view.View
import com.xin.dev.modulex.ModuleX
import com.xin.dev.modulex.placeholder.PlaceholderActivity
import com.xin.dev.modulex.placeholder.PlaceholderFragment
import com.xin.dev.modulex.placeholder.PlaceholderFragmentX
import com.xin.dev.modulex.placeholder.PlaceholderView
import com.xin.dev.modulex.proxy.AppProxy
import com.xin.dev.modulex.util.Utils
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap

/**
 *  Created by HuangXin on 2025/1/13.
 */
@Suppress("UNCHECKED_CAST")
internal class ModuleXImpl {
    private val servicesMap = ConcurrentHashMap<Class<*>, Any>()
    private lateinit var appProxy: AppProxy

    fun init(application: Application) {
        try {
            val clazz = Class.forName("${NAME_APP_PACKAGE}.${NAME_APP_NAME}")
            appProxy = clazz.getDeclaredConstructor().newInstance() as? AppProxy ?: AppProxy()
        } catch (cls: ClassNotFoundException) {
            Log.e(TAG, "ModuleX initialization failed")
        }
        if (!::appProxy.isInitialized) appProxy = AppProxy()
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
                Log.e(TAG, "No implementation class found for the $clazz")
                newInstance = Proxy.newProxyInstance(
                    clazz.classLoader, arrayOf(clazz)
                ) { _: Any?, method: Method, _: Array<Any?>? ->
                    Log.e(
                        TAG,
                        "No implementation class found for the ${clazz.name}, method invoke fail: ${method.name}()"
                    )
                    if (method.returnType.isPrimitive) {
                        return@newProxyInstance Utils.getDefaultValueForPrimitiveType(method.returnType)
                    }
                    null
                } as T?
            }
            servicesMap[clazz] = newInstance!!
        }
        return servicesMap[clazz] as T
    }

    fun clazzView(uri: String): Class<out View> {
        return (appProxy.clazzMap[uri] ?: PlaceholderView::class.java) as Class<out View>
    }

    fun clazzFragment(uri: String): Class<out Fragment> {
        return (appProxy.clazzMap[uri] ?: PlaceholderFragment::class.java) as Class<out Fragment>
    }

    fun clazzFragmentX(uri: String): Class<out androidx.fragment.app.Fragment> {
        return (appProxy.clazzMap[uri]
            ?: PlaceholderFragmentX::class.java) as Class<out androidx.fragment.app.Fragment>
    }

    fun clazzActivity(uri: String): Class<out Activity> {
        return (appProxy.clazzMap[uri]
            ?: PlaceholderActivity::class.java) as Class<out Activity>
    }

    companion object {
        private const val TAG = ModuleX.TAG
        private const val NAME_APP_PACKAGE = "com.xin.dev.modulex.apppxy"
        private const val NAME_APP_NAME = "com_xin_dev_modulex_AppProxy"
    }
}