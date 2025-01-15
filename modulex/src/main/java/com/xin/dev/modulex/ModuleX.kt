package com.xin.dev.modulex

import android.app.Activity
import android.app.Application
import android.app.Fragment
import android.view.View
import com.xin.dev.modulex.impl.ModuleXImpl

/**
 *  Created by HuangXin on 2025/1/13.
 *
 *  组件化工具
 */
object ModuleX {
    const val TAG = "ModuleX"
    private val moduleXImpl by lazy { ModuleXImpl() }

    @JvmStatic
    fun init(application: Application) {
        moduleXImpl.init(application)
    }

    /**
     * 通过服务接口获取服务实例
     */
    @JvmStatic
    fun <T> getService(clazz: Class<T>): T {
        return moduleXImpl.getService(clazz)
    }

    /**
     * 通过URI获取View的Class
     */
    fun clazzView(uri: String): Class<out View> {
        return moduleXImpl.clazzView(uri)
    }

    fun clazzFragment(uri: String): Class<out Fragment> {
        return moduleXImpl.clazzFragment(uri)
    }

    fun clazzFragmentX(uri: String): Class<out androidx.fragment.app.Fragment> {
        return moduleXImpl.clazzFragmentX(uri)
    }

    fun clazzActivity(uri: String): Class<out Activity> {
        return moduleXImpl.clazzActivity(uri)
    }
}